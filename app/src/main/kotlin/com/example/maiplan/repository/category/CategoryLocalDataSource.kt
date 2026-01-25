package com.example.maiplan.repository.category

import android.content.Context
import com.example.maiplan.database.MaiPlanDatabase
import com.example.maiplan.database.dao.CategoryDAO
import com.example.maiplan.database.entities.CategoryEntity
import com.example.maiplan.network.api.CategoryResponse
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.handleLocalResponse

class CategoryLocalDataSource(private val context: Context) {
    companion object {
        private const val EMPTY_CATEGORY_NAME_ERROR = 1
        private const val EMPTY_CATEGORY_DESCRIPTION_ERROR = 2
    }

    private val database: MaiPlanDatabase by lazy {
        MaiPlanDatabase.getDatabase(context)
    }

    private val categoryDao: CategoryDAO by lazy {
        database.categoryDAO()
    }

    suspend fun getPendingCategories(userId: Int): Result<List<CategoryEntity>> {
        return handleLocalResponse {
            categoryDao.getPendingCategories(userId)
        }
    }

    suspend fun getCategory(categoryId: Int?, userId: Int): CategoryEntity {
        return categoryDao.getCategory(categoryId!!, userId)
    }

    suspend fun getCategories(userId: Int): Result<List<CategoryEntity>> {
        return handleLocalResponse {
            categoryDao.getCategories(userId)
        }
    }

    suspend fun categoryUpsert(category: CategoryEntity): Result<Unit> {
        return handleLocalResponse {
            categoryDao.categoryUpsert(category)
        }
    }

    suspend fun categoryUpdate(category: CategoryResponse, userId: Int): Result<Unit> {
        if (category.name.isEmpty() || category.name.isBlank()) {
            return Result.Failure(EMPTY_CATEGORY_NAME_ERROR)
        }

        if (category.description.isEmpty() || category.description.isBlank()) {
            return Result.Failure(EMPTY_CATEGORY_DESCRIPTION_ERROR)
        }

        return handleLocalResponse {
            categoryDao.categoryUpdate(category.name, category.description, category.color, category.icon, category.categoryId, userId)
        }
    }

    suspend fun categoryInsert(category: CategoryEntity): Result<Unit> {
        if (category.name.isEmpty() || category.name.isBlank()) {
            return Result.Failure(EMPTY_CATEGORY_NAME_ERROR)
        }

        if (category.description.isEmpty() || category.description.isBlank()) {
            return Result.Failure(EMPTY_CATEGORY_DESCRIPTION_ERROR)
        }

        return handleLocalResponse {
            categoryDao.categoryInsert(category)
        }
    }

    suspend fun softDeleteCategory(categoryId: Int, userId: Int): Result<Unit> {
        return handleLocalResponse {
            categoryDao.softDeleteCategory(categoryId, userId)
        }
    }

    suspend fun deleteCategory(category: CategoryEntity): Result<Unit> {
        return handleLocalResponse {
            categoryDao.deleteCategory(category)
        }
    }

    suspend fun getCategoryId(serverId: Int): Int? {
        return categoryDao.getCategoryId(serverId)
    }

    suspend fun getServerId(localId: Int): Int? {
        return categoryDao.getServerId(localId)
    }
}