package com.example.maiplan.repository

import com.example.maiplan.network.ReminderApi
import com.example.maiplan.network.ReminderCreate
import com.example.maiplan.network.ReminderResponse

/**
 * Repository responsible for handling reminder-related network operations.
 *
 * Wraps API calls and returns a [Result] indicating success or failure,
 * abstracting network and error handling from the rest of the app.
 *
 * @property reminderApi An instance of [ReminderApi] for making reminder network requests.
 *
 * @see ReminderApi
 * @see Result
 */
class ReminderRepository(private val reminderApi: ReminderApi) {

    /**
     * Creates a new reminder.
     *
     * @param reminder The [ReminderCreate] object containing reminder data.
     * @return A [Result] indicating success or failure.
     *
     * @see Result
     * @see ReminderCreate
     * @see handleResponse
     */
    suspend fun createReminder(reminder: ReminderCreate): Result<Unit> {
        return try {
            handleResponse(reminderApi.createReminder(reminder))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Retrieves a specific reminder by its ID.
     *
     * @param reminderId The ID of the reminder.
     * @return A [Result] containing a [ReminderResponse] or an error.
     *
     * @see Result
     * @see ReminderResponse
     * @see handleResponse
     */
    suspend fun getReminder(reminderId: Int): Result<ReminderResponse> {
        return try {
            handleResponse(reminderApi.getReminder(reminderId))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}