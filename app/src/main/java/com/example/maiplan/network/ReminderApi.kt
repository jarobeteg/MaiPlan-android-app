package com.example.maiplan.network

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.time.LocalDateTime

/**
 * Data model for creating a new reminder.
 *
 * @property reminderId The unique ID for the reminder.
 * @property userId The ID of the user setting the reminder.
 * @property reminderTime The exact date and time the reminder should trigger.
 * @property frequency How often the reminder repeats (0 - one-time, 1 - daily, etc.).
 * @property status The status of the reminder (0 - inactive, 1 - active).
 * @property message The message or note associated with the reminder.
 */
data class ReminderCreate(
    @SerializedName("reminder_id") val reminderId: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("reminder_time") val reminderTime: LocalDateTime,
    val frequency: Int,
    val status: Int,
    val message: String,
)

/**
 * Data model representing a reminder fetched from the server.
 *
 * @property reminderId The unique ID of the reminder.
 * @property reminderTime The exact date and time the reminder is scheduled for.
 * @property frequency The frequency setting of the reminder.
 * @property status The current status of the reminder.
 * @property message The associated message of the reminder.
 */
data class ReminderResponse(
    @SerializedName("reminder_id") val reminderId: Int,
    @SerializedName("reminder_time") val reminderTime: LocalDateTime,
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
    suspend fun createReminder(@Body reminderCreate: ReminderCreate): Response<Unit>

    /**
     * Retrieves a single reminder by its ID.
     *
     * @param reminderId The ID of the reminder to fetch.
     * @return A [Response] containing a [ReminderResponse].
     */
    @GET("reminders/get-reminder-by-id")
    suspend fun getReminder(@Query("reminder_id") reminderId: Int): Response<ReminderResponse>
}