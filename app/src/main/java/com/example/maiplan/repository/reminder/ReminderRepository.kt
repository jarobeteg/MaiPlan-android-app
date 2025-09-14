package com.example.maiplan.repository.reminder

import com.example.maiplan.network.api.ReminderCreate
import com.example.maiplan.network.api.ReminderResponse
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.handleResponse

class ReminderRepository(private val remoteDataSource: ReminderRemoteDataSource) {
    suspend fun createReminder(reminder: ReminderCreate): Result<Int> {
        return try {
            handleResponse(remoteDataSource.createReminder(reminder))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getReminder(reminderId: Int): Result<ReminderResponse> {
        return try {
            handleResponse(remoteDataSource.getReminder(reminderId))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getAllReminders(userId: Int): Result<List<ReminderResponse>> {
        return try {
            handleResponse(remoteDataSource.getAllReminders(userId))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}