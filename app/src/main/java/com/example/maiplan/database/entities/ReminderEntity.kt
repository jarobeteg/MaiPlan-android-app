package com.example.maiplan.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reminder",
    foreignKeys = [
        ForeignKey(
            entity = AuthEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["user_id"], name = "idx_reminder_user")
    ]
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "reminder_id")
    val reminderId: Int = 0,

    @ColumnInfo(name = "user_id")
    val userId: Int,

    @ColumnInfo(name = "reminder_time")
    val reminderTime: Long,

    val frequency: Int = 0,

    val status: Int = 1,

    val message: String? = null,

    @ColumnInfo(name = "last_modified")
    val lastModified: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "sync_state")
    val syncState: Int = 0,

    @ColumnInfo(name = "is_deleted")
    val isDeleted: Int = 0,

    @ColumnInfo(name = "server_id")
    val serverId: Int? = null
)
