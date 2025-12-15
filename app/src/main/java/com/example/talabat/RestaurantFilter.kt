package com.example.talabat

class RestaurantFilter {
    fun filterRestaurants(
        list: List<Restaurant>,
        predicate: (Restaurant) -> Boolean
    ): List<Restaurant> {
        return list.filter(predicate)
    }
}