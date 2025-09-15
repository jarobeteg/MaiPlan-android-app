package com.example.maiplan.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "event",
    foreignKeys = [
        ForeignKey(
            entity = AuthEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["category_id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = ReminderEntity::class,
            parentColumns = ["reminder_id"],
            childColumns = ["reminder_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["user_id"], name = "idx_event_user"),
        Index(value = ["category_id"], name = "idx_event_category"),
        Index(value = ["reminder_id"], name = "idx_event_reminder"),
        Index(value = ["date"], name = "idx_event_date"),
        Index(value = ["user_id", "date"], name = "idx_event_user_date"),
    ]
)
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "event_id")
    val eventId: Int = 0,

    @ColumnInfo(name = "user_id")
    val userId: Int,

    @ColumnInfo(name = "category_id")
    val categoryId: Int? = null,

    @ColumnInfo(name = "reminder_id")
    val reminderId: Int? = null,

    val title: String,

    val description: String? = null,

    val date: Long,

    @ColumnInfo(name = "start_time")
    val startTime: Long? = null,

    @ColumnInfo(name = "end_time")
    val endTime: Long? = null,

    val priority: Int = 0,

    val location: String? = null,

    @ColumnInfo(name = "last_modified")
    val lastModified: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "sync_state")
    val syncState: Int = 0,

    @ColumnInfo(name = "is_deleted")
    val isDeleted: Int = 0,

    @ColumnInfo(name = "server_id")
    val serverId: Int? = null
)