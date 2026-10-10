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
    tableName = "subtask",
    foreignKeys = [
        ForeignKey(entity = UserEntity::class, parentColumns = ["user_local_id"], childColumns = ["user_local_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = TaskEntity::class, parentColumns = ["task_local_id"], childColumns = ["task_local_id"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index(value = ["sync_id"], unique = true), Index(value = ["user_local_id"]), Index(value = ["task_local_id", "sort_order"])],
)
data class SubtaskEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "subtask_local_id") val subtaskLocalId: Long = 0L,
    @ColumnInfo(name = "user_local_id") val userLocalId: Long,
    @ColumnInfo(name = "task_local_id") val taskLocalId: Long,
    val title: String,
    val status: Int = 0,
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
    @ColumnInfo(name = "estimated_time") val estimatedMilliseconds: Long? = null,
    @ColumnInfo(name = "completed_date") val completedDate: LocalDate? = null,
    @ColumnInfo(name = "sync_id") val syncId: UUID = UUID.randomUUID(),
    @ColumnInfo(name = "server_version") val serverVersion: Long? = null,
    @ColumnInfo(name = "created_at") val createdAt: Instant = Instant.now(),
    @ColumnInfo(name = "updated_at") val updatedAt: Instant = Instant.now(),
    @ColumnInfo(name = "deleted_at") val deletedAt: Instant? = null,
)
