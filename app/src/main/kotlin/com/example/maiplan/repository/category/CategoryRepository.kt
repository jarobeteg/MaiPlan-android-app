package com.example.maiplan.repository.category

import com.example.maiplan.category.data.CreateCategoryInput
import com.example.maiplan.category.data.UpdateCategoryInput
import com.example.maiplan.database.entities.CategoryEntity
import com.example.maiplan.repository.Result
import kotlinx.coroutines.flow.Flow

class CategoryRepository(
    private val local: CategoryLocalDataSource,
    private val requestSync: () -> Unit = {}
) {

    fun observeCategories(userLocalId: Long): Flow<List<CategoryEntity>> {
        return local.observeCategories(userLocalId)
    }

    suspend fun getCategory(categoryLocalId: Long, userLocalId: Long): CategoryEntity? {
        return local.getCategory(categoryLocalId, userLocalId)
    }

    suspend fun createCategory(input: CreateCategoryInput, userLocalId: Long): Result<Unit> {
        return local.createCategory(input, userLocalId).also(::requestSyncAfterSuccess)
    }

    suspend fun updateCategory(input: UpdateCategoryInput, userLocalId: Long): Result<Unit> {
        return local.updateCategory(input, userLocalId).also(::requestSyncAfterSuccess)
    }

    suspend fun softDeleteCategory(categoryLocalId: Long, userLocalId: Long): Result<Unit> {
        return local.softDeleteCategory(categoryLocalId, userLocalId).also(::requestSyncAfterSuccess)
    }

    private fun requestSyncAfterSuccess(result: Result<Unit>) {
        if (result is Result.Success) {
            runCatching(requestSync)
        }
    }
}
