package com.example.maiplan.network.sync

import com.example.maiplan.repository.reminder.ReminderRepository
import java.util.UUID

class SyncManager (
    private val reminderRepo: ReminderRepository,
    private val categorySynchronizer: CategoryTideSynchronizer
) {

    suspend fun syncAll(userLocalId: Long, userSyncId: UUID): Boolean {
        val categoryResult = categorySynchronizer.sync(
            userLocalId = userLocalId,
            userSyncId = userSyncId
        )
        reminderRepo.sync()
        return categoryResult.hasMoreWork
    }
}
