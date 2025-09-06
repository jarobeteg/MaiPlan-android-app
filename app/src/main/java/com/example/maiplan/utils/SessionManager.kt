package com.example.maiplan.utils

import android.content.Context
import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import java.util.Date
import androidx.core.content.edit

class SessionManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    /**
     * Saves the authentication [token] into shared preferences.
     *
     * @param token The JWT access token to be stored.
     */
    fun saveAuthToken(token: String) {
        sharedPreferences.edit { putString("access_token", token) }
    }

    /**
     * Retrieves the saved authentication token if it exists and is not expired.
     *
     * @return The token prefixed with `Bearer`, or null if no valid token exists.
     *
     * @see clearAuthToken
     * @see isTokenExpired
     */
    fun getAuthToken(): String? {
        val token = sharedPreferences.getString("access_token", null)
        if (isTokenExpired(token)) {
            clearAuthToken()
            return null
        }
        return "Bearer $token"
    }

    /**
     * Clears the saved authentication token from `SharedPreferences`.
     */
    private fun clearAuthToken() {
        sharedPreferences.edit { remove("access_token") }
    }

    /**
     * Checks if the given [token] has expired.
     *
     * @param token The JWT token string.
     * @return true if the token is expired or invalid, false otherwise.
     */
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