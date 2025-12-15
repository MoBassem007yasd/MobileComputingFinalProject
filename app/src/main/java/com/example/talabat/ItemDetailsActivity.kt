package com.example.talabat

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ItemDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = AppDatabase.getDatabase(this)
        val restId = intent.getIntExtra("REST_ID", 0)
        val itemId = intent.getIntExtra("ITEM_ID", 0)
        val name = intent.getStringExtra("NAME") ?: ""
        val price = intent.getDoubleExtra("PRICE", 0.0)
        val imgRes = intent.getIntExtra("IMG", 0)
        val imgUri = intent.getStringExtra("IMG_URI")

        val menuItem = MenuItem(id = itemId, restaurantId = restId, name = name, description = "", price = price, imageRes = imgRes, imageUri = imgUri, category = "")

        setContent {
            var quantity by remember { mutableIntStateOf(1) }
            val scope = rememberCoroutineScope()
            val basketItems by db.appDao().getBasket().collectAsState(initial = emptyList())
            var showClearBasketDialog by remember { mutableStateOf(false) }
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
                                    updateBasketQuantity(this@ItemDetailsActivity, db.appDao(), menuItem, quantity)
                                    finish()
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

            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                if (imgUri != null) {
                    Image(painter = rememberAsyncImagePainter(model = Uri.parse(imgUri)), contentDescription = null, modifier = Modifier.fillMaxWidth().height(200.dp), contentScale = ContentScale.Crop)
                } else {
                    Image(painter = painterResource(id = imgRes), contentDescription = null, modifier = Modifier.fillMaxWidth().height(200.dp), contentScale = ContentScale.Crop)
                }

                Text(name, fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp))
                Text("$price EGP", fontSize = 20.sp, color = Color.Gray)

                Spacer(modifier = Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = { if (quantity > 1) quantity-- }) { Text("-") }
                    Text("$quantity", modifier = Modifier.padding(horizontal = 16.dp), fontSize = 24.sp)
                    Button(onClick = { quantity++ }) { Text("+") }
                }
                Button(
                    onClick = {
                        if (basketItems.isNotEmpty() && basketItems.first().restaurantId != restId) {
                            showClearBasketDialog = true
                        } else {
                            scope.launch(Dispatchers.IO) {
                                updateBasketQuantity(this@ItemDetailsActivity, db.appDao(), menuItem, quantity)
                                finish()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Add to Basket - ${price * quantity} EGP", fontSize = 18.sp)
                }
            }
        }
    }
}