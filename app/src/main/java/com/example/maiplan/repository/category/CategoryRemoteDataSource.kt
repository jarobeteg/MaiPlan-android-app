package com.example.maiplan.repository.category

import com.example.maiplan.network.api.CategoryApi
import com.example.maiplan.network.api.CategoryCreate
import com.example.maiplan.network.api.CategoryResponse
import retrofit2.Response

/**
 * Remote data source that directly communicates with the category-related endpoints via [CategoryApi].
 *
 * Provides suspend functions that return raw [Response] objects for use by the [CategoryRepository].
 * This layer abstracts direct API calls from the repository layer.
 *
 * @property categoryApi An instance of [CategoryApi] used to perform category network requests.
 *
 * @see CategoryApi
 */
class CategoryRemoteDataSource(private val categoryApi: CategoryApi) {

    /**
     * Sends a request to create a new category.
     *
     * @param category The [CategoryCreate] payload containing category details.
     * @return A raw [Response] object indicating success or failure.
     */
    suspend fun createCategory(category: CategoryCreate): Response<Unit> {
        return categoryApi.createCategory(category)
    }

    /**
     * Retrieves all categories associated with a given user.
     *
     * @param userId The unique ID of the user whose categories should be fetched.
     * @return A raw [Response] containing a list of [CategoryResponse] objects or an error body.
     */
    suspend fun getAllCategories(userId: Int): Response<List<CategoryResponse>> {
        return categoryApi.getAllCategories(userId)
    }

    /**
     * Sends a request to update an existing category.
     *
     * @param category The [CategoryResponse] object with updated data.
     * @return A raw [Response] indicating success or failure.
     */
    suspend fun updateCategory(category: CategoryResponse): Response<Unit> {
        return categoryApi.updateCategory(category)
    }

    /**
     * Sends a request to delete a category by its ID.
     *
     * @param categoryId The unique ID of the category to be deleted.
     * @return A raw [Response] indicating success or failure.
     */
    suspend fun deleteCategory(categoryId: Int): Response<Unit> {
        return categoryApi.deleteCategory(categoryId)
    }
}