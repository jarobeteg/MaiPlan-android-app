package com.example.maiplan.network.api

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class UserRegisterRequest(
    @SerializedName("sync_id")
    val syncId: String,

    val email: String,
    val username: String,
    val password: String,

    @SerializedName("password_again")
    val passwordAgain: String
)

data class UserLoginRequest(
    val email: String,
    val password: String
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
    val refreshToken: String? = null,

    val user: UserResponse
)

interface AuthApi {
    @POST("auth/register")
    suspend fun register(@Body request: UserRegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: UserLoginRequest): Response<AuthResponse>

    @GET("auth/me")
    suspend fun getProfile(): Response<UserResponse>
}