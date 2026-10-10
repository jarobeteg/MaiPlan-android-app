package com.example.maiplan.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Entity(
    tableName = "task",
    foreignKeys = [
        ForeignKey(entity = UserEntity::class, parentColumns = ["user_local_id"], childColumns = ["user_local_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = CategoryEntity::class, parentColumns = ["category_local_id"], childColumns = ["category_local_id"], onDelete = ForeignKey.SET_NULL),
        ForeignKey(entity = ReminderEntity::class, parentColumns = ["reminder_local_id"], childColumns = ["reminder_local_id"], onDelete = ForeignKey.SET_NULL),
    ],
    indices = [
        Index(value = ["sync_id"], unique = true),
        Index(value = ["user_local_id", "scheduled_date", "status"]),
        Index(value = ["user_local_id", "status", "scheduled_date"]),
        Index(value = ["category_local_id"]),
        Index(value = ["reminder_local_id"]),
        Index(value = ["user_local_id", "series_id", "slot_date"], unique = true),
    ],
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "task_local_id") val taskLocalId: Long = 0L,
    @ColumnInfo(name = "user_local_id") val userLocalId: Long,
    val title: String,
    val description: String? = null,
    @ColumnInfo(name = "search_text") val searchText: String = taskSearchText(title, description),
    val status: Int = 0,
    @ColumnInfo(name = "scheduled_date") val scheduledDate: LocalDate? = null,
    @ColumnInfo(name = "estimated_time") val estimatedMilliseconds: Long? = null,
    @ColumnInfo(name = "completed_date") val completedDate: LocalDate? = null,
    @ColumnInfo(name = "category_local_id") val categoryLocalId: Long? = null,
    @ColumnInfo(name = "reminder_local_id") val reminderLocalId: Long? = null,
    @ColumnInfo(name = "series_id") val seriesId: String? = null,
    @ColumnInfo(name = "occurrence_number") val occurrenceNumber: Int? = null,
    @ColumnInfo(name = "slot_date") val slotDate: LocalDate? = null,
    @ColumnInfo(name = "generation_revision") val generationRevision: Int? = null,
    @ColumnInfo(name = "occurrence_override") val occurrenceOverride: Boolean = false,
    @ColumnInfo(name = "relative_reminder_json") val relativeReminderJson: String? = null,
    @ColumnInfo(name = "repeat_unit") val repeatUnit: Int? = null,
    @ColumnInfo(name = "repeat_interval") val repeatInterval: Int? = null,
    @ColumnInfo(name = "repeat_weekdays") val repeatWeekdays: Int? = null,
    @ColumnInfo(name = "repeat_end_date") val repeatEndDate: LocalDate? = null,
    @ColumnInfo(name = "repeat_anchor_date") val repeatAnchorDate: LocalDate? = null,
    @ColumnInfo(name = "sync_id") val syncId: UUID = UUID.randomUUID(),
    @ColumnInfo(name = "server_version") val serverVersion: Long? = null,
    @ColumnInfo(name = "created_at") val createdAt: Instant = Instant.now(),
    @ColumnInfo(name = "updated_at") val updatedAt: Instant = Instant.now(),
    @ColumnInfo(name = "deleted_at") val deletedAt: Instant? = null,
)

internal fun taskSearchText(title: String, description: String?) =
    (title + "\n" + description.orEmpty()).lowercase(java.util.Locale.ROOT)
