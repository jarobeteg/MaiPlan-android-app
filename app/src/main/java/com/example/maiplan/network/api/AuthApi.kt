package com.example.maiplan.network.api

import com.example.maiplan.network.sync.SyncRequest
import com.example.maiplan.network.sync.SyncResponse
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

data class Token(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String
)

data class UserRegister(
    val email: String,
    val username: String,
    val password: String,
    @SerializedName("password_again") val passwordAgain: String
)

data class UserResetPassword(
    val email: String,
    val password: String,
    @SerializedName("password_again") val passwordAgain: String
)

data class UserLogin(
    val email: String,
    val password: String
)

data class UserResponse(
    @SerializedName("user_id") val id: Int,
    val email: String,
    val username: String
)

data class AuthResponse(
    @SerializedName("access_token") val accessToken: String,
    val user: UserResponse
)

data class AuthSync(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("server_id") val serverId: Int,
    val email: String,
    val username: String,
    val balance: Float,
    @SerializedName("password_hash") val passwordHash: String,
    @SerializedName("last_modified") val lastModified: Long,
    @SerializedName("sync_state") val syncState: Int = 0,
    @SerializedName("is_deleted") val isDeleted: Int = 0
)

interface AuthApi {
    @POST("auth/register")
    suspend fun register(@Body auth: UserRegister): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body auth: UserLogin): Response<AuthResponse>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body auth: UserResetPassword): Response<AuthResponse>

    @POST("auth/token-refresh")
    suspend fun tokenRefresh(@Header("Authorization") token: String): Response<Token>

    @GET("auth/me")
    suspend fun getProfile(@Header("Authorization") token: String): Response<AuthResponse>

    @POST("auth/sync")
    suspend fun authSync(@Body request: SyncRequest<AuthSync>): Response<SyncResponse<AuthSync>>
}