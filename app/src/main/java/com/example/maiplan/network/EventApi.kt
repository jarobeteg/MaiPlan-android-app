package com.example.maiplan.network

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.time.LocalDate
import java.time.LocalTime

/**
 * Data model for creating a new event.
 *
 * @property eventId The unique Id for the event.
 * @property userId The Id of the user creating the event.
 * @property categoryId The Id of the category the event belongs to.
 * @property reminderId The Id of an associated reminder.
 * @property title The title of the event.
 * @property description A description of the event.
 * @property date The date of the event.
 * @property startTime The starting time of the event.
 * @property endTime The ending time of the event.
 * @property priority The priority level of the event (0 - low, 1 - medium, 2 - high).
 * @property location The location where the event takes place.
 */
data class EventCreate(
    @SerializedName("event_id") val eventId: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("category_id") val categoryId: Int,
    @SerializedName("reminder_id") val reminderId: Int,
    val title: String,
    val description: String,
    val date: LocalDate,
    @SerializedName("start_time") val startTime: LocalTime,
    @SerializedName("end_time") val endTime: LocalTime,
    val priority: Int,
    val location: String
)

/**
 * Data model representing an event fetched from the server.
 *
 * @property eventId The unique ID of the event.
 * @property categoryId The ID of the category the event belongs to.
 * @property reminderId The ID of the associated reminder.
 * @property title The title of the event.
 * @property description A description of the event.
 * @property date The date of the event.
 * @property startTime The starting time of the event.
 * @property endTime The ending time of the event.
 * @property priority The priority level of the event.
 * @property location The location where the event takes place.
 */
data class EventResponse(
    @SerializedName("event_id") val eventId: Int,
    @SerializedName("category_id") val categoryId: Int,
    @SerializedName("reminder_id") val reminderId: Int,
    val title: String,
    val description: String,
    val date: LocalDate,
    @SerializedName("start_time") val startTime: LocalTime,
    @SerializedName("end_time") val endTime: LocalTime,
    val priority: Int,
    val location: String
)

/**
 * API interface defining event-related endpoints.
 */
interface EventApi {

    /**
     * Creates a new event.
     *
     * @param eventCreate The [EventCreate] request body.
     * @return A [Response] indicating success or failure.
     */
    @POST("events/create-event")
    suspend fun createEvent(@Body eventCreate: EventCreate): Response<Unit>

    /**
     * Retrieves a single event by its ID.
     *
     * @param eventId The ID of the event to fetch.
     * @return A [Response] containing an [EventResponse].
     */
    @GET("events/get-event-by-id")
    suspend fun getEvent(@Query("event_id") eventId: Int): Response<EventResponse>
}