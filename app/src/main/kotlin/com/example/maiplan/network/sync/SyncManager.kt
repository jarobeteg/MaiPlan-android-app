package com.example.maiplan.network.sync

import com.example.maiplan.repository.category.CategoryRepository
import com.example.maiplan.repository.event.EventRepository
import com.example.maiplan.repository.reminder.ReminderRepository

class SyncManager (
    private val categoryRepo: CategoryRepository,
    private val reminderRepo: ReminderRepository,
    private val eventRepo: EventRepository
) {

    suspend fun syncAll() {
        categoryRepo.sync()
        reminderRepo.sync()
        eventRepo.sync()
    }
}