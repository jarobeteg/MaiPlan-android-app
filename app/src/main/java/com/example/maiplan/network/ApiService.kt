package com.example.maiplan.network

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET

data class Token(@SerializedName("access_token") val accessToken: String, @SerializedName("token_type") val tokenType: String)
data class UserAuth(val email: String, val username: String, val password: String)
data class UserResponse(@SerializedName("user_id") val id: Int, val email: String, val username: String)

interface ApiService {
    @POST("register")
    suspend fun register(@Body auth: UserAuth): Response<UserResponse>

    @POST("login")
    suspend fun login(@Body auth: UserAuth): Response<Token>

    @GET("me")
    suspend fun getProfile(@Header("Authorization") token: String): Response<UserResponse>
}