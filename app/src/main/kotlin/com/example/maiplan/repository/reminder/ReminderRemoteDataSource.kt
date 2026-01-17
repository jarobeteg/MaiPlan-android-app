package com.example.maiplan.repository.reminder

import com.example.maiplan.network.api.ReminderApi
import com.example.maiplan.network.api.ReminderCreate
import com.example.maiplan.network.api.ReminderResponse
import com.example.maiplan.network.api.ReminderSync
import com.example.maiplan.network.sync.SyncRequest
import com.example.maiplan.network.sync.SyncResponse
import retrofit2.Response

class ReminderRemoteDataSource(private val reminderApi: ReminderApi) {
    suspend fun createReminder(reminder: ReminderCreate): Response<Int> {
        return reminderApi.createReminder(reminder)
    }
    suspend fun getReminder(reminderId: Int): Response<ReminderResponse> {
        return reminderApi.getReminder(reminderId)
    }

    suspend fun getAllReminders(userId: Int): Response<List<ReminderResponse>> {
        return reminderApi.getAllReminders(userId)
    }

    suspend fun reminderSync(request: SyncRequest<ReminderSync>): Response<SyncResponse<ReminderSync>> {
        return reminderApi.reminderSync(request)
    }
}