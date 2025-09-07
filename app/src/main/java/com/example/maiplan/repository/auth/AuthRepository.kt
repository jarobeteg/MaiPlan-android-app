package com.example.maiplan.repository.auth

import com.example.maiplan.database.entities.AuthEntity
import com.example.maiplan.database.entities.toUserRegister
import com.example.maiplan.network.api.Token
import com.example.maiplan.network.api.UserLogin
import com.example.maiplan.network.api.UserRegister
import com.example.maiplan.network.api.UserResetPassword
import com.example.maiplan.network.api.UserResponse
import com.example.maiplan.network.sync.Syncable
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.handleResponse


class AuthRepository(
    private val remoteDataSource: AuthRemoteDataSource,
    private val localDataSource: AuthLocalDataSource
) : Syncable {

    override suspend fun sync() {
        val pending = localDataSource.getPendingUsers()
        if (pending.isNullOrEmpty()) return

        pending.forEach { user ->
            try {
                val result = register(user.toUserRegister())
                if (result is Result.Success) {
                    localDataSource.markSynced(listOf(user.userId))
                } else {
                    println("Sync failed for user ${user.userId}: $result")
                }
            } catch (e: Exception) {
                println("Exception syncing user ${user.userId}: ${e.message}")
            }
        }
    }

    suspend fun localRegister(user: AuthEntity): Long {
        return try {
            localDataSource.register(user)
        } catch (e: Exception) {

        } as Long
    }

    suspend fun register(user: UserRegister): Result<UserResponse> {
        return try {
            handleResponse(remoteDataSource.register(user))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun login(user: UserLogin): Result<Token> {
        return try {
            handleResponse(remoteDataSource.login(user))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun resetPassword(user: UserResetPassword): Result<Token> {
        return try {
            handleResponse(remoteDataSource.resetPassword(user))
        } catch (e: Exception){
            Result.Error(e)
        }
    }

    suspend fun tokenRefresh(token: String): Result<Token> {
        return try {
            handleResponse(remoteDataSource.tokenRefresh(token))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getProfile(token: String): Result<UserResponse> {
        return try {
            handleResponse(remoteDataSource.getProfile(token))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}