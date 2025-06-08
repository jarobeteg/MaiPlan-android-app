package com.example.maiplan.repository.category

import com.example.maiplan.network.api.CategoryApi
import com.example.maiplan.network.api.CategoryCreate
import com.example.maiplan.network.api.CategoryResponse
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.handleResponse

/**
 * Repository responsible for handling category-related network operations.
 *
 * Wraps API calls and returns a [com.example.maiplan.repository.Result] indicating success or failure,
 * abstracting network and error handling from the rest of the app.
 *
 * @property categoryApi An instance of [com.example.maiplan.network.api.CategoryApi] for making category network requests.
 *
 * @see com.example.maiplan.network.api.CategoryApi
 * @see com.example.maiplan.repository.Result
 */
class CategoryRepository(private val remoteDataSource: CategoryRemoteDataSource) {

    /**
     * Creates a new category.
     *
     * @param category The [com.example.maiplan.network.api.CategoryCreate] object containing category data.
     * @return A [com.example.maiplan.repository.Result] indicating success or failure.
     *
     * @see com.example.maiplan.repository.Result
     * @see com.example.maiplan.network.api.CategoryCreate
     * @see com.example.maiplan.repository.handleResponse
     */
    suspend fun createCategory(category: CategoryCreate): Result<Unit> {
        return try {
            handleResponse(remoteDataSource.createCategory(category))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Fetches all categories associated with a specific user.
     *
     * @param userId The Id of the user.
     * @return A [Result] containing a list of [com.example.maiplan.network.api.CategoryResponse] or an error.
     *
     * @see Result
     * @see com.example.maiplan.network.api.CategoryResponse
     * @see handleResponse
     */
    suspend fun getAllCategories(userId: Int): Result<List<CategoryResponse>> {
        return try {
            handleResponse(remoteDataSource.getAllCategories(userId))
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
            handleResponse(remoteDataSource.updateCategory(category))
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
            handleResponse(remoteDataSource.deleteCategory(categoryId))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}