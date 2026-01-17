package com.example.maiplan.network.api

import com.example.maiplan.network.sync.SyncRequest
import com.example.maiplan.network.sync.SyncResponse
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

data class ReminderSync(
    @SerializedName("reminder_id") val reminderId: Int,
    @SerializedName("server_id") val serverId: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("reminder_time") val reminderTime: Long,
    val frequency: Int,
    val status: Int,
    val message: String,
    @SerializedName("created_at") val createdAt: Long,
    @SerializedName("updated_at") val updatedAt: Long,
    @SerializedName("last_modified") val lastModified: Long,
    @SerializedName("sync_state") val syncState: Int = 0,
    @SerializedName("is_deleted") val isDeleted: Int = 0
)

interface ReminderApi {
    @POST("reminders/create-reminder")
    suspend fun createReminder(@Body reminderCreate: ReminderCreate): Response<Int>

    @GET("reminders/get-reminder-by-id")
    suspend fun getReminder(@Query("reminder_id") reminderId: Int): Response<ReminderResponse>

    @GET("reminders/get-all-reminder")
    suspend fun getAllReminders(@Query("user_id") userId: Int): Response<List<ReminderResponse>>

    @POST("reminders/sync")
    suspend fun reminderSync(@Body request: SyncRequest<ReminderSync>): Response<SyncResponse<ReminderSync>>
}