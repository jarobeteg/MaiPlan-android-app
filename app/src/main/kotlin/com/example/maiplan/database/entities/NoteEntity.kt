package com.example.maiplan.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "note",
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
        Index(value = ["last_modified"]),
        Index(value = ["sync_state"]),
        Index(value = ["server_id"])
    ]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "note_id")
    val noteId: Int = 0,

    @ColumnInfo(name = "user_local_id")
    val userLocalId: Long,

    @ColumnInfo(name = "category_id")
    val categoryId: Int? = null,

    @ColumnInfo(name = "reminder_id")
    val reminderId: Int? = null,

    val title: String,

    val content: String? = null,

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
    val serverId: Int? = null,

    @ColumnInfo(name = "is_pinned")
    val isPinned: Int = 0
)
