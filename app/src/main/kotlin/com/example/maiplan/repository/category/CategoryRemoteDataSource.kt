package com.example.maiplan.repository.category

import com.example.maiplan.network.api.CategoryApi
import com.example.maiplan.network.api.CategoryCreate
import com.example.maiplan.network.api.CategoryResponse
import com.example.maiplan.network.api.CategorySync
import com.example.maiplan.network.sync.SyncRequest
import com.example.maiplan.network.sync.SyncResponse
import retrofit2.Response

class CategoryRemoteDataSource(private val categoryApi: CategoryApi) {
    suspend fun createCategory(category: CategoryCreate): Response<Unit> {
        return categoryApi.createCategory(category)
    }

    suspend fun getAllCategories(userId: Int): Response<List<CategoryResponse>> {
        return categoryApi.getAllCategories(userId)
    }

    suspend fun updateCategory(category: CategoryResponse): Response<Unit> {
        return categoryApi.updateCategory(category)
    }

    suspend fun deleteCategory(categoryId: Int): Response<Unit> {
        return categoryApi.deleteCategory(categoryId)
    }

    suspend fun categorySync(request: SyncRequest<CategorySync>): Response<SyncResponse<CategorySync>> {
        return categoryApi.categorySync(request)
    }
}