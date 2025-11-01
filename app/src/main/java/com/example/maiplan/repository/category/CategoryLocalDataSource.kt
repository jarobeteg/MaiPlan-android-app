package com.example.maiplan.repository.category

import android.content.Context
import com.example.maiplan.database.MaiPlanDatabase
import com.example.maiplan.database.dao.CategoryDAO
import com.example.maiplan.database.entities.CategoryEntity
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.handleLocalResponse

class CategoryLocalDataSource(private val context: Context) {

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

    suspend fun getCategory(categoryId: Int, userId: Int): Result<CategoryEntity> {
        return handleLocalResponse {
            categoryDao.getCategory(categoryId, userId)
        }
    }

    suspend fun getCategories(userId: Int): Result<List<CategoryEntity>> {
        return handleLocalResponse {
            categoryDao.getCategories(userId)
        }
    }

    suspend fun categoryUpsert(category: CategoryEntity) {
        categoryDao.categoryUpsert(category)
    }

    suspend fun deleteCategory(category: CategoryEntity): Result<Int> {
        return handleLocalResponse {
            categoryDao.deleteCategory(category)
        }
    }
}