package com.example.maiplan.utils

import android.content.Context
import com.example.maiplan.utils.security.TokenCipher
import java.time.Instant
import java.util.UUID
import androidx.core.content.edit

class SessionManager(context: Context) {
    private val sharedPreferences = context.applicationContext.getSharedPreferences(
        "user_session",
        Context.MODE_PRIVATE
    )
    private val tokenCipher = TokenCipher()

    private companion object {
        const val ACCESS_TOKEN = "access_token"
        const val REFRESH_TOKEN = "refresh_token"
        const val SESSION_ID = "session_id"
        const val ACTIVE_USER_SYNC_ID = "active_user_sync_id"
        const val ACCESS_TOKEN_EXPIRES_AT = "access_token_expires_at"
        const val REFRESH_TOKEN_EXPIRES_AT = "refresh_token_expires_at"
        const val ENCRYPTED_TOKEN_PREFIX = "v1:"
    }

    fun saveSession(
        accessToken: String,
        refreshToken: String,
        sessionId: String,
        userSyncId: UUID,
        accessTokenExpiresAt: String,
        refreshTokenExpiresAt: String
    ) {
        require(accessToken.isNotBlank()) {
            "Cannot save a session with a blank access token"
        }
        require(refreshToken.isNotBlank()) {
            "Cannot save a session with a blank refresh token"
        }

        val parsedSessionId = UUID.fromString(sessionId)
        val parsedAccessTokenExpiry = Instant.parse(accessTokenExpiresAt)
        val parsedRefreshTokenExpiry = Instant.parse(refreshTokenExpiresAt)
        val now = Instant.now()

        require(parsedAccessTokenExpiry.isAfter(now)) {
            "The server returned an already-expired access token"
        }
        require(parsedRefreshTokenExpiry.isAfter(now)) {
            "The server returned an already-expired refresh token"
        }

        require(parsedRefreshTokenExpiry.isAfter(parsedAccessTokenExpiry)) {
            "Refresh token must expire after the access token"
        }

        val encryptedAccessToken = tokenCipher.encrypt(accessToken)
        val encryptedRefreshToken = tokenCipher.encrypt(refreshToken)

        val wasSaved = sharedPreferences.edit().run {
            putString(ACCESS_TOKEN, encryptedAccessToken)
            putString(REFRESH_TOKEN, encryptedRefreshToken)
            putString(SESSION_ID, parsedSessionId.toString())
            putString(ACTIVE_USER_SYNC_ID, userSyncId.toString())
            putLong(ACCESS_TOKEN_EXPIRES_AT, parsedAccessTokenExpiry.toEpochMilli())
            putLong(REFRESH_TOKEN_EXPIRES_AT, parsedRefreshTokenExpiry.toEpochMilli())
            commit()
        }

        check(wasSaved) {
            "Could not persist the authenticated session"
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
        return readToken(ACCESS_TOKEN)
    }

    fun getAuthorizationHeader(): String? {
        return getAccessToken()
            ?.takeIf { it.isNotBlank() }
            ?.let { "Bearer $it" }
    }

    fun getRefreshToken(): String? {
        return readToken(REFRESH_TOKEN)
    }

    fun getSessionId(): UUID? {
        val value = sharedPreferences.getString(SESSION_ID, null) ?: return null

        return runCatching {
            UUID.fromString(value)
        }.getOrNull()
    }

    fun hasUsableAccessToken(now: Instant = Instant.now()): Boolean {
        val expiry = getStoredInstant(ACCESS_TOKEN_EXPIRES_AT) ?: return false

        return !getAccessToken().isNullOrBlank() && expiry.isAfter(now)
    }

    fun hasSession(): Boolean {
        val refreshExpiry = getStoredInstant(REFRESH_TOKEN_EXPIRES_AT) ?: return false

        return getActiveUserSyncId() != null &&
                getSessionId() != null &&
                !getRefreshToken().isNullOrBlank() &&
                refreshExpiry.isAfter(Instant.now())
    }

    fun clearSession() {
        sharedPreferences.edit { clear() }
    }

    private fun getStoredInstant(key: String): Instant? {
        if (!sharedPreferences.contains(key)) {
            return null
        }

        return runCatching {
            Instant.ofEpochMilli(sharedPreferences.getLong(key, 0L))
        }.getOrNull()
    }

    private fun readToken(key: String): String? {
        val storedValue = sharedPreferences.getString(key, null) ?: return null

        if (!storedValue.startsWith(ENCRYPTED_TOKEN_PREFIX)) {
            return storedValue
        }

        return runCatching {
            tokenCipher.decrypt(storedValue)
        }.getOrNull()
    }
}
