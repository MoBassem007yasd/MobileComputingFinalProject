package com.example.talabat

import android.content.Context
import android.widget.Toast
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Database(entities = [Restaurant::class, MenuItem::class, BasketItem::class, OrderHistory::class, User::class, Driver::class, UserFavorite::class], version = 6, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "talabat_db"
                )
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

suspend fun updateBasketQuantity(
    context: Context,
    dao: AppDao,
    menuItem: MenuItem,
    change: Int
) {
    val currentBasket = dao.getBasketList()
    if (currentBasket.isNotEmpty() && currentBasket.first().restaurantId != menuItem.restaurantId) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Clear basket to order from a new restaurant.", Toast.LENGTH_SHORT).show()
        }
        return
    }
    val existingItem = dao.getBasketItemById(menuItem.id)
    if (existingItem == null) {
        if (change > 0) {
            dao.addToBasket(BasketItem(
                restaurantId = menuItem.restaurantId,
                menuItemId = menuItem.id,
                name = menuItem.name,
                price = menuItem.price,
                quantity = change,
                imageRes = menuItem.imageRes,
                imageUri = menuItem.imageUri
            ))
        }
    } else {
        val newQuantity = existingItem.quantity + change
        if (newQuantity > 0) {
            dao.updateBasketItem(existingItem.copy(quantity = newQuantity))
        } else {
            dao.deleteBasketItem(existingItem)
        }
    }
}