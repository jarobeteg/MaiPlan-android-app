package com.example.maiplan.repository.reminder

import com.example.maiplan.database.entities.ReminderEntity
import com.example.maiplan.database.entities.toReminderEntity
import com.example.maiplan.database.entities.toReminderSync
import com.example.maiplan.network.api.ReminderCreate
import com.example.maiplan.network.api.ReminderResponse
import com.example.maiplan.network.api.ReminderSync
import com.example.maiplan.network.sync.SyncRequest
import com.example.maiplan.network.sync.Syncable
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.handleRemoteResponse
import com.example.maiplan.utils.common.UserSession

class ReminderRepository(
    private val remote: ReminderRemoteDataSource,
    private val local: ReminderLocalDataSource
) : Syncable {

    override suspend fun sync() {
        try {
            val pendingRemindersResult = local.getPendingReminders(UserSession.userId!!)
            if (pendingRemindersResult is Result.Success) {
                val reminders: List<ReminderEntity> = pendingRemindersResult.data
                val changes: MutableList<ReminderSync> = mutableListOf()
                reminders.map { changes.add(it.toReminderSync()) }
                val request: SyncRequest<ReminderSync> = SyncRequest(UserSession.userId!!, changes)
                val response = remote.reminderSync(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        if (body.acknowledged.isNotEmpty()) body.acknowledged.map { local.reminderUpsert(it.toReminderEntity()) }
                        if (body.rejected.isNotEmpty()) body.rejected.map { local.deleteReminder(it.toReminderEntity()) }
                    }
                }
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun createReminder(reminder: ReminderCreate): Result<Int> {
        return try {
            handleRemoteResponse(remote.createReminder(reminder))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getReminder(reminderId: Int): Result<ReminderResponse> {
        return try {
            handleRemoteResponse(remote.getReminder(reminderId))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getAllReminders(userId: Int): Result<List<ReminderResponse>> {
        return try {
            handleRemoteResponse(remote.getAllReminders(userId))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}