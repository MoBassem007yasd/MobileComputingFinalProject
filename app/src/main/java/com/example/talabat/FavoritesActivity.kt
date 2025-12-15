package com.example.talabat

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch

class FavoritesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = AppDatabase.getDatabase(this)
        val dao = db.appDao()

        setContent {
            FavoritesScreen(dao)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(dao: AppDao) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val session = SessionManager(LocalContext.current)
    val userEmail = session.getUserEmail() ?: ""
    val favoriteRestaurants by dao.getFavoriteRestaurantsForUser(userEmail).collectAsState(initial = emptyList())
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Favorites") },
                navigationIcon = {
                    IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (favoriteRestaurants.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No favorites yet!", color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.padding(16.dp)) {
                    items(favoriteRestaurants) { restaurant ->
                        FavRestaurantCard(
                            restaurant = restaurant,
                            onItemClick = {
                                val intent = Intent(context, MenuActivity::class.java)
                                intent.putExtra("REST_ID", restaurant.id)
                                context.startActivity(intent)
                            },
                            onFavoriteClick = {
                                scope.launch {
                                    dao.removeUserFavorite(UserFavorite(userEmail, restaurant.id))
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FavRestaurantCard(restaurant: Restaurant, onItemClick: () -> Unit, onFavoriteClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { onItemClick() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            Box {
                if (restaurant.imageUri != null) {
                    Image(painter = rememberAsyncImagePainter(model = Uri.parse(restaurant.imageUri)), contentDescription = null, modifier = Modifier.height(150.dp).fillMaxWidth(), contentScale = ContentScale.Crop)
                } else {
                    val img = if (restaurant.imageRes != 0) restaurant.imageRes else android.R.drawable.ic_menu_gallery
                    Image(painter = painterResource(id = img), contentDescription = null, modifier = Modifier.height(150.dp).fillMaxWidth(), contentScale = ContentScale.Crop)
                }
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Unfavorite",
                    tint = Color.Red,
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).clickable { onFavoriteClick() }
                )
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(restaurant.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Rating: ${restaurant.rating}", color = Color.Gray)
            }
        }
    }
}