package com.example.maiplan.network

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.time.LocalDateTime

data class ReminderCreate(
    @SerializedName("reminder_id") val reminderId: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("reminder_time") val reminderTime: LocalDateTime,
    val frequency: Int,
    val status: Int,
    val message: String,
)
data class ReminderResponse(
    @SerializedName("reminder_id") val reminderId: Int,
    @SerializedName("reminder_time") val reminderTime: LocalDateTime,
    val frequency: Int,
    val status: Int,
    val message: String,
)

interface ReminderApi {
    @POST("reminders/create-reminder")
    suspend fun createReminder(@Body reminderCreate: ReminderCreate): Response<Unit>

    @GET("reminders/get-reminder-by-id")
    suspend fun getReminder(@Query("reminder_id") reminderId: Int): Response<ReminderResponse>
}