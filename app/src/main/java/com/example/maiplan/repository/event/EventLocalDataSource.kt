package com.example.maiplan.repository.event

import android.content.Context
import androidx.room.Transaction
import com.example.maiplan.database.MaiPlanDatabase
import com.example.maiplan.database.dao.EventDAO
import com.example.maiplan.database.dao.ReminderDAO
import com.example.maiplan.database.entities.EventEntity
import com.example.maiplan.database.entities.ReminderEntity

class EventLocalDataSource(private val context: Context) {
    private val database: MaiPlanDatabase by lazy {
        MaiPlanDatabase.getDatabase(context)
    }

    private val reminderDao: ReminderDAO by lazy {
        database.reminderDAO()
    }

    private val eventDao: EventDAO by lazy {
        database.eventDAO()
    }

    @Transaction
    suspend fun createEventWithReminder(
        reminder: ReminderEntity?,
        event: EventEntity
    ) {
        val reminderId: Int? = reminder?.let {
            reminderDao.reminderInsert(it).toInt()
        }

        eventDao.eventInsert(event.copy(reminderId = reminderId))
    }
}