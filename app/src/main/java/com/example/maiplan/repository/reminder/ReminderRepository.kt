package com.example.maiplan.repository.reminder

import com.example.maiplan.network.api.ReminderCreate
import com.example.maiplan.network.api.ReminderResponse
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.handleRemoteResponse

class ReminderRepository(private val remoteDataSource: ReminderRemoteDataSource) {
    suspend fun createReminder(reminder: ReminderCreate): Result<Int> {
        return try {
            handleRemoteResponse(remoteDataSource.createReminder(reminder))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getReminder(reminderId: Int): Result<ReminderResponse> {
        return try {
            handleRemoteResponse(remoteDataSource.getReminder(reminderId))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getAllReminders(userId: Int): Result<List<ReminderResponse>> {
        return try {
            handleRemoteResponse(remoteDataSource.getAllReminders(userId))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}