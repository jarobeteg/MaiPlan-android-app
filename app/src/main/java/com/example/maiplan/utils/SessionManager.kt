package com.example.maiplan.utils

import android.content.Context

class SessionManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    fun saveAuthToken(token: String) {
        sharedPreferences.edit().putString("access_token", token).apply()
    }

    fun getAuthToken(): String? {
        return sharedPreferences.getString("access_token", null)
    }

    fun clearAuthToken() {
        sharedPreferences.edit().remove("access_token").apply()
    }
}