package com.example.maiplan.repository.auth

import com.example.maiplan.database.entities.AuthEntity
import com.example.maiplan.database.entities.AuthEntityResponse
import com.example.maiplan.database.entities.toAuthEntity
import com.example.maiplan.database.entities.toAuthSync
import com.example.maiplan.network.api.AuthResponse
import com.example.maiplan.network.api.AuthSync
import com.example.maiplan.network.api.Token
import com.example.maiplan.network.api.UserLogin
import com.example.maiplan.network.api.UserRegister
import com.example.maiplan.network.api.UserResetPassword
import com.example.maiplan.network.api.UserResponse
import com.example.maiplan.network.sync.SyncRequest
import com.example.maiplan.network.sync.Syncable
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.handleResponse
import com.example.maiplan.utils.common.UserSession

class AuthRepository(
    private val remote: AuthRemoteDataSource,
    private val local: AuthLocalDataSource
) : Syncable {

    // a side note to later be fixed when the code will be refactored
    // if the returned user_id is 0 or the user got registered on the server first
    // how to sync such data as those
    override suspend fun sync() {
        try {
            val pendingUser: AuthEntity? = local.getPendingUser(UserSession.email!!)
            val changes: MutableList<AuthSync> = mutableListOf()
            if (pendingUser != null) changes.add(pendingUser.toAuthSync())
            val request : SyncRequest<AuthSync> = SyncRequest(UserSession.email!!, changes)
            val response = remote.authSync(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    if (body.acknowledged.isNotEmpty()) local.authSync(body.acknowledged.first().toAuthEntity())
                    if (body.rejected.isNotEmpty()) local.deleteUser(body.rejected.first().toAuthEntity())
                }
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun register(user: UserRegister): Result<AuthResponse> {
        return try {
            handleResponse(remote.register(user))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun login(user: UserLogin): Result<AuthResponse> {
        return try {
            handleResponse(remote.login(user))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun localLogin(user: UserLogin): AuthEntityResponse? {
        return local.login(user)
    }

    suspend fun resetPassword(user: UserResetPassword): Result<AuthResponse> {
        return try {
            handleResponse(remote.resetPassword(user))
        } catch (e: Exception){
            Result.Error(e)
        }
    }

    suspend fun tokenRefresh(token: String): Result<Token> {
        return try {
            handleResponse(remote.tokenRefresh(token))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getProfile(token: String): Result<AuthResponse> {
        return try {
            handleResponse(remote.getProfile(token))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun pseudoAuth(user: UserResponse): Boolean {
        if (!local.doesUserExist(user.email)) {
            val auth = AuthEntity(
                userId = user.id,
                email = user.email,
                username = user.username,
                passwordHash = "pseudo",
                syncState = 4
            )
            return local.insertPseudoAuth(auth)
        } else {
            return true
        }
    }
}