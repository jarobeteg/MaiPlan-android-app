package com.example.maiplan.network.api

import com.example.maiplan.network.sync.SyncRequest
import com.example.maiplan.network.sync.SyncResponse
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

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

data class CategorySync(
    @SerializedName("category_id") val categoryId: Int,
    @SerializedName("server_id") val serverId: Int,
    @SerializedName("user_id") val userId: Int,
    val name: String,
    val description: String,
    val color: String,
    val icon: String,
    @SerializedName("last_modified") val lastModified: Long,
    @SerializedName("sync_state") val syncState: Int = 0,
    @SerializedName("is_deleted") val isDeleted: Int = 0
)

interface CategoryApi {
    @POST("categories/create-category")
    suspend fun createCategory(@Body categoryCreate: CategoryCreate): Response<Unit>

    @GET("categories/get-all-category")
    suspend fun getAllCategories(@Query("user_id") userId: Int): Response<List<CategoryResponse>>

    @POST("categories/update-category")
    suspend fun updateCategory(@Body category: CategoryResponse): Response<Unit>

    @DELETE("categories/{category_id}")
    suspend fun deleteCategory(@Path("category_id") categoryId: Int): Response<Unit>

    @POST("categories/sync")
    suspend fun categorySync(@Body request: SyncRequest<CategorySync>): Response<SyncResponse<CategorySync>>
}