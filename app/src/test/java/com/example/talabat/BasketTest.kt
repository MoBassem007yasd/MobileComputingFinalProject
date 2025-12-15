package com.example.talabat

import org.junit.Assert.assertEquals
import org.junit.Test

class BasketTest {

    @Test
    fun `calculate total price sums items correctly`() {
        val basketItems = listOf(
            BasketItem(restaurantId = 1, menuItemId = 101, name = "Cake", price = 50.0, quantity = 2, imageRes = 0, imageUri = null),
            BasketItem(restaurantId = 1, menuItemId = 102, name = "Coffee", price = 25.0, quantity = 1, imageRes = 0, imageUri = null)
        )
        val deliveryFee = 25.0
        val subtotal = basketItems.sumOf { it.price * it.quantity }
        val total = subtotal + deliveryFee

        assertEquals(125.0, subtotal, 0.0)
        assertEquals(150.0, total, 0.0)
    }
}