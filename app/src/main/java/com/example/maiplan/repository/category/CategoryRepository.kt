package com.example.maiplan.repository.category

import com.example.maiplan.network.api.CategoryApi
import com.example.maiplan.network.api.CategoryCreate
import com.example.maiplan.network.api.CategoryResponse
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.handleResponse

/**
 * Repository responsible for handling category-related business logic and network operations.
 *
 * Acts as an abstraction layer between the [CategoryRemoteDataSource] and the rest of the application.
 * Wraps raw network responses using [Result] and handles errors gracefully via [handleResponse].
 *
 * @property remoteDataSource An instance of [CategoryRemoteDataSource] that interacts with the [CategoryApi].
 *
 * @see CategoryRemoteDataSource
 * @see CategoryApi
 * @see Result
 * @see handleResponse
 */
class CategoryRepository(private val remoteDataSource: CategoryRemoteDataSource) {

    /**
     * Creates a new category.
     *
     * @param category The [CategoryCreate] object containing category name, icon, color, etc.
     * @return A [Result] indicating success (with [Unit]) or failure ([Exception]).
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
     * @param userId The unique identifier of the user.
     * @return A [Result] containing a list of [CategoryResponse] objects on success, or an [Exception] on failure.
     */
    suspend fun getAllCategories(userId: Int): Result<List<CategoryResponse>> {
        return try {
            handleResponse(remoteDataSource.getAllCategories(userId))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Updates an existing category with new data.
     *
     * @param category The [CategoryResponse] object containing updated category fields.
     * @return A [Result] indicating success (with [Unit]) or failure ([Exception]).
     */
    suspend fun updateCategory(category: CategoryResponse): Result<Unit> {
        return try {
            handleResponse(remoteDataSource.updateCategory(category))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Deletes a category identified by its unique ID.
     *
     * @param categoryId The ID of the category to delete.
     * @return A [Result] indicating success (with [Unit]) or failure ([Exception]).
     */
    suspend fun deleteCategory(categoryId: Int): Result<Unit> {
        return try {
            handleResponse(remoteDataSource.deleteCategory(categoryId))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}