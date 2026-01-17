package com.example.maiplan.utils

import android.content.Context
import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import java.util.Date
import androidx.core.content.edit
import com.example.maiplan.network.api.UserResponse
import com.example.maiplan.utils.common.UserSession

class SessionManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    companion object {
        private const val ACCESS_TOKEN = "access_token"
        private const val USER_ID = "user_id"
        private const val USERNAME = "username"
        private const val EMAIL = "email"
    }

    fun saveSession(token: String, user: UserResponse) {
        UserSession.setup(user)
        sharedPreferences.edit {
            putString(ACCESS_TOKEN, token)
            putInt(USER_ID, user.id)
            putString(USERNAME, user.username)
            putString(EMAIL, user.email)
        }
    }

    fun getToken(): String? {
        val token = sharedPreferences.getString(ACCESS_TOKEN, null)
        if (isTokenExpired(token)) {
            clear()
            return null
        }
        return "Bearer $token"
    }

    fun getUserInfo(): UserResponse? {
        val userId = sharedPreferences.getInt(USER_ID, -1).takeIf { it != -1 }
        val username = sharedPreferences.getString(USERNAME, null)
        val email = sharedPreferences.getString(EMAIL, null)
        if (userId == null || username == null || email == null) return null
        return UserResponse(userId, username, email)
    }

    fun clearAll() {
        UserSession.clear()
        clear()
    }

    fun clear() {
        sharedPreferences.edit { clear() }
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