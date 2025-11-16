package com.example.maiplan.network.sync

import android.content.Context
import com.example.maiplan.network.RetrofitClient
import com.example.maiplan.repository.category.CategoryLocalDataSource
import com.example.maiplan.repository.category.CategoryRemoteDataSource
import com.example.maiplan.repository.category.CategoryRepository

object ServiceLocator {
    fun provideSyncManager(context: Context): SyncManager {
        return SyncManager(
            provideCategoryRepo(context)
        )
    }

    private fun provideCategoryRepo(context: Context): CategoryRepository {
        val remote = CategoryRemoteDataSource(RetrofitClient.categoryApi)
        val local = CategoryLocalDataSource(context)
        return CategoryRepository(remote, local)
    }
}