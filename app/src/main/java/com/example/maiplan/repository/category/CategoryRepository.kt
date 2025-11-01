package com.example.maiplan.repository.category

import com.example.maiplan.database.entities.toCategoryEntity
import com.example.maiplan.database.entities.toCategoryResponse
import com.example.maiplan.network.api.CategoryCreate
import com.example.maiplan.network.api.CategoryResponse
import com.example.maiplan.network.sync.Syncable
import com.example.maiplan.repository.Result

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
        return when (val result = local.getCategories(userId)) {
            is Result.Success -> Result.Success(result.data.map { it.toCategoryResponse() })
            is Result.Error -> Result.Error(result.exception)
            is Result.Failure -> Result.Failure(result.errorCode)
            is Result.Idle -> Result.Idle
            is Result.Loading -> Result.Loading
        }
    }

    suspend fun updateCategory(category: CategoryResponse, userId: Int): Result<Unit> {
        return local.categoryUpsert(category.toCategoryEntity(userId))
    }

    suspend fun deleteCategory(categoryId: Int, userId: Int): Result<Unit> {
        return when (val category = local.getCategory(categoryId, userId)) {
            is Result.Success -> local.deleteCategory(category.data)
            is Result.Error -> Result.Error(category.exception)
            is Result.Failure -> Result.Failure(category.errorCode)
            is Result.Idle -> Result.Idle
            is Result.Loading -> Result.Loading
        }
    }
}