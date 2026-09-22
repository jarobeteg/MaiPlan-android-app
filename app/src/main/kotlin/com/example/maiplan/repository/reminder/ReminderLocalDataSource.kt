package com.example.maiplan.repository.reminder

import android.content.Context
import androidx.room.withTransaction
import com.example.maiplan.database.MaiPlanDatabase
import com.example.maiplan.database.entities.ReminderEntity
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.handleLocalResponse

class ReminderLocalDataSource(context: Context) {
    private val database: MaiPlanDatabase by lazy {
        MaiPlanDatabase.getDatabase(context.applicationContext)
    }
    private val reminderDao by lazy { database.reminderDAO() }
    private val mutationWriter by lazy { ReminderMutationWriter(database) }

    suspend fun createReminder(reminder: ReminderEntity): Result<ReminderEntity> {
        return handleLocalResponse {
            database.withTransaction {
                mutationWriter.create(reminder, reminder.userLocalId)
            }
        }
    }

    suspend fun updateReminder(reminder: ReminderEntity): Result<ReminderEntity> {
        return handleLocalResponse {
            database.withTransaction {
                mutationWriter.update(reminder, reminder.userLocalId)
            }
        }
    }

    suspend fun softDeleteReminder(
        reminderLocalId: Long,
        userLocalId: Long
    ): Result<Unit> {
        return handleLocalResponse {
            database.withTransaction {
                mutationWriter.delete(reminderLocalId, userLocalId)
                Unit
            }
        }
    }

    suspend fun getReminder(
        reminderLocalId: Long,
        userLocalId: Long
    ): ReminderEntity? {
        return reminderDao.getReminderByLocalId(reminderLocalId, userLocalId)
    }
}
