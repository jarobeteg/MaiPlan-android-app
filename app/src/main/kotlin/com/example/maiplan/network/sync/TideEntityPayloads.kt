package com.example.maiplan.network.sync

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class ReminderMutationPayload(
    @SerializedName("reminder_time")
    val reminderTime: Long,
    @SerializedName("zone_id")
    val zoneId: String,
    val frequency: Int,
    val status: Int,
    val message: String?
)

data class EventMutationPayload(
    @SerializedName("category_sync_id")
    val categorySyncId: UUID?,
    @SerializedName("reminder_sync_id")
    val reminderSyncId: UUID?,
    val title: String,
    val description: String?,
    val date: Long,
    @SerializedName("start_time")
    val startTime: Long?,
    @SerializedName("end_time")
    val endTime: Long?,
    @SerializedName("zone_id")
    val zoneId: String,
    val priority: Int,
    val location: String?
)

data class NoteMutationPayload(
    @SerializedName("category_sync_id")
    val categorySyncId: UUID?,
    @SerializedName("reminder_sync_id")
    val reminderSyncId: UUID?,
    val title: String,
    val content: String?,
    @SerializedName("is_pinned")
    val isPinned: Boolean
)
