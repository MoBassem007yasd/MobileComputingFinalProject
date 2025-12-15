package com.example.talabat

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    companion object {
        const val KEY_EMAIL = "email"
        const val KEY_IS_ADMIN = "is_admin"
        const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    fun saveLoginSession(email: String, isAdmin: Boolean) {
        val editor = prefs.edit()
        editor.putString(KEY_EMAIL, email)
        editor.putBoolean(KEY_IS_ADMIN, isAdmin)
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.apply()
    }

    fun getUserEmail(): String? {
        return prefs.getString(KEY_EMAIL, null)
    }

    fun isAdmin(): Boolean {
        return prefs.getBoolean(KEY_IS_ADMIN, false)
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun logout() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}