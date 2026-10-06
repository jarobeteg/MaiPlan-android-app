package com.example.maiplan.repository.auth

import com.example.maiplan.database.entities.UserEntity
import com.example.maiplan.network.api.AccountApi
import com.example.maiplan.network.api.PasswordChangeRequest
import com.example.maiplan.network.api.UserResponse
import com.example.maiplan.network.api.UsernameChangeRequest
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.handleRemoteResponse
import com.example.maiplan.utils.SessionManager
import kotlinx.coroutines.CancellationException
import retrofit2.Response
import java.util.UUID

class AccountRepository(
    private val api: AccountApi,
    private val local: UserLocalDataSource,
    private val session: SessionManager,
) {
    suspend fun changeUsername(username: String): Result<UserEntity> = updateProfile {
        api.changeUsername(UsernameChangeRequest(username))
    }

    suspend fun changePassword(password: String, passwordAgain: String): Result<UserEntity> = updateProfile {
        api.changePassword(PasswordChangeRequest(password, passwordAgain))
    }

    private suspend fun updateProfile(request: suspend () -> Response<UserResponse>): Result<UserEntity> {
        val expectedUserId = session.getActiveUserSyncId()
            ?: return Result.Failure(errorCode = -1, httpStatus = 401)
        return try {
            when (val result = handleRemoteResponse(request())) {
                is Result.Success -> {
                    check(UUID.fromString(result.data.syncId) == expectedUserId) {
                        "Server returned a different account"
                    }
                    check(result.data.deletedAt == null) { "Account is unavailable" }
                    if (session.getActiveUserSyncId() != expectedUserId) {
                        return Result.Failure(errorCode = -1, httpStatus = 401)
                    }
                    val user = local.reconcileServerUser(result.data)
                    if (session.getActiveUserSyncId() != expectedUserId) {
                        return Result.Failure(errorCode = -1, httpStatus = 401)
                    }
                    Result.Success(user)
                }
                is Result.Failure -> result
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
