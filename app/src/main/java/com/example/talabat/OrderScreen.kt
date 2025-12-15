package com.example.talabat
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider // FIX: Updated Import
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf // FIX: Use standard mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OrdersScreen(dao: AppDao) {
    val context = LocalContext.current
    val session = SessionManager(context)
    val userEmail = session.getUserEmail() ?: ""
    val orders by dao.getOrdersForUser(userEmail).collectAsState(initial = emptyList())

    // FIX: Changed mutableLongStateOf -> mutableStateOf to resolve import/type errors
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = System.currentTimeMillis()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("My Orders", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))

        if (orders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No past orders found.", color = Color.Gray) }
        } else {
            LazyColumn {
                items(orders) { order ->
                    val isDelivered = (currentTime - order.orderTime) > 90000
                    val statusText = if (isDelivered) "Delivered" else "Preparing..."
                    val statusColor = if (isDelivered) Color(0xFF4CAF50) else Color(0xFFFFA000)
                    val dateString = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(order.orderTime))

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(2.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (order.imageUri != null) {
                                    Image(painter = rememberAsyncImagePainter(Uri.parse(order.imageUri)), contentDescription = null, modifier = Modifier.size(50.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                                } else {
                                    val img = if (order.imageRes != 0) order.imageRes else android.R.drawable.ic_menu_gallery
                                    Image(painter = painterResource(img), contentDescription = null, modifier = Modifier.size(50.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(order.restaurantName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text(dateString, color = Color.Gray, fontSize = 12.sp)
                                    Text("Driver: ${order.driverName}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Blue)
                                }
                                Surface(color = statusColor.copy(alpha = 0.1f), shape = RoundedCornerShape(16.dp)) {
                                    Text(text = statusText, color = statusColor, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }
                            }
                            // FIX: Replaced Divider with HorizontalDivider
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray.copy(alpha = 0.5f))

                            Text(order.itemsSummary, fontSize = 14.sp, lineHeight = 20.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Total: ${order.totalPrice} EGP", fontWeight = FontWeight.Bold, color = Color(0xFFFF5722), fontSize = 14.sp, modifier = Modifier.align(Alignment.End))
                        }
                    }
                }
            }
        }
    }
}