package com.example.maiplan.repository.category

import com.example.maiplan.network.api.CategoryCreate
import com.example.maiplan.network.api.CategoryResponse
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.handleResponse

class CategoryRepository(private val remoteDataSource: CategoryRemoteDataSource) {
    suspend fun createCategory(category: CategoryCreate): Result<Unit> {
        return try {
            handleResponse(remoteDataSource.createCategory(category))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getAllCategories(userId: Int): Result<List<CategoryResponse>> {
        return try {
            handleResponse(remoteDataSource.getAllCategories(userId))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun updateCategory(category: CategoryResponse): Result<Unit> {
        return try {
            handleResponse(remoteDataSource.updateCategory(category))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun deleteCategory(categoryId: Int): Result<Unit> {
        return try {
            handleResponse(remoteDataSource.deleteCategory(categoryId))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}