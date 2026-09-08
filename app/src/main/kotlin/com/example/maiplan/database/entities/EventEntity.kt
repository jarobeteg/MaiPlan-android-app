package com.example.maiplan.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.maiplan.network.api.EventSync

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
        Index(value = ["user_local_id"]),
        Index(value = ["category_id"]),
        Index(value = ["reminder_id"]),
        Index(value = ["date"]),
        Index(value = ["user_local_id", "date"]),
        Index(value = ["last_modified"]),
        Index(value = ["sync_state"]),
        Index(value = ["server_id"])
    ]
)
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "event_id")
    val eventId: Int = 0,

    @ColumnInfo(name = "user_local_id")
    val userLocalId: Long,

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

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "last_modified")
    val lastModified: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "sync_state")
    val syncState: Int = 0,

    @ColumnInfo(name = "is_deleted")
    val isDeleted: Int = 0,

    @ColumnInfo(name = "server_id")
    val serverId: Int? = null
)

fun EventEntity.toEventSync(): EventSync {
    return EventSync(
        eventId = this.eventId,
        serverId = this.serverId ?: 0,
        userLocalId = this.userLocalId,
        categoryId = this.categoryId ?: 0,
        reminderId = this.reminderId ?: 0,
        title = this.title,
        description = this.description ?: "",
        date = this.date,
        startTime = this.startTime ?: 0,
        endTime = this.endTime ?: 0,
        priority = this.priority,
        location = this.location ?: "",
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        lastModified = this.lastModified,
        syncState = this.syncState,
        isDeleted = this.isDeleted
    )
}

fun EventSync.toEventEntity(): EventEntity {
    return EventEntity(
        eventId = this.eventId,
        serverId = this.serverId,
        userLocalId = this.userLocalId,
        categoryId = this.categoryId,
        reminderId = this.reminderId,
        title = this.title,
        description = this.description,
        date = this.date,
        startTime = this.startTime,
        endTime = this.endTime,
        priority = this.priority,
        location = this.location,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        lastModified = this.lastModified,
        syncState = this.syncState,
        isDeleted = this.isDeleted
    )
}