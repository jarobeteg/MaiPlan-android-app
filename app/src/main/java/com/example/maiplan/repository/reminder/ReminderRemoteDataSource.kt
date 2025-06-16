package com.example.maiplan.repository.reminder

import com.example.maiplan.network.api.ReminderApi
import com.example.maiplan.network.api.ReminderCreate
import com.example.maiplan.network.api.ReminderResponse
import retrofit2.Response

/**
 * Remote data source responsible for performing reminder-related network requests via [ReminderApi].
 *
 * Provides suspend functions that make network calls and return raw [Response] objects for the repository to handle.
 * This class isolates Retrofit-related logic from the rest of the application.
 *
 * @property reminderApi An instance of [ReminderApi] used to execute API calls for reminders.
 *
 * @see ReminderApi
 */
class ReminderRemoteDataSource(private val reminderApi: ReminderApi) {

    /**
     * Sends a request to create a new reminder.
     *
     * @param reminder A [ReminderCreate] object containing reminder details such as time, title, etc.
     * @return A raw [Response] indicating success or failure of the creation operation.
     */
    suspend fun createReminder(reminder: ReminderCreate): Response<Unit> {
        return reminderApi.createReminder(reminder)
    }

    /**
     * Fetches the details of a specific reminder by its ID.
     *
     * @param reminderId The unique identifier of the reminder.
     * @return A raw [Response] containing a [ReminderResponse] on success or an error body on failure.
     */
    suspend fun getReminder(reminderId: Int): Response<ReminderResponse> {
        return reminderApi.getReminder(reminderId)
    }
}