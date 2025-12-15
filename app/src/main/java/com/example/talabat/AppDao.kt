package com.example.talabat

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM restaurants")
    fun getAllRestaurants(): Flow<List<Restaurant>>

    @Query("SELECT COUNT(*) FROM restaurants WHERE name = :name")
    suspend fun checkRestaurantNameExists(name: String): Int

    @Query("SELECT DISTINCT category FROM restaurants")
    fun getRestaurantCategories(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM restaurants")
    suspend fun getRestaurantCount(): Int

    @Update
    suspend fun updateRestaurant(restaurant: Restaurant)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRestaurant(restaurant: Restaurant): Long

    @Delete
    suspend fun deleteRestaurant(restaurant: Restaurant)

    @Query("SELECT * FROM restaurants WHERE id = :restId")
    suspend fun getRestaurantById(restId: Int): Restaurant

    @Query("SELECT * FROM restaurants WHERE isFavorite = 1")
    fun getFavoriteRestaurants(): Flow<List<Restaurant>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserFavorite(userFavorite: UserFavorite)
    @Delete
    suspend fun removeUserFavorite(fav: UserFavorite)
    @Query("SELECT * FROM menu_items WHERE restaurantId = :restId")
    fun getMenuForRestaurant(restId: Int): Flow<List<MenuItem>>

    @Query("SELECT COUNT(*) FROM menu_items WHERE restaurantId = :restId AND name = :name")
    suspend fun checkMenuItemExists(restId: Int, name: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMenuItem(item: MenuItem)

    @Delete
    suspend fun deleteMenuItem(item: MenuItem)

    @Query("SELECT * FROM basket")
    fun getBasket(): Flow<List<BasketItem>>

    @Query("SELECT * FROM basket")
    suspend fun getBasketList(): List<BasketItem>

    @Query("SELECT * FROM basket WHERE menuItemId = :itemId LIMIT 1")
    suspend fun getBasketItemById(itemId: Int): BasketItem?

    @Insert
    suspend fun addToBasket(item: BasketItem)

    @Update
    suspend fun updateBasketItem(item: BasketItem)

    @Delete
    suspend fun deleteBasketItem(item: BasketItem)

    @Query("DELETE FROM basket")
    suspend fun clearBasket()

    @Insert
    suspend fun addOrderHistory(order: OrderHistory)

    @Query("SELECT * FROM order_history WHERE userEmail = :email GROUP BY restaurantId ORDER BY MAX(id) DESC LIMIT 5")
    fun getRecentRestaurantsForUser(email: String): Flow<List<OrderHistory>>

    @Query("SELECT * FROM order_history WHERE userEmail = :email ORDER BY orderTime DESC")
    fun getOrdersForUser(email: String): Flow<List<OrderHistory>>

    @Query("SELECT * FROM order_history ORDER BY orderTime DESC")
    fun getAllOrdersAdmin(): Flow<List<OrderHistory>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUser(email: String): User?

    @Insert
    suspend fun insertDriver(driver: Driver)

    @Delete
    suspend fun deleteDriver(driver: Driver)

    @Update
    suspend fun updateDriver(driver: Driver)

    @Query("SELECT * FROM drivers")
    fun getAllDrivers(): Flow<List<Driver>>

    @Query("SELECT COUNT(*) FROM drivers WHERE name = :name")
    suspend fun checkDriverNameExists(name: String): Int

    @Query("SELECT * FROM drivers WHERE isBusy = 0 LIMIT 1")
    suspend fun getAvailableDriver(): Driver?
    
    @Query("SELECT * FROM restaurants INNER JOIN user_favorites ON restaurants.id = user_favorites.restaurantId WHERE user_favorites.userEmail = :email")
    fun getFavoriteRestaurantsForUser(email: String): Flow<List<Restaurant>>

    @Query("SELECT * FROM drivers")
    suspend fun getDriversSync(): List<Driver>

    @Query("SELECT * FROM order_history ORDER BY orderTime DESC")
    suspend fun getAllOrdersSync(): List<OrderHistory>
}