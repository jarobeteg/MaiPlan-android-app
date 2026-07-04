package com.example.maiplan.network.api

import com.example.maiplan.network.sync.SyncRequest
import com.example.maiplan.network.sync.SyncResponse
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class NoteSync(
    @SerializedName("note_id") val noteId: Int,
    @SerializedName("server_id") val serverId: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("category_id") val categoryId: Int,
    @SerializedName("reminder_id") val reminderId: Int,
    val title: String,
    val content: String,
    @SerializedName("created_at") val createdAt: Long,
    @SerializedName("updated_at") val updatedAt: Long,
    @SerializedName("last_modified") val lastModified: Long,
    @SerializedName("sync_state") val syncState: Int = 0,
    @SerializedName("is_deleted") val isDeleted: Int = 0
)

interface NoteApi {
    @POST("notes/sync")
    suspend fun noteSync(@Body request: SyncRequest<NoteSync>): Response<SyncResponse<NoteSync>>
}
