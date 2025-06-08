package com.example.maiplan.repository.reminder

import com.example.maiplan.network.api.ReminderApi
import com.example.maiplan.network.api.ReminderCreate
import com.example.maiplan.network.api.ReminderResponse
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.handleResponse

/**
 * Repository responsible for handling reminder-related network operations.
 *
 * Wraps API calls and returns a [com.example.maiplan.repository.Result] indicating success or failure,
 * abstracting network and error handling from the rest of the app.
 *
 * @property reminderApi An instance of [com.example.maiplan.network.api.ReminderApi] for making reminder network requests.
 *
 * @see com.example.maiplan.network.api.ReminderApi
 * @see com.example.maiplan.repository.Result
 */
class ReminderRepository(private val remoteDataSource: ReminderRemoteDataSource) {

    /**
     * Creates a new reminder.
     *
     * @param reminder The [com.example.maiplan.network.api.ReminderCreate] object containing reminder data.
     * @return A [com.example.maiplan.repository.Result] indicating success or failure.
     *
     * @see com.example.maiplan.repository.Result
     * @see com.example.maiplan.network.api.ReminderCreate
     * @see com.example.maiplan.repository.handleResponse
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
     * @param reminderId The ID of the reminder.
     * @return A [Result] containing a [com.example.maiplan.network.api.ReminderResponse] or an error.
     *
     * @see Result
     * @see com.example.maiplan.network.api.ReminderResponse
     * @see handleResponse
     */
    suspend fun getReminder(reminderId: Int): Result<ReminderResponse> {
        return try {
            handleResponse(remoteDataSource.getReminder(reminderId))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}