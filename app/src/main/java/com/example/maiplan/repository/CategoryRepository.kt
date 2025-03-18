package com.example.maiplan.repository

import com.example.maiplan.network.CategoryApi
import com.example.maiplan.network.CategoryCreate
import com.example.maiplan.network.CategoryResponse

class CategoryRepository(private val categoryApi: CategoryApi) {
    suspend fun createCategory(category: CategoryCreate): Result<Unit> {
        return try {
            handleResponse(categoryApi.createCategory(category))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getAllCategories(userId: Int): Result<List<CategoryResponse>> {
        return try {
            handleResponse(categoryApi.getAllCategories(userId))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun updateCategory(category: CategoryResponse): Result<Unit> {
        return try {
            handleResponse(categoryApi.updateCategory(category))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun deleteCategory(categoryId: Int): Result<Unit> {
        return try {
            handleResponse(categoryApi.deleteCategory(categoryId))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}