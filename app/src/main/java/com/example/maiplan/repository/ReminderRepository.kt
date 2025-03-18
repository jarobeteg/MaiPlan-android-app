package com.example.maiplan.repository

import com.example.maiplan.network.ReminderApi
import com.example.maiplan.network.ReminderCreate
import com.example.maiplan.network.ReminderResponse

class ReminderRepository(private val reminderApi: ReminderApi) {
    suspend fun createReminder(reminder: ReminderCreate): Result<Unit> {
        return try {
            handleResponse(reminderApi.createReminder(reminder))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getReminder(reminderId: Int): Result<ReminderResponse> {
        return try {
            handleResponse(reminderApi.getReminder(reminderId))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}