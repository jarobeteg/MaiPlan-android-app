package com.example.maiplan.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "scheduled_reminder", indices = [Index("sourceKey"), Index("userLocalId"), Index(value = ["userLocalId", "taskLocalId"])])
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
    val taskLocalId: Long? = null,
) {
    init {
        require(userLocalId > 0 && listOfNotNull(eventLocalId, noteLocalId, taskLocalId).size == 1) {
            "A reminder queue row must belong to exactly one source and one user"
        }
        val source = when {
            eventLocalId != null -> "event:$eventLocalId"
            noteLocalId != null -> "note:$noteLocalId"
            else -> "task:$taskLocalId"
        }
        require(listOfNotNull(eventLocalId, noteLocalId, taskLocalId).single() > 0 && sourceKey == source)
        require(occurrenceDate == null || eventLocalId != null)
    }
}
