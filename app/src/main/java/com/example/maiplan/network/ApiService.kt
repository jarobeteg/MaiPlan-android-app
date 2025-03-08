package com.example.maiplan.network

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// user auth data classes
data class Token(@SerializedName("access_token") val accessToken: String, @SerializedName("token_type") val tokenType: String)
data class UserRegister(val email: String, val username: String, val password: String, @SerializedName("password_again") val passwordAgain: String)
data class UserResetPassword(val email: String, val password: String, @SerializedName("password_again") val passwordAgain: String)
data class UserLogin(val email: String, val password: String)
data class UserResponse(@SerializedName("user_id") val id: Int, val email: String, val username: String)

// category data classes
data class CategoryCreate(
    @SerializedName("user_id") val userId: Int,
    val name: String,
    val description: String,
    val color: String,
    val icon: String
    )
data class CategoryResponse(
    @SerializedName("category_id") val categoryId: Int,
    val name: String,
    val description: String,
    val color: String,
    val icon: String
)

interface ApiService {
    // user auth api
    @POST("auth/register")
    suspend fun register(@Body auth: UserRegister): Response<Token>

    @POST("auth/login")
    suspend fun login(@Body auth: UserLogin): Response<Token>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body auth: UserResetPassword): Response<Token>

    @POST("auth/token-refresh")
    suspend fun tokenRefresh(@Header("Authorization") token: String): Response<Token>

    @GET("auth/me")
    suspend fun getProfile(@Header("Authorization") token: String): Response<UserResponse>

    // category api
    @POST("categories/create-category")
    suspend fun createCategory(@Body categoryCreate: CategoryCreate): Response<Unit>

    @GET("categories/get-all-category")
    suspend fun getAllCategories(@Query("user_id") userId: Int): Response<List<CategoryResponse>>

    @POST("categories/update-category")
    suspend fun updateCategory(@Body category: CategoryResponse): Response<Unit>

    @DELETE("categories/{category_id}")
    suspend fun deleteCategory(@Path("category_id") categoryId: Int): Response<Unit>
}