package com.example.maiplan.repository.auth

import com.example.maiplan.database.entities.AuthEntity
import com.example.maiplan.network.api.AuthResponse
import com.example.maiplan.network.api.Token
import com.example.maiplan.network.api.UserLogin
import com.example.maiplan.network.api.UserRegister
import com.example.maiplan.network.api.UserResetPassword
import com.example.maiplan.network.sync.Syncable
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.handleResponse

class AuthRepository(
    private val remote: AuthRemoteDataSource,
    private val local: AuthLocalDataSource
) : Syncable {

    override suspend fun sync() {}

    suspend fun localRegister(user: AuthEntity) {
        try {
            local.register(user)
        } catch (_: Exception) {

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
}