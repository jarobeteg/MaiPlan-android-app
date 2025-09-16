package com.example.maiplan.utils

import android.content.Context
import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import java.util.Date
import androidx.core.content.edit

class SessionManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        sharedPreferences.edit { putString("access_token", token) }
    }

    fun getToken(): String? {
        val token = sharedPreferences.getString("access_token", null)
        if (isTokenExpired(token)) {
            clearToken()
            return null
        }
        return "Bearer $token"
    }

    fun clearToken() {
        sharedPreferences.edit { remove("access_token") }
    }

    private fun isTokenExpired(token: String?): Boolean {
        if (token == null) return true

        try {
            val jwt: DecodedJWT = JWT.decode(token)
            val expirationDate: Date? = jwt.expiresAt
            return expirationDate?.before(Date()) != false
        } catch (e: Exception) {
            e.printStackTrace()
            return true
        }
    }
}