package com.example.talabat

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BasketActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = AppDatabase.getDatabase(this)
        val session = SessionManager(this)
        val userEmail = session.getUserEmail() ?: ""
        val sourceRestId = intent.getIntExtra("SOURCE_REST_ID", -1)
        setContent {
            val basketItems by db.appDao().getBasket().collectAsState(initial = emptyList())
            val scope = rememberCoroutineScope()
            var deliveryFee by remember { mutableDoubleStateOf(0.0) }
            LaunchedEffect(basketItems) {
                if (basketItems.isNotEmpty()) {
                    val restId = basketItems.first().restaurantId
                    val restaurant = db.appDao().getRestaurantById(restId)
                    deliveryFee = restaurant.deliveryFee
                } else {
                    deliveryFee = 0.0
                }
            }
            val subtotal = basketItems.sumOf { it.price * it.quantity }
            val total = subtotal + deliveryFee

            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    "Basket",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
                if (basketItems.isEmpty()) {
                    Column(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_agenda),
                            contentDescription = "Empty Basket",
                            modifier = Modifier.size(100.dp),
                            tint = Color.LightGray
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "There's nothing in your cart yet",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("Ready to order?", color = Color.Gray)
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = {
                                if (sourceRestId != -1) {
                                    val intent =
                                        Intent(this@BasketActivity, MenuActivity::class.java)
                                    intent.putExtra("REST_ID", sourceRestId)
                                    intent.flags =
                                        Intent.FLAG_ACTIVITY_CLEAR_TOP // clear basket activity
                                    startActivity(intent)
                                    finish()
                                } else {
                                    finish()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))
                        ) {
                            Text("Add items")
                        }
                    }
                } else {
                    Column(modifier = Modifier.weight(1f).padding(16.dp)) {
                        Text(
                            "Basket",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(basketItems) { item ->
                                val menuItem = MenuItem(
                                    id = item.menuItemId,
                                    restaurantId = item.restaurantId,
                                    name = item.name,
                                    description = "",
                                    price = item.price,
                                    imageRes = item.imageRes,
                                    imageUri = item.imageUri,
                                    category = ""
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = {
                                                scope.launch(Dispatchers.IO) {
                                                    updateBasketQuantity(
                                                        this@BasketActivity,
                                                        db.appDao(),
                                                        menuItem,
                                                        -1
                                                    )
                                                }
                                            },
                                            modifier = Modifier.background(
                                                Color(0xFFEEEEEE),
                                                RoundedCornerShape(8.dp)
                                            ).size(32.dp)
                                        ) { Text("-", fontWeight = FontWeight.Bold) }

                                        Text(
                                            "${item.quantity}",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 12.dp)
                                        )

                                        IconButton(
                                            onClick = {
                                                scope.launch(Dispatchers.IO) {
                                                    updateBasketQuantity(
                                                        this@BasketActivity,
                                                        db.appDao(),
                                                        menuItem,
                                                        1
                                                    )
                                                }
                                            },
                                            modifier = Modifier.background(
                                                Color(0xFFFFE0B2),
                                                RoundedCornerShape(8.dp)
                                            ).size(32.dp)
                                        ) { Text("+", color = Color(0xFFFF5722)) }
                                    }
                                    Text(
                                        item.name,
                                        modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        "${item.price * item.quantity} EGP",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Divider(color = Color.LightGray)
                            }

                            item {
                                Text(
                                    "Summary",
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 24.dp)
                                )
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) { Text("Subtotal"); Text("$subtotal EGP") }
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) { Text("Delivery"); Text("$deliveryFee EGP") }
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Total", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    Text(
                                        "$total EGP",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            scope.launch(Dispatchers.IO) {
                                if (basketItems.isNotEmpty()) {
                                    val driver = db.appDao().getAvailableDriver()

                                    if (driver == null) {
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(
                                                this@BasketActivity,
                                                "No drivers available at the moment, try again later.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    } else {
                                        val restId = basketItems[0].restaurantId
                                        val restaurant = db.appDao().getRestaurantById(restId)
                                        val restName = restaurant.name
                                        val restImageRes = restaurant.imageRes
                                        val restImageUri = restaurant.imageUri

                                        val summary =
                                            basketItems.joinToString("\n") { "${it.quantity}x ${it.name}" }

                                        db.appDao().addOrderHistory(
                                            OrderHistory(
                                                restaurantId = restId,
                                                restaurantName = restName,
                                                userEmail = userEmail,
                                                driverName = driver.name,
                                                imageRes = restImageRes,
                                                imageUri = restImageUri,
                                                totalPrice = total,
                                                itemsSummary = summary,
                                                orderTime = System.currentTimeMillis()
                                            )
                                        )

                                        db.appDao().updateDriver(
                                            driver.copy(
                                                deliveriesCount = driver.deliveriesCount + 1,
                                                isBusy = true
                                            )
                                        )

                                        db.appDao().clearBasket()

                                        val intent = Intent(
                                            this@BasketActivity,
                                            OrderSuccessActivity::class.java
                                        )
                                        intent.putExtra("SUMMARY", summary)
                                        intent.putExtra("TOTAL", total)
                                        startActivity(intent)
                                        finish()
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Complete Order", fontSize = 18.sp, modifier = Modifier.padding(8.dp))
                    }
                }
            }
        }
    }
}