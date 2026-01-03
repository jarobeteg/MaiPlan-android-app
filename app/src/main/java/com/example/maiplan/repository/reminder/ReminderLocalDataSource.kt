package com.example.maiplan.repository.reminder

import android.content.Context
import com.example.maiplan.database.MaiPlanDatabase
import com.example.maiplan.database.dao.ReminderDAO
import com.example.maiplan.database.entities.ReminderEntity
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.handleLocalResponse

class ReminderLocalDataSource(private val context: Context) {
    private val database: MaiPlanDatabase by lazy {
        MaiPlanDatabase.getDatabase(context)
    }

    private val reminderDao: ReminderDAO by lazy {
        database.reminderDAO()
    }

    suspend fun reminderUpsert(reminder: ReminderEntity): Result<Unit> {
        return handleLocalResponse {
            reminderDao.reminderUpsert(reminder)
        }
    }

    suspend fun deleteReminder(reminder: ReminderEntity): Result<Unit> {
        return handleLocalResponse {
            reminderDao.deleteReminder(reminder)
        }
    }

    suspend fun softDeleteReminder(reminderId: Int, userId: Int): Result<Unit> {
        return handleLocalResponse {
            reminderDao.softDeleteReminder(reminderId, userId)
        }
    }

    suspend fun getPendingReminders(userId: Int): Result<List<ReminderEntity>> {
        return handleLocalResponse {
            reminderDao.getPendingReminders(userId)
        }
    }
}