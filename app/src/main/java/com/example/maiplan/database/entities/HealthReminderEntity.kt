package com.example.maiplan.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "health_reminder",
    foreignKeys = [
        ForeignKey(
            entity = AuthEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["user_id"], name = "idx_health_reminder_user")
    ]
)
data class HealthReminderEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "reminder_id")
    val reminderId: Int = 0,

    @ColumnInfo(name = "user_id")
    val userId: Int,

    val type: Int,

    @ColumnInfo(name = "start_time")
    val startTime: Long,

    @ColumnInfo(name = "end_time")
    val endTime: Long,

    val frequency: Int,

    @ColumnInfo(name = "last_modified")
    val lastModified: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "sync_state")
    val syncState: Int = 0,

    @ColumnInfo(name = "is_deleted")
    val isDeleted: Int = 0,

    @ColumnInfo(name = "server_id")
    val serverId: Int? = null
)
