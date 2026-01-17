package com.example.maiplan.repository.auth

import com.example.maiplan.network.api.AuthApi
import com.example.maiplan.network.api.AuthResponse
import com.example.maiplan.network.api.AuthSync
import com.example.maiplan.network.api.Token
import com.example.maiplan.network.api.UserLogin
import com.example.maiplan.network.api.UserRegister
import com.example.maiplan.network.api.UserResetPassword
import com.example.maiplan.network.sync.SyncRequest
import com.example.maiplan.network.sync.SyncResponse
import retrofit2.Response

class AuthRemoteDataSource(private val authApi: AuthApi) {
    suspend fun register(user: UserRegister): Response<AuthResponse> {
        return authApi.register(user)
    }

    suspend fun login(user: UserLogin): Response<AuthResponse> {
        return authApi.login(user)
    }

    suspend fun resetPassword(user: UserResetPassword): Response<AuthResponse> {
        return authApi.resetPassword(user)
    }

    suspend fun tokenRefresh(token: String): Response<Token> {
        return authApi.tokenRefresh(token)
    }

    suspend fun getProfile(token: String): Response<AuthResponse> {
        return authApi.getProfile(token)
    }

    suspend fun authSync(request: SyncRequest<AuthSync>): Response<SyncResponse<AuthSync>> {
        return authApi.authSync(request)
    }
}