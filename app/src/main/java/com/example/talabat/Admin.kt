package com.example.talabat

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.toString
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AdminDashboard(dao: AppDao, onLogout: () -> Unit) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Admin Panel", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                TextButton(onClick = onLogout) { Text("Logout", color = Color.Red) }
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = selectedTab == 0, onClick = { selectedTab = 0 }, icon = { Icon(Icons.Default.Home, "Restos") }, label = { Text("Restaurants") })
                NavigationBarItem(selected = selectedTab == 1, onClick = { selectedTab = 1 }, icon = { Icon(Icons.Default.Person, "Drivers") }, label = { Text("Drivers") })
                NavigationBarItem(selected = selectedTab == 2, onClick = { selectedTab = 2 }, icon = { Icon(Icons.Default.List, "All Orders") }, label = { Text("Orders") })
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            when(selectedTab) {
                0 -> AdminRestaurantsScreen(dao)
                1 -> AdminDriversScreen(dao)
                2 -> AdminAllOrdersScreen(dao)
            }
        }
    }
}

@Composable
fun AdminAllOrdersScreen(dao: AppDao) {
    val orders by dao.getAllOrdersAdmin().collectAsState(initial = emptyList())
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while(true) {
            delay(1000)
            currentTime = System.currentTimeMillis()
        }
    }

    LazyColumn(Modifier.padding(16.dp)) {
        item { Text("All User Orders & Drivers", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp)) }
        items(orders) { order ->
            val isDelivered = (currentTime - order.orderTime) > 90000
            val statusText = if (isDelivered) "Delivered" else "Preparing..."
            val statusColor = if (isDelivered) Color(0xFF4CAF50) else Color(0xFFFFA000)

            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Order #${order.id}", fontWeight = FontWeight.Bold)
                    Text("User: ${order.userEmail}")
                    Text("Restaurant: ${order.restaurantName}")
                    Text("Assigned Driver: ${order.driverName}", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Status: ", fontWeight = FontWeight.Bold)
                        Text(statusText, color = statusColor, fontWeight = FontWeight.Bold)
                    }
                    Text("Total: ${order.totalPrice} EGP")
                }
            }
        }
    }
}

@Composable
fun AdminDriversScreen(dao: AppDao) {
    val drivers by dao.getAllDrivers().collectAsState(initial = emptyList())
    var name by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(Modifier.padding(16.dp)) {
        Text("Manage Drivers", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Driver Name") }, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                scope.launch {
                    if (name.isBlank()) {
                        withContext(Dispatchers.Main) {
                        }
                        return@launch
                    }
                    val exists = dao.checkDriverNameExists(name)
                    if (exists > 0) {
                        return@launch
                    }
                    withContext(Dispatchers.IO) {
                        val newDriver = Driver(name = name, deliveriesCount = 0, isBusy = false)
                        dao.insertDriver(newDriver)
                        try {
                            syncDriverToFirebase(newDriver)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    name = ""
                }
            }) { Text("Add") }
        }
        Spacer(Modifier.height(16.dp))
        LazyColumn {
            items(drivers) { driver ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).background(Color.LightGray.copy(alpha=0.2f), RoundedCornerShape(8.dp)).padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(driver.name, fontWeight = FontWeight.Bold)
                        Text("Deliveries: ${driver.deliveriesCount}")
                        Text(if(driver.isBusy) "Busy" else "Available", color = if(driver.isBusy) Color.Red else Color.Green)
                    }
                    Row {

                        IconButton(onClick = { scope.launch { dao.deleteDriver(driver) } }) {
                            Icon(Icons.Default.Delete, "Delete", tint = Color.Red)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminRestaurantsScreen(dao: AppDao) {
    val restaurants by dao.getAllRestaurants().collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var restaurantToEdit by remember { mutableStateOf<Restaurant?>(null) }
    var restaurantToManageMenu by remember { mutableStateOf<Restaurant?>(null) }
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Button(onClick = { showAddDialog = true }, modifier = Modifier.fillMaxWidth()) {
            Text("Add New Restaurant")
        }
        LazyColumn {
            items(restaurants) { res ->
                Card(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text(res.name, fontWeight = FontWeight.Bold)
                            Row {
                                IconButton(onClick = { restaurantToEdit = res }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                                }
                                IconButton(onClick = { scope.launch { dao.deleteRestaurant(res) } }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                }
                            }
                        }
                        Text("Category: ${res.category} | Fee: ${res.deliveryFee} | Time: ${res.deliveryTime}", fontSize = 12.sp, color = Color.Gray)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { restaurantToManageMenu = res }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) {
                            Text("Manage Menu Items")
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        RestaurantDialog(dao, null) { showAddDialog = false }
    }
    if (restaurantToEdit != null) {
        RestaurantDialog(dao, restaurantToEdit) { restaurantToEdit = null }
    }
    if (restaurantToManageMenu != null) {
        ManageMenuDialog(dao, restaurantToManageMenu!!) { restaurantToManageMenu = null }
    }
}
@Composable
fun RestaurantDialog(dao: AppDao, restaurantToEdit: Restaurant?, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(restaurantToEdit?.name ?: "") }
    var category by remember { mutableStateOf(restaurantToEdit?.category ?: "") }
    var deliveryFee by remember { mutableStateOf(restaurantToEdit?.deliveryFee?.toString() ?: "") }
    var deliveryTime by remember { mutableStateOf(restaurantToEdit?.deliveryTime ?: "") }
    var imageUri by remember { mutableStateOf<Uri?>(restaurantToEdit?.imageUri?.let { Uri.parse(it) }) }
    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? -> imageUri = uri }
    val existingCategories by dao.getRestaurantCategories().collectAsState(initial = emptyList())
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (restaurantToEdit == null) "Add Restaurant" else "Edit Restaurant") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                OutlinedTextField(value = deliveryFee, onValueChange = { deliveryFee = it }, label = { Text("Fee (EGP)") })
                OutlinedTextField(value = deliveryTime, onValueChange = { deliveryTime = it }, label = { Text("Time (e.g. 30 mins)") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (e.g. Oriental)") },
                    placeholder = { Text("Type new or select below") }
                )
                if (existingCategories.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text("Select existing:", fontSize = 12.sp, color = Color.Gray)
                    LazyRow {
                        items(existingCategories) { cat ->
                            if(cat.isNotBlank()) {
                                FilterChip(
                                    selected = category == cat,
                                    onClick = { category = cat },
                                    label = { Text(cat) },
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = { launcher.launch("image/*") }) {
                    Text(if (imageUri == null) "Upload Image (Local)" else "Image Selected")
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                scope.launch {
                    val fee = deliveryFee.toDoubleOrNull() ?: 0.0
                    if (restaurantToEdit == null) {
                        dao.insertRestaurant(Restaurant(
                            name = name, category = category,
                            imageRes = android.R.drawable.ic_menu_gallery,
                            imageUri = imageUri.toString(), rating = 4.5,
                            deliveryTime = deliveryTime, deliveryFee = fee
                        ))
                    } else {
                        dao.updateRestaurant(restaurantToEdit.copy(
                            name = name, category = category,
                            imageUri = imageUri?.toString() ?: restaurantToEdit.imageUri,
                            deliveryTime = deliveryTime, deliveryFee = fee
                        ))
                    }
                    onDismiss()
                }
            }) { Text("Save") }
        }
    )
}

@Composable
fun ManageMenuDialog(dao: AppDao, restaurant: Restaurant, onDismiss: () -> Unit) {
    val menuItems by dao.getMenuForRestaurant(restaurant.id).collectAsState(initial = emptyList())

    var itemName by remember { mutableStateOf("") }
    var itemPrice by remember { mutableStateOf("") }
    var itemDesc by remember { mutableStateOf("") }

    var itemCategory by remember { mutableStateOf("") }
    var itemUri by remember { mutableStateOf<Uri?>(null) }

    val existingCategories = remember(menuItems) {
        menuItems.map { it.category }.distinct().filter { it.isNotEmpty() }
    }

    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? -> itemUri = uri }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage Menu: ${restaurant.name}") },
        text = {
            Column(modifier = Modifier.fillMaxHeight(0.8f).verticalScroll(rememberScrollState())) {

                Text("Add New Item", fontWeight = FontWeight.Bold, color = Color(0xFFFF5722))

                OutlinedTextField(value = itemName, onValueChange = { itemName = it }, label = { Text("Item Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = itemDesc, onValueChange = { itemDesc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = itemPrice, onValueChange = { itemPrice = it }, label = { Text("Price") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = itemCategory,
                    onValueChange = { itemCategory = it },
                    label = { Text("Category (e.g. Gateaux)") },
                    placeholder = { Text("Type new or select below") },
                    modifier = Modifier.fillMaxWidth()
                )
                if (existingCategories.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text("Select from existing:", fontSize = 12.sp, color = Color.Gray)
                    LazyRow(modifier = Modifier.padding(vertical = 8.dp)) {
                        items(existingCategories) { cat ->
                            FilterChip(
                                selected = itemCategory == cat,
                                onClick = { itemCategory = cat },
                                label = { Text(cat) },
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = { launcher.launch("image/*") }) {
                    Text(if (itemUri == null) "Upload Item Image" else "Image Selected")
                }

                Button(
                    onClick = {
                        scope.launch {
                            val price = itemPrice.toDoubleOrNull() ?: 0.0
                            if (itemName.isNotEmpty()) {
                                val exists = dao.checkMenuItemExists(restaurant.id, itemName)
                                if (exists > 0) {
                                    return@launch
                                }
                                val finalCategory = if (itemCategory.isNotBlank()) itemCategory else "General"

                                dao.insertMenuItem(MenuItem(
                                    restaurantId = restaurant.id,
                                    name = itemName,
                                    description = itemDesc,
                                    price = price,
                                    category = finalCategory,
                                    imageRes = android.R.drawable.ic_menu_gallery,
                                    imageUri = itemUri?.toString()
                                ))
                                itemName = ""; itemPrice = ""; itemDesc = ""; itemCategory = ""; itemUri = null
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Add Item") }

                HorizontalDivider(Modifier.padding(vertical = 16.dp))

                Text("Existing Items", fontWeight = FontWeight.Bold)
                menuItems.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.name, fontWeight = FontWeight.Bold)
                            Text("${item.category} • ${item.price} EGP", fontSize = 12.sp, color = Color.Gray)
                        }
                        IconButton(onClick = { scope.launch { dao.deleteMenuItem(item) } }) {
                            Icon(Icons.Default.Delete, "Delete Item", tint = Color.Red)
                        }
                    }
                    HorizontalDivider(color = Color.LightGray)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}