package com.example.maiplan.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "scheduled_reminder", indices = [Index("sourceKey"), Index("userLocalId")])
data class ScheduledReminderEntity(
    @PrimaryKey val alarmKey: String,
    val sourceKey: String,
    val userLocalId: Long,
    val eventLocalId: Long? = null,
    val noteLocalId: Long? = null,
    val occurrenceDate: String? = null,
    val triggerAt: Long,
    val title: String,
    val message: String,
    val deliveredAt: Long? = null,
    val scheduledAt: Long? = null,
    val lastError: String? = null,
)
