package com.example.maiplan.repository

import com.example.maiplan.network.api.CategoryApi
import com.example.maiplan.network.api.CategoryCreate
import com.example.maiplan.network.api.CategoryResponse

/**
 * Repository responsible for handling category-related network operations.
 *
 * Wraps API calls and returns a [Result] indicating success or failure,
 * abstracting network and error handling from the rest of the app.
 *
 * @property categoryApi An instance of [CategoryApi] for making category network requests.
 *
 * @see CategoryApi
 * @see Result
 */
class CategoryRepository(private val categoryApi: CategoryApi) {

    /**
     * Creates a new category.
     *
     * @param category The [CategoryCreate] object containing category data.
     * @return A [Result] indicating success or failure.
     *
     * @see Result
     * @see CategoryCreate
     * @see handleResponse
     */
    suspend fun createCategory(category: CategoryCreate): Result<Unit> {
        return try {
            handleResponse(categoryApi.createCategory(category))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Fetches all categories associated with a specific user.
     *
     * @param userId The Id of the user.
     * @return A [Result] containing a list of [CategoryResponse] or an error.
     *
     * @see Result
     * @see CategoryResponse
     * @see handleResponse
     */
    suspend fun getAllCategories(userId: Int): Result<List<CategoryResponse>> {
        return try {
            handleResponse(categoryApi.getAllCategories(userId))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Updates an existing category.
     *
     * @param category The [CategoryResponse] object with updated information.
     * @return A [Result] indicating success or failure.
     *
     * @see Result
     * @see CategoryResponse
     * @see handleResponse
     */
    suspend fun updateCategory(category: CategoryResponse): Result<Unit> {
        return try {
            handleResponse(categoryApi.updateCategory(category))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Deletes a category by its Id.
     *
     * @param categoryId The Id of the category to delete.
     * @return A [Result] indicating success or failure.
     *
     * @see Result
     * @see handleResponse
     */
    suspend fun deleteCategory(categoryId: Int): Result<Unit> {
        return try {
            handleResponse(categoryApi.deleteCategory(categoryId))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}