package com.example.maiplan.network.api

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

data class ReminderCreate(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("reminder_time") val reminderTime: String,
    val frequency: Int,
    val status: Int,
    val message: String,
)

data class ReminderResponse(
    @SerializedName("reminder_id") val reminderId: Int,
    @SerializedName("reminder_time") val reminderTime: String,
    val frequency: Int,
    val status: Int,
    val message: String,
)

interface ReminderApi {
    @POST("reminders/create-reminder")
    suspend fun createReminder(@Body reminderCreate: ReminderCreate): Response<Int>

    @GET("reminders/get-reminder-by-id")
    suspend fun getReminder(@Query("reminder_id") reminderId: Int): Response<ReminderResponse>

    @GET("reminders/get-all-reminder")
    suspend fun getAllReminders(@Query("user_id") userId: Int): Response<List<ReminderResponse>>
}