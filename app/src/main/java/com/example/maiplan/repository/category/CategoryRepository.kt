package com.example.maiplan.repository.category

import com.example.maiplan.database.entities.CategoryEntity
import com.example.maiplan.database.entities.toCategoryEntity
import com.example.maiplan.database.entities.toCategoryResponse
import com.example.maiplan.database.entities.toCategorySync
import com.example.maiplan.network.api.CategoryCreate
import com.example.maiplan.network.api.CategoryResponse
import com.example.maiplan.network.api.CategorySync
import com.example.maiplan.network.sync.SyncRequest
import com.example.maiplan.network.sync.Syncable
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.map
import com.example.maiplan.utils.common.UserSession

class CategoryRepository(
    private val remote: CategoryRemoteDataSource,
    private val local: CategoryLocalDataSource
    ) : Syncable {

    override suspend fun sync() {
        try {
            val pendingCategoriesResult = local.getPendingCategories(UserSession.userId!!)
            if (pendingCategoriesResult is Result.Success) {
                val categories: List<CategoryEntity> = pendingCategoriesResult.data
                val changes: MutableList<CategorySync> = mutableListOf()
                categories.map { changes.add(it.toCategorySync()) }
                val request: SyncRequest<CategorySync> = SyncRequest(UserSession.userId!!, changes)
                val response = remote.categorySync(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        if (body.acknowledged.isNotEmpty()) body.acknowledged.map { local.categoryUpsert(it.toCategoryEntity()) }
                        if (body.rejected.isNotEmpty()) body.rejected.map { local.deleteCategory(it.toCategoryEntity()) }
                    }
                }
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun createCategory(category: CategoryCreate): Result<Unit> {
        return local.categoryInsert(category.toCategoryEntity())
    }

    suspend fun getAllCategories(userId: Int): Result<List<CategoryResponse>> {
        return local.getCategories(userId)
            .map { list -> list.map { it.toCategoryResponse() } }
    }

    suspend fun updateCategory(category: CategoryResponse, userId: Int): Result<Unit> {
        return local.categoryUpdate(category, userId)
    }

    suspend fun softDeleteCategory(categoryId: Int, userId: Int): Result<Unit> {
        return local.softDeleteCategory(categoryId, userId)
    }

    suspend fun deleteCategory(category: CategoryEntity): Result<Unit> {
        return local.deleteCategory(category)
    }
}