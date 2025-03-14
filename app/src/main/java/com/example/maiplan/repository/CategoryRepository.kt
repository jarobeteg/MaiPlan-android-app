package com.example.maiplan.repository

import com.example.maiplan.network.CategoryApi
import com.example.maiplan.network.CategoryCreate
import com.example.maiplan.network.CategoryResponse
import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Response

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

    private fun <T> handleResponse(response: Response<T>): Result<T> {
        return if (response.isSuccessful) {
            response.body()?.let {
                Result.Success(it)
            } ?: Result.Failure(-1)
        } else {
            val errorBody = response.errorBody()?.string()
            val json = Gson().fromJson(errorBody, JsonObject::class.java)
            val errorDetail = json.getAsJsonObject("detail")
            val errorCode = errorDetail?.get("code")?.asInt ?: -1
            Result.Failure(errorCode)
        }
    }
}