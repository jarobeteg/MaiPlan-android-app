package com.example.maiplan.network.api

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.POST

data class UsernameChangeRequest(val username: String)

data class PasswordChangeRequest(
    val password: String,
    @SerializedName("password_again") val passwordAgain: String,
)

interface AccountApi {
    @PATCH("auth/me")
    suspend fun changeUsername(@Body request: UsernameChangeRequest): Response<UserResponse>

    @POST("auth/change-password")
    suspend fun changePassword(@Body request: PasswordChangeRequest): Response<UserResponse>
}
