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
    @SerializedName("start_date")
    val startDate: Long,
    @SerializedName("end_date")
    val endDate: Long,
    @SerializedName("start_time")
    val startTime: Long?,
    @SerializedName("end_time")
    val endTime: Long?,
    @SerializedName("recurrence_frequency")
    val recurrenceFrequency: String?,
    @SerializedName("recurrence_interval")
    val recurrenceInterval: Int?,
    @SerializedName("recurrence_weekdays")
    val recurrenceWeekdays: Int?,
    @SerializedName("recurrence_monthly_mode")
    val recurrenceMonthlyMode: String?,
    @SerializedName("recurrence_until_date")
    val recurrenceUntilDate: Long?,
    @SerializedName("reminder_offset_minutes")
    val reminderOffsetMinutes: Int?,
    @SerializedName("reminder_lead_days")
    val reminderLeadDays: Int?,
    @SerializedName("reminder_minute_of_day")
    val reminderMinuteOfDay: Int?,
    @SerializedName("relative_reminder_message")
    val relativeReminderMessage: String?,
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
