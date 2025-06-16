package com.example.maiplan.repository.reminder

import com.example.maiplan.network.api.ReminderApi
import com.example.maiplan.network.api.ReminderCreate
import com.example.maiplan.network.api.ReminderResponse
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.handleResponse

/**
 * Repository responsible for handling reminder-related network operations.
 *
 * Wraps API calls and returns a [Result] indicating success or failure,
 * abstracting network and error handling from the rest of the app.
 *
 * @property remoteDataSource An instance of [ReminderRemoteDataSource] used to perform reminder-related API calls.
 *
 * @see ReminderApi
 * @see Result
 * @see handleResponse
 */
class ReminderRepository(private val remoteDataSource: ReminderRemoteDataSource) {

    /**
     * Creates a new reminder.
     *
     * @param reminder The [ReminderCreate] object containing reminder data like time, label, and repeat settings.
     * @return A [Result] indicating success (with `Unit`) or failure (with an [Exception]).
     */
    suspend fun createReminder(reminder: ReminderCreate): Result<Unit> {
        return try {
            handleResponse(remoteDataSource.createReminder(reminder))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }


    /**
     * Retrieves a specific reminder by its ID.
     *
     * @param reminderId The ID of the reminder to retrieve.
     * @return A [Result] containing a [ReminderResponse] on success, or an [Exception] on failure.
     */
    suspend fun getReminder(reminderId: Int): Result<ReminderResponse> {
        return try {
            handleResponse(remoteDataSource.getReminder(reminderId))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}