package com.example.maiplan.network.api

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

data class EventCreate(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("category_id") val categoryId: Int?,
    @SerializedName("reminder_id") val reminderId: Int?,
    val title: String,
    val description: String,
    val date: String,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("end_time") val endTime: String,
    val priority: Int,
    val location: String
)

data class EventResponse(
    @SerializedName("event_id") val eventId: Int,
    @SerializedName("category_id") val categoryId: Int,
    @SerializedName("reminder_id") val reminderId: Int,
    val title: String,
    val description: String,
    val date: String,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("end_time") val endTime: String,
    val priority: Int,
    val location: String
)

interface EventApi {
    @POST("events/create-event")
    suspend fun createEvent(@Body eventCreate: EventCreate): Response<Unit>

    @GET("events/get-event-by-id")
    suspend fun getEvent(@Query("event_id") eventId: Int): Response<EventResponse>

    @GET("events/get-all-event")
    suspend fun getAllEvents(@Query("user_id") userId: Int): Response<List<EventResponse>>
}