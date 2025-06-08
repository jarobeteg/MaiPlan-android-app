package com.example.maiplan.repository.reminder

import com.example.maiplan.network.api.ReminderApi
import com.example.maiplan.network.api.ReminderCreate
import com.example.maiplan.network.api.ReminderResponse
import retrofit2.Response

class ReminderRemoteDataSource(private val reminderApi: ReminderApi) {

    suspend fun createReminder(reminder: ReminderCreate): Response<Unit> {
        return reminderApi.createReminder(reminder)
    }

    suspend fun getReminder(reminderId: Int): Response<ReminderResponse> {
        return reminderApi.getReminder(reminderId)
    }
}