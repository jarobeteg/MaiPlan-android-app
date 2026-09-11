package com.example.maiplan.repository.auth

import com.example.maiplan.database.entities.UserEntity
import com.example.maiplan.network.api.AuthResponse
import com.example.maiplan.network.api.UserLoginRequest
import com.example.maiplan.network.api.UserRegisterRequest
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.handleRemoteResponse
import com.example.maiplan.utils.SessionManager
import kotlinx.coroutines.CancellationException
import java.util.UUID

class AuthRepository(
    private val remote: AuthRemoteDataSource,
    private val local: UserLocalDataSource,
    private val session: SessionManager
) {
    private suspend fun completeAuthentication(
        result: Result<AuthResponse>,
        expectedUserSyncId: UUID? = null,
        expectedSessionId: UUID? = null
    ): Result<UserEntity> {
        if (result !is Result.Success) {
            return when (result) {
                is Result.Failure -> result
                is Result.Error -> result
                is Result.Idle -> result
                is Result.Loading -> result
                is Result.Success -> error("Handled above")
            }
        }

        if (result.data.accessToken.isBlank()) {
            return Result.Error(
                IllegalStateException("The server returned a blank access token")
            )
        }

        if (result.data.refreshToken.isBlank()) {
            return Result.Error(
                IllegalStateException("The server returned a blank refresh token")
            )
        }

        if (!result.data.tokenType.equals("bearer", ignoreCase = true)) {
            return Result.Error(
                IllegalStateException("The server returned an unsupported token type")
            )
        }

        val returnedUserSyncId = runCatching {
            UUID.fromString(result.data.user.syncId)
        }.getOrElse {
            return Result.Error(IllegalStateException("The server returned an invalid user ID", it))
        }

        if (expectedUserSyncId != null && returnedUserSyncId != expectedUserSyncId) {
            return Result.Error(
                IllegalStateException("The server returned a different user")
            )
        }

        val returnedSessionId = runCatching {
            UUID.fromString(result.data.sessionId)
        }.getOrElse {
            return Result.Error(IllegalStateException("The server returned an invalid session ID", it))
        }

        if (expectedSessionId != null && returnedSessionId != expectedSessionId) {
            return Result.Error(
                IllegalStateException("The server replaced the session during token refresh")
            )
        }

        val localUser: UserEntity = local.reconcileServerUser(result.data.user)

        if (localUser.deletedAt != null) {
            return Result.Error(
                IllegalStateException("Cannot establish a session for a deleted user")
            )
        }

        try {
            session.saveSession(
                accessToken = result.data.accessToken,
                refreshToken = result.data.refreshToken,
                sessionId = result.data.sessionId,
                userSyncId = localUser.syncId,
                accessTokenExpiresAt = result.data.accessTokenExpiresAt,
                refreshTokenExpiresAt = result.data.refreshTokenExpiresAt
            )
        } catch (exception: Exception) {
            return Result.Error(exception)
        }

        return Result.Success(localUser)
    }

    suspend fun register(request: UserRegisterRequest): Result<UserEntity> {
        return try {
            val result = handleRemoteResponse(remote.register(request))

            completeAuthentication(result)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.Error(exception)
        }
    }

    suspend fun login(request: UserLoginRequest): Result<UserEntity> {
        return try {
            val result = handleRemoteResponse(remote.login(request))

            completeAuthentication(result)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.Error(exception)
        }
    }

    suspend fun getCachedSessionUser(): Result<UserEntity?> {
        return try {
            if (!session.hasSession()) {
                session.clearSession()
                return Result.Success(null)
            }

            val userSyncId = session.getActiveUserSyncId()
                ?: return Result.Success(null)

            val localUser = local.getActiveUserBySyncId(userSyncId)

            if (localUser == null) {
                session.clearSession()
            }

            Result.Success(localUser)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.Error(exception)
        }
    }

    suspend fun refreshSession(): Result<UserEntity> {
        return try {
            val expectedUserSyncId = session.getActiveUserSyncId()
                ?: run {
                    session.clearSession()
                    return Result.Error(IllegalStateException("No active user in the session"))
                }
            val expectedSessionId = session.getSessionId()
                ?: run {
                    session.clearSession()
                    return Result.Error(IllegalStateException("No session ID in the session"))
                }
            val refreshToken = session.getRefreshToken()
                ?.takeIf { it.isNotBlank() }
                ?: run {
                    session.clearSession()
                    return Result.Error(IllegalStateException("No refresh token in the session"))
                }

            when (
                val result = completeAuthentication(
                    result = handleRemoteResponse(remote.refresh(refreshToken)),
                    expectedUserSyncId = expectedUserSyncId,
                    expectedSessionId = expectedSessionId
                )
            ) {
                is Result.Success -> result
                is Result.Failure -> {
                    if (result.httpStatus == 401 || result.httpStatus == 403) {
                        session.clearSession()
                    }

                    result
                }
                is Result.Error -> {
                    session.clearSession()
                    result
                }
                is Result.Idle -> result
                is Result.Loading -> result
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.Error(exception)
        }
    }
}
