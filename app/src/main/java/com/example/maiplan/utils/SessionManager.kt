package com.example.maiplan.utils

import android.content.Context
import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import java.util.Date

class SessionManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    fun saveAuthToken(token: String) {
        sharedPreferences.edit().putString("access_token", token).apply()
    }

    fun getAuthToken(): String? {
        val token = sharedPreferences.getString("access_token", null)
        if (isTokenExpired(token)) {
            clearAuthToken()
            return null
        }
        return token
    }

    private fun clearAuthToken() {
        sharedPreferences.edit().remove("access_token").apply()
    }

    private fun isTokenExpired(token: String?): Boolean {
        if (token == null) return true

        try {
            val jwt: DecodedJWT = JWT.decode(token)
            val expirationDate: Date? = jwt.expiresAt
            return expirationDate?.before(Date()) ?: true
        } catch (e: Exception) {
            e.printStackTrace()
            return true
        }
    }
}