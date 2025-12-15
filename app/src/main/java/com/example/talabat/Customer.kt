package com.example.talabat
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch

@Composable
fun MainAppLayout(dao: AppDao, onLogout: () -> Unit) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onLogout) { Text("Logout", color = Color.Red) }
            }
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, "Home") },
                    label = { Text("Main") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, "Orders") },
                    label = { Text("Orders") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (selectedTab == 0) HomeScreen(dao) else OrdersScreen(dao)
        }
    }
}

@Composable
fun HomeScreen(dao: AppDao) {
    val context = LocalContext.current
    val restaurants by dao.getAllRestaurants().collectAsState(initial = emptyList())
    val session = SessionManager(context)
    val userEmail = session.getUserEmail() ?: ""
    val myFavorites by dao.getFavoriteRestaurantsForUser(userEmail).collectAsState(initial = emptyList())
    val myFavoriteIds = myFavorites.map { it.id }
    val recentRestaurants by dao.getRecentRestaurantsForUser(userEmail).collectAsState(initial = emptyList())

    val scope = rememberCoroutineScope()
    var searchText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    val filterHelper = remember { RestaurantFilter() }
    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = searchText, onValueChange = { searchText = it },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                placeholder = { Text("Search for restaurants...") },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)
            )
            Spacer(Modifier.height(8.dp))
            LazyRow {
                item {
                    FilterChip(selected = selectedCategory == "All", onClick = { selectedCategory = "All" }, label = { Text("All") })
                    Spacer(Modifier.width(8.dp))
                }
                items(listOf("Dessert", "Oriental", "Chicken")) { cat ->
                    FilterChip(selected = selectedCategory == cat, onClick = { selectedCategory = cat }, label = { Text(cat) })
                    Spacer(Modifier.width(8.dp))
                }
            }
        }

        LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
            item {
                IconButton(
                    onClick = { context.startActivity(Intent(context, FavoritesActivity::class.java)) },
                    modifier = Modifier.padding(bottom = 8.dp)
                ) { Icon(Icons.Default.FavoriteBorder, "Favorites") }
            }

            if (recentRestaurants.isNotEmpty()) {
                item {
                    Text("Order Again", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(vertical = 8.dp))
                    LazyRow {
                        items(recentRestaurants) { order ->
                            Column(
                                modifier = Modifier.padding(end = 16.dp).clickable {
                                    val intent = Intent(context, MenuActivity::class.java)
                                    intent.putExtra("REST_ID", order.restaurantId)
                                    context.startActivity(intent)
                                },
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (order.imageUri != null) {
                                    Image(painter = rememberAsyncImagePainter(Uri.parse(order.imageUri)), contentDescription = null, modifier = Modifier.size(70.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                                } else {
                                    val img = if (order.imageRes != 0) order.imageRes else android.R.drawable.ic_menu_gallery
                                    Image(painter = painterResource(img), contentDescription = null, modifier = Modifier.size(70.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                                }
                                Text(order.restaurantName, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                }
            }

            item { Text("All Restaurants", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(vertical = 16.dp)) }
            val filteredRestaurants = filterHelper.filterRestaurants(restaurants) { restaurant ->
                restaurant.name.contains(searchText, ignoreCase = true) &&
                        (selectedCategory == "All" || restaurant.category == selectedCategory)
            }
            items(items = filteredRestaurants) { restaurant ->
                val isFav = myFavoriteIds.contains(restaurant.id)

                RestaurantCard(
                    restaurant = restaurant.copy(isFavorite = isFav),
                    onItemClick = {
                        val intent = Intent(context, MenuActivity::class.java)
                        intent.putExtra("REST_ID", restaurant.id)
                        context.startActivity(intent)
                    },
                    onFavoriteClick = {
                        scope.launch {
                            if (isFav) {
                                dao.removeUserFavorite(UserFavorite(userEmail, restaurant.id))
                            } else {
                                dao.insertUserFavorite(UserFavorite(userEmail, restaurant.id))
                            }
                        }
                    }
                )
            }
        }
    }
}
@Composable
fun RestaurantCard(restaurant: Restaurant, onItemClick: () -> Unit, onFavoriteClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { onItemClick() },
        shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(4.dp), colors = CardDefaults.cardColors(Color.White)
    ) {
        Box {
            Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Card(modifier = Modifier.size(80.dp), shape = RoundedCornerShape(8.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                    if (restaurant.imageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(model = Uri.parse(restaurant.imageUri)),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        val img = if (restaurant.imageRes != 0) restaurant.imageRes else android.R.drawable.ic_menu_gallery
                        Image(painter = painterResource(id = img), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(restaurant.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(restaurant.category, fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(painter = painterResource(android.R.drawable.star_on), null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${restaurant.rating}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${restaurant.deliveryTime} • EGP ${restaurant.deliveryFee}", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }
            IconButton(onClick = onFavoriteClick, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)) {
                Icon(imageVector = if (restaurant.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = "Favorite", tint = if (restaurant.isFavorite) Color.Red else Color.Gray, modifier = Modifier.background(Color.White.copy(alpha = 0.5f), CircleShape))
            }
        }
    }
}