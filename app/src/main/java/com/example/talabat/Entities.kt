package com.example.talabat

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
@Entity(tableName = "restaurants")
data class Restaurant(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val rating: Double,
    val deliveryTime: String,
    val deliveryFee: Double,
    val imageRes: Int,
    val imageUri: String? = null,
    val category: String = "Oriental",
    var isFavorite: Boolean = false
)

@Entity(
    tableName = "menu_items",
    foreignKeys = [
        ForeignKey(
            entity = Restaurant::class,
            parentColumns = ["id"],
            childColumns = ["restaurantId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MenuItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val restaurantId: Int,
    val name: String,
    val description: String,
    val price: Double,
    val imageRes: Int,
    val imageUri: String? = null,
    val category: String
)

@Entity(tableName = "basket")
data class BasketItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val restaurantId: Int,
    val menuItemId: Int,
    val name: String,
    val price: Double,
    val quantity: Int,
    val imageRes: Int,
    val imageUri: String? = null
)

@Entity(
    tableName = "order_history",
    foreignKeys = [
        ForeignKey(
            entity = Restaurant::class,
            parentColumns = ["id"],
            childColumns = ["restaurantId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class OrderHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val restaurantId: Int,
    val restaurantName: String,
    val userEmail: String,
    val driverName: String,
    val imageRes: Int,
    val imageUri: String? = null,
    val orderTime: Long = System.currentTimeMillis(),
    val totalPrice: Double,
    val itemsSummary: String
)

@Entity(tableName = "users")
data class User(
    @PrimaryKey val email: String,
    val password: String,
    val isAdmin: Boolean = false
)

@Entity(tableName = "drivers")
data class Driver(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val deliveriesCount: Int = 0,
    val isBusy: Boolean = false
)
@Entity(tableName = "user_favorites", primaryKeys = ["userEmail", "restaurantId"])
data class UserFavorite(
    val userEmail: String,
    val restaurantId: Int
)