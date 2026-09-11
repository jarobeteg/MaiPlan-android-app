package com.example.maiplan.network

import com.example.maiplan.network.api.RefreshTokenRequest
import com.example.maiplan.network.api.TokenRefreshApi
import com.example.maiplan.utils.AppVisibilityTracker
import com.example.maiplan.utils.SessionManager
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import java.io.IOException
import java.util.UUID

class ForegroundTokenAuthenticator(
    private val sessionManager: SessionManager,
    private val refreshApi: TokenRefreshApi,
    private val deviceId: UUID
) : Authenticator {
    private val refreshLock = Any()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (!AppVisibilityTracker.isAppInForeground || responseCount(response) >= 2) {
            return null
        }

        return synchronized(refreshLock) {
            val failedAuthorization = response.request().header("Authorization")

            if (!sessionManager.hasSession()) {
                sessionManager.clearSession()
                return@synchronized null
            }

            val currentAuthorization = sessionManager.getAuthorizationHeader()

            if (currentAuthorization != null && failedAuthorization != currentAuthorization) {
                return@synchronized retryWith(response.request(), currentAuthorization)
            }

            val refreshToken = sessionManager.getRefreshToken()
                ?.takeIf { it.isNotBlank() }
                ?: return@synchronized null
            val expectedUserSyncId = sessionManager.getActiveUserSyncId()
                ?: return@synchronized null
            val expectedSessionId = sessionManager.getSessionId()
                ?: return@synchronized null

            val refreshResponse = try {
                refreshApi.refresh(
                    RefreshTokenRequest(
                        refreshToken = refreshToken,
                        deviceId = deviceId.toString()
                    )
                ).execute()
            } catch (_: IOException) {
                return@synchronized null
            }

            if (!refreshResponse.isSuccessful) {
                if (refreshResponse.code() == 401 || refreshResponse.code() == 403) {
                    sessionManager.clearSession()
                }

                return@synchronized null
            }

            val refreshedSession = refreshResponse.body() ?: return@synchronized null
            val returnedUserSyncId = runCatching {
                UUID.fromString(refreshedSession.user.syncId)
            }.getOrNull()
            val returnedSessionId = runCatching {
                UUID.fromString(refreshedSession.sessionId)
            }.getOrNull()

            if (
                refreshedSession.accessToken.isBlank() ||
                refreshedSession.refreshToken.isBlank() ||
                !refreshedSession.tokenType.equals("bearer", ignoreCase = true) ||
                refreshedSession.user.deletedAt != null ||
                returnedUserSyncId != expectedUserSyncId ||
                returnedSessionId != expectedSessionId
            ) {
                sessionManager.clearSession()
                return@synchronized null
            }

            try {
                sessionManager.saveSession(
                    accessToken = refreshedSession.accessToken,
                    refreshToken = refreshedSession.refreshToken,
                    sessionId = refreshedSession.sessionId,
                    userSyncId = expectedUserSyncId,
                    accessTokenExpiresAt = refreshedSession.accessTokenExpiresAt,
                    refreshTokenExpiresAt = refreshedSession.refreshTokenExpiresAt
                )
            } catch (_: Exception) {
                sessionManager.clearSession()
                return@synchronized null
            }

            val refreshedAuthorization = sessionManager.getAuthorizationHeader()
                ?: return@synchronized null

            retryWith(response.request(), refreshedAuthorization)
        }
    }

    private fun retryWith(request: Request, authorization: String): Request {
        return request.newBuilder()
            .header("Authorization", authorization)
            .build()
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var priorResponse = response.priorResponse()

        while (priorResponse != null) {
            count += 1
            priorResponse = priorResponse.priorResponse()
        }

        return count
    }
}
