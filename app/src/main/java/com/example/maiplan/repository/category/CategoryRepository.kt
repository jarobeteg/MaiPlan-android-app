package com.example.maiplan.repository.category

import com.example.maiplan.network.api.CategoryCreate
import com.example.maiplan.network.api.CategoryResponse
import com.example.maiplan.network.sync.Syncable
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.handleResponse

class CategoryRepository(
    private val remote: CategoryRemoteDataSource,
    private val local: CategoryLocalDataSource
    ) : Syncable {

    override suspend fun sync() {
        TODO("Not yet implemented")
    }

    suspend fun createCategory(category: CategoryCreate): Result<Unit> {
        return try {
            handleResponse(remote.createCategory(category))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getAllCategories(userId: Int): Result<List<CategoryResponse>> {
        return try {
            handleResponse(remote.getAllCategories(userId))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun updateCategory(category: CategoryResponse): Result<Unit> {
        return try {
            handleResponse(remote.updateCategory(category))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun deleteCategory(categoryId: Int): Result<Unit> {
        return try {
            handleResponse(remote.deleteCategory(categoryId))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}