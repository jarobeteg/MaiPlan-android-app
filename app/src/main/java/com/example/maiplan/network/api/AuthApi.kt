package com.example.maiplan.network.api

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST


data class Token(@SerializedName("access_token") val accessToken: String, @SerializedName("token_type") val tokenType: String)

data class UserRegister(val email: String, val username: String, @SerializedName("password_hash") val passwordHash: String)

data class UserResetPassword(val email: String, val password: String, @SerializedName("password_again") val passwordAgain: String)

data class UserLogin(val email: String, val password: String)

data class UserResponse(@SerializedName("user_id") val id: Int, val email: String, val username: String)

interface AuthApi {
    @POST("auth/register")
    suspend fun register(@Body auth: UserRegister): Response<UserResponse>

    @POST("auth/login")
    suspend fun login(@Body auth: UserLogin): Response<Token>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body auth: UserResetPassword): Response<Token>

    @POST("auth/token-refresh")
    suspend fun tokenRefresh(@Header("Authorization") token: String): Response<Token>

    @GET("auth/me")
    suspend fun getProfile(@Header("Authorization") token: String): Response<UserResponse>
}