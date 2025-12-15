package com.example.talabat

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.*
import org.junit.Rule
import org.junit.Test

class LoginUITest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun login_screen_displays_correctly() {
        composeTestRule.onNodeWithText("Talabat Login").assertExists()
        composeTestRule.onNodeWithText("Login").assertExists()
    }
}