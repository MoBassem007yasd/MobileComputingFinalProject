package com.example.talabat
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = AppDatabase.getDatabase(this)
        val dao = db.appDao()
        val session = SessionManager(this)

        lifecycleScope.launch(Dispatchers.IO) {
            if (dao.getUser("admin") == null) {
                dao.insertUser(User("admin", "123", isAdmin = true))
            }
            if (dao.getRestaurantCount() == 0) {
                seedDatabase(dao)
            }
        }
        lifecycleScope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(1000)
                val currentTime = System.currentTimeMillis()
                val drivers = dao.getDriversSync()
                val orders = dao.getAllOrdersSync()
                // .filter, .forEach, .find process the data in RAM
                val busyDrivers = drivers.filter { it.isBusy }
                busyDrivers.forEach { driver ->
                    val lastOrder = orders.find { it.driverName == driver.name }

                    if (lastOrder != null) {
                        val timeElapsed = currentTime - lastOrder.orderTime
                        if (timeElapsed > 60000) {
                            dao.updateDriver(driver.copy(isBusy = false))
                        }
                    }
                    // If an order was deleted (because of deletion of its restaurant) from the database but the driver remained stuck as "Busy."
                    else {
                        dao.updateDriver(driver.copy(isBusy = false))
                    }
                }
            }
        }

        setContent {
            var isLoggedIn by remember { mutableStateOf(session.isLoggedIn()) }
            var isAdmin by remember { mutableStateOf(session.isAdmin()) }

            if (!isLoggedIn) {
                LoginScreen(dao) { email, adminStatus ->
                    session.saveLoginSession(email, adminStatus)
                    isLoggedIn = true
                    isAdmin = adminStatus
                }
            } else {
                if (isAdmin) {
                    AdminDashboard(dao) {
                        session.logout()
                        isLoggedIn = false
                    }
                } else {
                    MainAppLayout(dao) {
                        session.logout()
                        isLoggedIn = false
                    }
                }
            }
        }
    }

    private suspend fun seedDatabase(dao: AppDao) {
        val r1Id = dao.insertRestaurant(Restaurant(
            name = "La Poire", rating = 4.9, deliveryTime = "20 mins", deliveryFee = 25.0,
            imageRes = R.drawable.la_poire, category = "Dessert"
        )).toInt()

        if (r1Id != -1) {
            dao.insertMenuItem(MenuItem(
                restaurantId = r1Id, name = "White Forest Gateaux",
                description = "Layers of white sponge cake filled with fresh cream and cherry jam, covered with white chocolate and cherry",
                price = 80.00, category = "Gateaux", imageRes = R.drawable.gateaux1
            ))
            dao.insertMenuItem(MenuItem(
                restaurantId = r1Id, name = "Black Forest",
                description = "Layers of chocolate sponge cake filled with fresh cream and cherry bites topped with grated chocolate and cherry",
                price = 85.00, category = "Gateaux", imageRes = R.drawable.gateaux2
            ))
            dao.insertMenuItem(MenuItem(
                restaurantId = r1Id, name = "Cigarette Gateaux",
                description = "Layers of fragile chocolate sponge cake filled with rich chocolate cream topped with grated chocolate and powdered sugar",
                price = 80.00, category = "Gateaux", imageRes = R.drawable.gateaux3
            ))
            dao.insertMenuItem(MenuItem(
                restaurantId = r1Id, name = "Jam Millefeuille",
                description = "Layers of La Poire’s famous Millefeuille flakes with apricot jam",
                price = 85.00, category = "Gateaux", imageRes = R.drawable.gateaux4
            ))
            dao.insertMenuItem(MenuItem(
                restaurantId = r1Id, name = "Chocolate Mille Feuille",
                description = "Layers of La Poire’s famous Millefeuille, filled and covered with the delicious millefeuille chocolate cream",
                price = 85.00, category = "Gateaux", imageRes = R.drawable.gateaux5
            ))
            dao.insertMenuItem(MenuItem(
                restaurantId = r1Id, name = "Welly Gateaux",
                description = "Two layers of chocolate sponge cake, stuffed with caramel cream, crushed sable & Nutella, covered with chocolate",
                price = 70.00, category = "Gateaux", imageRes = R.drawable.gateaux6
            ))
            dao.insertMenuItem(MenuItem(
                restaurantId = r1Id, name = "Chocolate Eclair",
                description = "La Poire's famous Choux pastry filled with chocolate cream and covered with chocolate",
                price = 80.00, category = "Gateaux", imageRes = R.drawable.gateaux7
            ))
            dao.insertMenuItem(MenuItem(
                restaurantId = r1Id, name = "Walnut Tart",
                description = "Vanilla sable, caramel and white chocolate with walnut",
                price = 105.00, category = "Gateaux", imageRes = R.drawable.gateaux8
            ))
            dao.insertMenuItem(MenuItem(
                restaurantId = r1Id, name = "White Chocolate Torte",
                description = "Vanilla sponge cake filled with white forest cream, topped with grated white chocolate and meringue. serve from 3 to 4 person",
                price = 495.00, category = "Torte", imageRes = R.drawable.torte1
            ))
            dao.insertMenuItem(MenuItem(
                restaurantId = r1Id, name = "Mini Choco Lover Torte",
                description = "Chocolate sponge with layers of chocolate cream. Serve from 3 to 4 person",
                price = 385.00, category = "Torte", imageRes = R.drawable.torte2
            ))
            dao.insertMenuItem(MenuItem(
                restaurantId = r1Id, name = "Mini Black Forest Torte",
                description = "Layers of chocolate sponge fluffy cake filled with fresh cream and cherry bites, topped with grated chocolate and cherry. serve from 3 to 4 person",
                price = 440.00, category = "Torte", imageRes = R.drawable.torte3
            ))
            dao.insertMenuItem(MenuItem(
                restaurantId = r1Id, name = "La Poirette Torte",
                description = "1/2 chocolate: 3 layers of chocolate sponge cake filled with rich chocolate cream. 1/2 vanilla: 3 layers of white sponge cake filled with la Poire's special cream and topped with pieces of pineapple and peach. Serve from 10 to 12 person",
                price = 910.00, category = "Torte", imageRes = R.drawable.torte4
            ))
            dao.insertMenuItem(MenuItem(
                restaurantId = r1Id, name = "Choco Lover Torte",
                description = "Chocolate sponge layered with chocolate cream. Serves 10 to 12 people.",
                price = 745.00, category = "Torte", imageRes = R.drawable.torte5
            ))
            dao.insertMenuItem(MenuItem(
                restaurantId = r1Id, name = "Chocolate Amido Torte",
                description = "2 layers of chocolate fudge cake , filled with Nutella chocolate cream, chocolate and crispy, topped with a special cream layer from La Poire decorated with chocolate glaze and crispy. Serve from 8 to 10 person",
                price = 765.00, category = "Torte", imageRes = R.drawable.torte6
            ))
            dao.insertMenuItem(MenuItem(
                restaurantId = r1Id, name = "Black Forest Torte",
                description = "Layers of fragile chocolate sponge cake stuffed with fresh cream and cherry pieces, decorated with grated chocolate and cherries. Serve from 10 to 12 person",
                price = 875.00, category = "Torte", imageRes = R.drawable.torte7
            ))
            dao.insertMenuItem(MenuItem(
                restaurantId = r1Id, name = "Marble Cake Mold",
                description = "Marble cake mold with chocolate",
                price = 245.00, category = "Cakes", imageRes = R.drawable.cake1
            ))
            dao.insertMenuItem(MenuItem(
                restaurantId = r1Id, name = "Almond Cake",
                description = "White cake with almonds and apricot jam covered with sliced almonds and powdered sugar.",
                price = 300.00, category = "Cakes", imageRes = R.drawable.cake2
            ))
            dao.insertMenuItem(MenuItem(
                restaurantId = r1Id, name = "Christmas Cake",
                description = "English cake with candied fruits and walnut.",
                price = 435.00, category = "Cakes", imageRes = R.drawable.cake3
            ))
        }
        val r2Id = dao.insertRestaurant(Restaurant(
            name = "Foul w Falafel",
            rating = 4.5,
            deliveryTime = "30 mins",
            deliveryFee = 15.0,
            imageRes = R.drawable.oriental_logo,
            category = "Oriental"
        )).toInt()

        if (r2Id != -1) {
            dao.insertMenuItem(MenuItem(
                restaurantId = r2Id,
                name = "Flaxseed Oil Foul Sandwich",
                description = "Fresh bread stuffed with yellow fava foul with flaxseed oil to add a delicious spicy touch.",
                price = 15.00,
                category = "Foul",
                imageRes = R.drawable.alexfsandwich
            ))
            dao.insertMenuItem(MenuItem(
                restaurantId = r2Id,
                name = "Alexandrian Foul Sandwich",
                description = "Crispy bread stuffed with yellow fava foul, hot tahini sauce, fresh vegetables and pickles, served in the famous Alexandrian style.",
                price = 15.00,
                category = "Foul",
                imageRes = R.drawable.flaxseed
            ))
            dao.insertMenuItem(MenuItem(
                restaurantId = r2Id,
                name = "Foul with Boiled Eggs Sandwich",
                description = "Fresh bread stuffed with yellow fava foul with a chopped boiled egg to add protein and a rich texture.",
                price = 20.00,
                category = "Foul",
                imageRes = R.drawable.boiledeggs
            ))
            dao.insertMenuItem(MenuItem(
                restaurantId = r2Id,
                name = "Taamia with Salad sandwich",
                description = "Fresh bread stuffed with fried Taamia with fresh green salad (lettuce, tomato, cucumber, onion) to add a refreshing flavour.",
                price = 13.50,
                category = "Taamia",
                imageRes = R.drawable.tam
            ))
            dao.insertMenuItem(MenuItem(
                restaurantId = r2Id,
                name = "Taamia With Eggplant Sandwich",
                description = "Stuffed bread, falafel, and fresh fried eggplant",
                price = 15.00,
                category = "Taamia",
                imageRes = R.drawable.taamia_eggplant
            ))
            dao.insertMenuItem(MenuItem(
                restaurantId = r2Id,
                name = "Taamia with Eggs Sandwich",
                description = "Fresh bread stuffed with fried Taamia with a boiled or fried egg to add protein and a rich texture.",
                price = 15.00,
                category = "Taamia",
                imageRes = R.drawable.ta_eg
            ))
        }
        val r3Id = dao.insertRestaurant(Restaurant(
            name = "Apache Chicken",
            rating = 4.2,
            deliveryTime = "40 mins",
            deliveryFee = 20.0,
            imageRes = R.drawable.apache,
            category = "Chicken"
        )).toInt()

        if (r3Id != -1) {
            dao.insertMenuItem(MenuItem(
                restaurantId = r3Id,
                name = "Blue Chicken Sandwich",
                description = "Crispy chicken fillet, lettuce, barbecue sauce, caramelized onions, tomatoes, smoked turkey, pickled cucumbers, cheddar cheese sauce and mayonnaise, served in apache bread.",
                price = 115.00,
                category = "Sandwiches",
                imageRes = R.drawable.cherokoko
            ))
            dao.insertMenuItem(MenuItem(
                restaurantId = r3Id,
                name = "Kuri Fire Chicken Sandwich",
                description = "Crispy chicken fillet, lettuce, Jalapeno , tomatoes,  cheddar cheese sauce and Kuri Spicy Sauce , served in Apache bread.",
                price = 105.00,
                category = "Sandwiches",
                imageRes = R.drawable.kuri
            ))
            dao.insertMenuItem(MenuItem(
                restaurantId = r3Id,
                name = "Apache Chicken Sandwich",
                description = "Crispy chicken fillet, lettuce, red onions, tomatoes, cheddar cheese sauce and mayonnaise, served in apache bread.",
                price = 95.00,
                category = "Sandwiches",
                imageRes = R.drawable.old_spirit
            ))
        }
        dao.insertDriver(Driver(name = "Ahmed (Driver)", isBusy = false))
        dao.insertDriver(Driver(name = "Mohamed (Driver)", isBusy = false))
    }
}
@Composable
fun LoginScreen(dao: AppDao, onLoginSuccess: (String, Boolean) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegistering by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(if (isRegistering) "Register" else "Talabat Login", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF5722))
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Username/Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                scope.launch {
                    if (email.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    if (isRegistering) {
                        val existingUser = dao.getUser(email)
                        if (existingUser != null) {
                            Toast.makeText(context, "User already exists!", Toast.LENGTH_SHORT).show()
                        } else {
                            dao.insertUser(User(email, password, false))
                            Toast.makeText(context, "User Created! Please Login.", Toast.LENGTH_SHORT).show()
                            isRegistering = false
                        }
                    } else {
                        val user = dao.getUser(email)
                        if (user != null && user.password == password) {
                            onLoginSuccess(user.email, user.isAdmin)
                        } else {
                            Toast.makeText(context, "Invalid Credentials", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))
        ) {
            Text(if (isRegistering) "Sign Up" else "Login")
        }
        TextButton(onClick = { isRegistering = !isRegistering }) {
            Text(if (isRegistering) "Back to Login" else "Create New Account")
        }
    }
}