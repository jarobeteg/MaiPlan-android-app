package com.example.maiplan.network

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Data model for creating a new category.
 *
 * @property userId The ID of the user creating the category.
 * @property name The name of the category.
 * @property description A description of the category.
 * @property color The color associated with the category (HEX string).
 * @property icon The icon representing the category.
 */
data class CategoryCreate(
    @SerializedName("user_id") val userId: Int,
    val name: String,
    val description: String,
    val color: String,
    val icon: String
)

/**
 * Data model representing a category fetched from the server.
 *
 * @property categoryId The unique ID of the category.
 * @property name The name of the category.
 * @property description A description of the category.
 * @property color The color associated with the category (HEX string).
 * @property icon The icon representing the category.
 */
data class CategoryResponse(
    @SerializedName("category_id") val categoryId: Int,
    val name: String,
    val description: String,
    val color: String,
    val icon: String
)

/**
 * API interface defining category-related endpoints.
 */
interface CategoryApi {

    /**
     * Creates a new category.
     *
     * @param categoryCreate The [CategoryCreate] request body.
     * @return A [Response] indicating success or failure.
     */
    @POST("categories/create-category")
    suspend fun createCategory(@Body categoryCreate: CategoryCreate): Response<Unit>

    /**
     * Fetches all categories for a specific user.
     *
     * @param userId The ID of the user whose categories to fetch.
     * @return A [Response] containing a list of [CategoryResponse] items.
     */
    @GET("categories/get-all-category")
    suspend fun getAllCategories(@Query("user_id") userId: Int): Response<List<CategoryResponse>>

    /**
     * Updates an existing category.
     *
     * @param category The [CategoryResponse] representing updated category data.
     * @return A [Response] indicating success or failure.
     */
    @POST("categories/update-category")
    suspend fun updateCategory(@Body category: CategoryResponse): Response<Unit>

    /**
     * Deletes a category by its ID.
     *
     * @param categoryId The ID of the category to delete.
     * @return A [Response] indicating success or failure.
     */
    @DELETE("categories/{category_id}")
    suspend fun deleteCategory(@Path("category_id") categoryId: Int): Response<Unit>
}