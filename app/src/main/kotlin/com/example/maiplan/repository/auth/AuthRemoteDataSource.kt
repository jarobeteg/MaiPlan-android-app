package com.example.maiplan.repository.auth

import com.example.maiplan.network.api.AuthApi
import com.example.maiplan.network.api.AuthResponse
import com.example.maiplan.network.api.RefreshTokenRequest
import com.example.maiplan.network.api.UserLoginRequest
import com.example.maiplan.network.api.UserRegisterRequest
import retrofit2.Response
import java.util.UUID

class AuthRemoteDataSource(
    private val publicAuthApi: AuthApi,
    private val deviceId: UUID
) {
    suspend fun register(user: UserRegisterRequest): Response<AuthResponse> {
        return publicAuthApi.register(
            user.copy(deviceId = deviceId.toString())
        )
    }

    suspend fun login(user: UserLoginRequest): Response<AuthResponse> {
        return publicAuthApi.login(
            user.copy(deviceId = deviceId.toString())
        )
    }

    suspend fun refresh(refreshToken: String): Response<AuthResponse> {
        return publicAuthApi.refresh(
            RefreshTokenRequest(
                refreshToken = refreshToken,
                deviceId = deviceId.toString()
            )
        )
    }

}
