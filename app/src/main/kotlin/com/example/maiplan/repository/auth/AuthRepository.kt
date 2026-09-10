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
    private suspend fun completeAuthentication(result: Result<AuthResponse>): Result<UserEntity> {
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

        if (!result.data.tokenType.equals("bearer", ignoreCase = true)) {
            return Result.Error(
                IllegalStateException("The server returned an unsupported token type")
            )
        }

        val localUser: UserEntity = local.reconcileServerUser(result.data.user)

        check(localUser.deletedAt == null) {
            "Cannot establish a session for a deleted user"
        }

        session.saveSession(
            accessToken = result.data.accessToken,
            refreshToken = result.data.refreshToken,
            userSyncId = localUser.syncId
        )

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

    suspend fun refreshProfile(): Result<UserEntity> {
        return try {
            val expectedSyncId = session.getActiveUserSyncId()
                ?: run {
                    session.clearSession()
                    return Result.Error(IllegalStateException("No active user in the session"))
                }

            if (session.getAccessToken().isNullOrBlank()) {
                session.clearSession()
                return Result.Error(IllegalStateException("No access token in the session"))
            }

            when (
                val result = handleRemoteResponse(remote.getProfile())
            ) {
                is Result.Success -> {
                    val returnedSyncId = UUID.fromString(result.data.syncId)

                    if (returnedSyncId != expectedSyncId) {
                        session.clearSession()

                        return Result.Error(
                            IllegalStateException("The server returned a different user")
                        )
                    }

                    val localUser = local.reconcileServerUser(result.data)

                    if (localUser.deletedAt != null) {
                        session.clearSession()

                        Result.Error(IllegalStateException("The current user account has been deleted"))
                    } else {
                        Result.Success(localUser)
                    }
                }

                is Result.Failure -> {
                    if (result.httpStatus == 401 || result.httpStatus == 403) {
                        session.clearSession()
                    }

                    result
                }
                is Result.Error -> result
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
