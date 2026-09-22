package com.example.maiplan.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.ZoneId
import java.util.UUID

@Entity(
    tableName = "event",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["user_local_id"],
            childColumns = ["user_local_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["category_local_id"],
            childColumns = ["category_local_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = ReminderEntity::class,
            parentColumns = ["reminder_local_id"],
            childColumns = ["reminder_local_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["sync_id"], unique = true),
        Index(value = ["user_local_id"]),
        Index(value = ["category_local_id"]),
        Index(value = ["reminder_local_id"]),
        Index(value = ["user_local_id", "date"])
    ]
)
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "event_local_id")
    val eventLocalId: Long = 0L,

    @ColumnInfo(name = "user_local_id")
    val userLocalId: Long,

    @ColumnInfo(name = "category_local_id")
    val categoryLocalId: Long? = null,

    @ColumnInfo(name = "reminder_local_id")
    val reminderLocalId: Long? = null,

    val title: String,

    val description: String? = null,

    val date: Long,

    @ColumnInfo(name = "start_time")
    val startTime: Long? = null,

    @ColumnInfo(name = "end_time")
    val endTime: Long? = null,

    @ColumnInfo(name = "zone_id")
    val zoneId: String = ZoneId.systemDefault().id,

    val priority: Int = 0,

    val location: String? = null,

    @ColumnInfo(name = "sync_id")
    val syncId: UUID = UUID.randomUUID(),

    @ColumnInfo(name = "server_version")
    val serverVersion: Long? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Instant = Instant.now(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant = Instant.now(),

    @ColumnInfo(name = "deleted_at")
    val deletedAt: Instant? = null
)
