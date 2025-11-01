package com.example.maiplan.repository.category

import com.example.maiplan.database.entities.CategoryEntity
import com.example.maiplan.database.entities.toCategoryEntity
import com.example.maiplan.database.entities.toCategoryResponse
import com.example.maiplan.network.api.CategoryCreate
import com.example.maiplan.network.api.CategoryResponse
import com.example.maiplan.network.sync.Syncable
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.map

class CategoryRepository(
    private val remote: CategoryRemoteDataSource,
    private val local: CategoryLocalDataSource
    ) : Syncable {

    override suspend fun sync() {
        TODO("Not yet implemented")
    }

    suspend fun createCategory(category: CategoryCreate): Result<Unit> {
        return local.categoryUpsert(category.toCategoryEntity())
    }

    suspend fun getAllCategories(userId: Int): Result<List<CategoryResponse>> {
        return local.getCategories(userId)
            .map { list -> list.map { it.toCategoryResponse() } }
    }

    suspend fun updateCategory(category: CategoryResponse, userId: Int): Result<Unit> {
        return local.categoryUpsert(category.toCategoryEntity(userId, syncState = 2))
    }

    suspend fun softDeleteCategory(categoryId: Int, userId: Int): Result<Unit> {
        return local.softDeleteCategory(categoryId, userId)
    }

    suspend fun deleteCategory(category: CategoryEntity): Result<Unit> {
        return local.deleteCategory(category)
    }
}