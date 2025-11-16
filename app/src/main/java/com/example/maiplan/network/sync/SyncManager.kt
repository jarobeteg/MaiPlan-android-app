package com.example.maiplan.network.sync

import com.example.maiplan.repository.category.CategoryRepository

class SyncManager (
    private val categoryRepo: CategoryRepository
) {

    suspend fun syncAll() {
        categoryRepo.sync()
    }
}