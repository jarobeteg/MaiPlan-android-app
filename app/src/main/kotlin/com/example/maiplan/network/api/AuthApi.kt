package com.example.maiplan.network.api

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class UserRegisterRequest(
    @SerializedName("sync_id")
    val syncId: String,

    val email: String,
    val username: String,
    val password: String,

    @SerializedName("password_again")
    val passwordAgain: String,

    @SerializedName("device_id")
    val deviceId: String? = null
)

data class UserLoginRequest(
    val email: String,
    val password: String,

    @SerializedName("device_id")
    val deviceId: String? = null
)

data class RefreshTokenRequest(
    @SerializedName("refresh_token")
    val refreshToken: String,

    @SerializedName("device_id")
    val deviceId: String
)

data class UserResponse(
    @SerializedName("sync_id")
    val syncId: String,

    val email: String,
    val username: String,

    @SerializedName("server_version")
    val serverVersion: Long,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String,

    @SerializedName("deleted_at")
    val deletedAt: String?
)

data class AuthResponse(
    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("token_type")
    val tokenType: String,

    @SerializedName("refresh_token")
    val refreshToken: String,

    @SerializedName("session_id")
    val sessionId: String,

    @SerializedName("access_token_expires_at")
    val accessTokenExpiresAt: String,

    @SerializedName("refresh_token_expires_at")
    val refreshTokenExpiresAt: String,

    val user: UserResponse
)

interface AuthApi {
    @POST("auth/register")
    suspend fun register(@Body request: UserRegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: UserLoginRequest): Response<AuthResponse>

    @POST("auth/refresh")
    suspend fun refresh(@Body request: RefreshTokenRequest): Response<AuthResponse>
}

interface TokenRefreshApi {
    @POST("auth/refresh")
    fun refresh(@Body request: RefreshTokenRequest): Call<AuthResponse>
}
