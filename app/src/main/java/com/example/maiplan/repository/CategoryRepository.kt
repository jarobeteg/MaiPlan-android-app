package com.example.maiplan.repository

import com.example.maiplan.network.ApiService
import com.example.maiplan.network.CategoryCreate
import com.example.maiplan.network.CategoryResponse
import retrofit2.Response

class CategoryRepository(private val apiService: ApiService) {
    suspend fun createCategory(category: CategoryCreate): Result<Unit> {
        return try {
            handleResponse(apiService.createCategory(category))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getAllCategories(userId: Int): Result<List<CategoryResponse>> {
        return try {
            handleResponse(apiService.getAllCategories(userId))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    private fun <T> handleResponse(response: Response<T>): Result<T> {
        return if (response.isSuccessful) {
            response.body()?.let {
                Result.Success(it)
            } ?: Result.Failure(-1)
        } else {
            Result.Failure(-1)
        }
    }
}