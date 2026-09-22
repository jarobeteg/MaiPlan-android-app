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
    tableName = "reminder",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["user_local_id"],
            childColumns = ["user_local_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["sync_id"], unique = true),
        Index(value = ["user_local_id"])
    ]
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "reminder_local_id")
    val reminderLocalId: Long = 0L,

    @ColumnInfo(name = "user_local_id")
    val userLocalId: Long,

    @ColumnInfo(name = "reminder_time")
    val reminderTime: Long,

    @ColumnInfo(name = "zone_id")
    val zoneId: String = ZoneId.systemDefault().id,

    val frequency: Int = 0,

    val status: Int = 1,

    val message: String? = null,

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
