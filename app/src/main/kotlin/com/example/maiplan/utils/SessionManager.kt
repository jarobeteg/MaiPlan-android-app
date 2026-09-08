package com.example.maiplan.utils

import android.content.Context
import androidx.core.content.edit
import java.util.UUID

class SessionManager(context: Context) {
    private val sharedPreferences = context.applicationContext.getSharedPreferences(
        "user_session",
        Context.MODE_PRIVATE
    )

    private companion object {
        const val ACCESS_TOKEN = "access_token"
        const val REFRESH_TOKEN = "refresh_token"
        const val ACTIVE_USER_SYNC_ID = "active_user_sync_id"
    }

    fun saveSession(
        accessToken: String,
        refreshToken: String?,
        userSyncId: UUID
    ) {
        require(accessToken.isNotBlank()) {
            "Cannot save a session with a blank access token"
        }

        sharedPreferences.edit {
            putString(ACCESS_TOKEN, accessToken)
            putString(REFRESH_TOKEN, refreshToken?.takeIf { it.isNotBlank() })
            putString(ACTIVE_USER_SYNC_ID, userSyncId.toString())
        }
    }

    fun getActiveUserSyncId(): UUID? {
        val value = sharedPreferences.getString(
            ACTIVE_USER_SYNC_ID,
            null
        ) ?: return null

        return runCatching {
            UUID.fromString(value)
        }.getOrNull()
    }

    fun getAccessToken(): String? {
        return sharedPreferences.getString(ACCESS_TOKEN, null)
    }

    fun getAuthorizationHeader(): String? {
        return getAccessToken()
            ?.takeIf { it.isNotBlank() }
            ?.let { "Bearer $it" }
    }

    fun getRefreshToken(): String? {
        return sharedPreferences.getString(REFRESH_TOKEN, null)
    }

    fun hasSession(): Boolean {
        return getActiveUserSyncId() != null &&
                !getAccessToken().isNullOrBlank()
    }

    fun clearSession() {
        sharedPreferences.edit {
            clear()
        }
    }
}
