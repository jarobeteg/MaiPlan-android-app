package com.example.maiplan.repository.auth

import com.example.maiplan.network.api.AuthApi
import com.example.maiplan.network.api.AuthResponse
import com.example.maiplan.network.api.UserResponse
import com.example.maiplan.network.api.UserLoginRequest
import com.example.maiplan.network.api.UserRegisterRequest
import retrofit2.Response

class AuthRemoteDataSource(
    private val publicAuthApi: AuthApi,
    private val authenticatedAuthApi: AuthApi
) {
    suspend fun register(user: UserRegisterRequest): Response<AuthResponse> {
        return publicAuthApi.register(user)
    }

    suspend fun login(user: UserLoginRequest): Response<AuthResponse> {
        return publicAuthApi.login(user)
    }

    suspend fun getProfile(): Response<UserResponse> {
        return authenticatedAuthApi.getProfile()
    }
}
