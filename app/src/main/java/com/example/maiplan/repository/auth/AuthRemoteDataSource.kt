package com.example.maiplan.repository.auth

import com.example.maiplan.network.api.AuthApi
import com.example.maiplan.network.api.Token
import com.example.maiplan.network.api.UserLogin
import com.example.maiplan.network.api.UserRegister
import com.example.maiplan.network.api.UserResetPassword
import com.example.maiplan.network.api.UserResponse
import retrofit2.Response

class AuthRemoteDataSource(private val authApi: AuthApi) {
    suspend fun register(user: UserRegister): Response<Token> {
        return authApi.register(user)
    }

    suspend fun login(user: UserLogin): Response<Token> {
        return authApi.login(user)
    }

    suspend fun resetPassword(user: UserResetPassword): Response<Token> {
        return authApi.resetPassword(user)
    }

    suspend fun tokenRefresh(token: String): Response<Token> {
        return authApi.tokenRefresh(token)
    }

    suspend fun getProfile(token: String): Response<UserResponse> {
        return  authApi.getProfile(token)
    }
}