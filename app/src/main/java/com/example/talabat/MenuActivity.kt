package com.example.talabat

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MenuActivity : ComponentActivity() {
    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = AppDatabase.getDatabase(this)
        val restId = intent.getIntExtra("REST_ID", 0)

        setContent {
            val menuItems by db.appDao().getMenuForRestaurant(restId).collectAsState(initial = emptyList())
            val basketItems by db.appDao().getBasket().collectAsState(initial = emptyList())
            var searchText by remember { mutableStateOf("") }
            val scope = rememberCoroutineScope()

            Scaffold(
                topBar = {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        placeholder = { Text("Search menu...") },
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                },
                bottomBar = {
                    if (basketItems.isNotEmpty()) {
                        val total = basketItems.sumOf { it.price * it.quantity }
                        Button(
                            onClick = { startActivity(Intent(this, BasketActivity::class.java)) },
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("View Basket")
                                Text("$total EGP")
                            }
                        }
                    }
                }
            ) { padding ->
                val filteredItems = menuItems.filter { it.name.contains(searchText, ignoreCase = true) }
                val groupedItems = filteredItems.groupBy { it.category }

                LazyColumn(modifier = Modifier.padding(padding)) {
                    groupedItems.forEach { (category, items) ->
                        stickyHeader {
                            Text(
                                text = category,
                                fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray,
                                modifier = Modifier.fillMaxWidth().background(Color(0xFFEEEEEE)).padding(12.dp)
                            )
                        }
                        items(items) { item ->
                            MenuItemRow(item, basketItems, db, scope)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MenuItemRow(
    item: MenuItem,
    basketItems: List<BasketItem>,
    db: AppDatabase,
    scope: kotlinx.coroutines.CoroutineScope
) {
    val context = LocalContext.current
    val basketItem = basketItems.find { it.menuItemId == item.id }
    val quantity = basketItem?.quantity ?: 0
    var showClearBasketDialog by remember { mutableStateOf(false) }

    fun handleAddToBasket() {
        if (basketItems.isNotEmpty() && basketItems.first().restaurantId != item.restaurantId) {
            showClearBasketDialog = true
        } else {
            scope.launch(Dispatchers.IO) { updateBasketQuantity(context, db.appDao(), item, 1) }
        }
    }

    if (showClearBasketDialog) {
        AlertDialog(
            onDismissRequest = { showClearBasketDialog = false },
            title = { Text("Start new order?") },
            text = { Text("You have items from another restaurant in your basket. Would you like to clear them and add this item?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            db.appDao().clearBasket()
                            updateBasketQuantity(context, db.appDao(), item, 1)
                        }
                        showClearBasketDialog = false
                    }
                ) { Text("New Order", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showClearBasketDialog = false }) { Text("Cancel") }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable {
                val intent = Intent(context, ItemDetailsActivity::class.java)
                intent.putExtra("ITEM_ID", item.id)
                intent.putExtra("REST_ID", item.restaurantId)
                intent.putExtra("NAME", item.name)
                intent.putExtra("PRICE", item.price)
                intent.putExtra("IMG", item.imageRes)
                intent.putExtra("IMG_URI", item.imageUri)
                context.startActivity(intent)
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (item.imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(model = Uri.parse(item.imageUri)),
                    contentDescription = null,
                    modifier = Modifier.size(80.dp).background(Color.LightGray, RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                val img = if (item.imageRes != 0) item.imageRes else android.R.drawable.ic_menu_gallery
                Image(
                    painter = painterResource(id = img), contentDescription = null,
                    modifier = Modifier.size(80.dp).background(Color.LightGray, RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(item.description, color = Color.Gray, fontSize = 12.sp, maxLines = 2)
                Text("${item.price} EGP", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp))
            }

            if (quantity > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { scope.launch(Dispatchers.IO) { updateBasketQuantity(context, db.appDao(), item, -1) } },
                        modifier = Modifier.background(Color(0xFFEEEEEE), RoundedCornerShape(8.dp)).size(32.dp)
                    ) { Text("-", fontWeight = FontWeight.Bold) }

                    Text("$quantity", modifier = Modifier.padding(horizontal = 8.dp), fontWeight = FontWeight.Bold)

                    IconButton(
                        onClick = { handleAddToBasket() },
                        modifier = Modifier.background(Color(0xFFFFE0B2), RoundedCornerShape(8.dp)).size(32.dp)
                    ) { Text("+", color = Color(0xFFFF5722)) }
                }
            } else {
                IconButton(
                    onClick = { handleAddToBasket() },
                    modifier = Modifier.background(Color(0xFFFFE0B2), RoundedCornerShape(8.dp))
                ) { Icon(Icons.Default.Add, contentDescription = "Add", tint = Color(0xFFFF5722)) }
            }
        }
    }
}