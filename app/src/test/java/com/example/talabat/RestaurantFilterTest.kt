package com.example.talabat

import org.junit.Assert.assertEquals
import org.junit.Test

class RestaurantFilterTest {
    private val sampleRestaurants = listOf(
        Restaurant(name = "La Poire", category = "Dessert", rating = 4.9, deliveryTime = "20m", deliveryFee = 25.0, imageRes = 0),
        Restaurant(name = "Foul w Falafel", category = "Oriental", rating = 4.5, deliveryTime = "30m", deliveryFee = 15.0, imageRes = 0),
        Restaurant(name = "Apache Chicken", category = "Chicken", rating = 4.2, deliveryTime = "40m", deliveryFee = 20.0, imageRes = 0)
    )

    private val filterHelper = RestaurantFilter()

    @Test
    fun `filter returns only restaurants matching category`() {
        val result = filterHelper.filterRestaurants(sampleRestaurants) {
            it.category == "Dessert"
        }

        assertEquals(1, result.size)
        assertEquals("La Poire", result[0].name)
    }

    @Test
    fun `filter returns only restaurants matching search text`() {
        val result = filterHelper.filterRestaurants(sampleRestaurants) {
            it.name.contains("Chicken", ignoreCase = true)
        }

        assertEquals(1, result.size)
        assertEquals("Apache Chicken", result[0].name)
    }

    @Test
    fun `filter returns empty list when no match found`() {
        val result = filterHelper.filterRestaurants(sampleRestaurants) {
            it.name.contains("Pizza", ignoreCase = true)
        }

        assertEquals(0, result.size)
    }
}