package com.example.maiplan.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.maiplan.network.api.ReminderSync

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
        Index(value = ["user_id"]),
        Index(value = ["last_modified"]),
        Index(value = ["sync_state"]),
        Index(value = ["server_id"])
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

fun ReminderEntity.toReminderSync(): ReminderSync {
    return ReminderSync(
        reminderId = this.reminderId,
        serverId = this.serverId ?: 0,
        userId = this.userId,
        reminderTime = this.reminderTime,
        frequency = this.frequency,
        status = this.status,
        message = this.message ?: "",
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        lastModified = this.lastModified,
        syncState = this.syncState,
        isDeleted = this.isDeleted
    )
}

fun ReminderSync.toReminderEntity(): ReminderEntity {
    return ReminderEntity(
        reminderId = this.reminderId,
        serverId = this.serverId,
        userId = this.userId,
        reminderTime = this.reminderTime,
        frequency = this.frequency,
        status = this.status,
        message = this.message,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        lastModified = this.lastModified,
        syncState = this.syncState,
        isDeleted = this.isDeleted
    )
}
