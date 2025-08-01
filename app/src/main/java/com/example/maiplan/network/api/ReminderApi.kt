package com.example.maiplan.network.api

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Data model for creating a new reminder.
 *
 * @property userId The Id of the user setting the reminder.
 * @property reminderTime The exact date and time the reminder should trigger.
 * @property frequency How often the reminder repeats (0 - one-time, 1 - daily, etc.).
 * @property status The status of the reminder (0 - inactive, 1 - active).
 * @property message The message or note associated with the reminder.
 */
data class ReminderCreate(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("reminder_time") val reminderTime: String,
    val frequency: Int,
    val status: Int,
    val message: String,
)

/**
 * Data model representing a reminder fetched from the server.
 *
 * @property reminderId The unique Id of the reminder.
 * @property reminderTime The exact date and time the reminder is scheduled for.
 * @property frequency The frequency setting of the reminder.
 * @property status The current status of the reminder.
 * @property message The associated message of the reminder.
 */
data class ReminderResponse(
    @SerializedName("reminder_id") val reminderId: Int,
    @SerializedName("reminder_time") val reminderTime: String,
    val frequency: Int,
    val status: Int,
    val message: String,
)

/**
 * API interface defining reminder-related endpoints.
 */
interface ReminderApi {

    /**
     * Creates a new reminder.
     *
     * @param reminderCreate The [ReminderCreate] request body.
     * @return A [Response] indicating success or failure.
     */
    @POST("reminders/create-reminder")
    suspend fun createReminder(@Body reminderCreate: ReminderCreate): Response<Int>

    /**
     * Retrieves a single reminder by its ID.
     *
     * @param reminderId The ID of the reminder to fetch.
     * @return A [Response] containing a [ReminderResponse].
     */
    @GET("reminders/get-reminder-by-id")
    suspend fun getReminder(@Query("reminder_id") reminderId: Int): Response<ReminderResponse>

    /**
     * Fetches all reminders for a specific user.
     *
     * @param userId The Id of the user whose reminders to fetch.
     * @return A [Response] containing a list of [ReminderResponse] items.
     */
    @GET("reminders/get-all-reminder")
    suspend fun getAllReminders(@Query("user_id") userId: Int): Response<List<ReminderResponse>>
}