package com.example.maiplan.database.entities

import androidx.room.*
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Entity(tableName = "task_series", indices = [Index(value = ["sync_id"], unique = true), Index("user_local_id")],
    foreignKeys = [ForeignKey(entity = UserEntity::class, parentColumns = ["user_local_id"], childColumns = ["user_local_id"], onDelete = ForeignKey.CASCADE)])
data class TaskSeriesEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "series_local_id") val seriesLocalId: Long = 0,
    @ColumnInfo(name = "user_local_id") val userLocalId: Long,
    @ColumnInfo(name = "definition_json") val definitionJson: String,
    @ColumnInfo(name = "pending_operation_json") val pendingOperationJson: String? = null,
    @ColumnInfo(name = "history_through") val historyThrough: LocalDate? = null,
    @ColumnInfo(name = "sync_id") val syncId: UUID = UUID.randomUUID(),
    @ColumnInfo(name = "server_version") val serverVersion: Long? = null,
    @ColumnInfo(name = "created_at") val createdAt: Instant = Instant.now(),
    @ColumnInfo(name = "updated_at") val updatedAt: Instant = Instant.now(),
    @ColumnInfo(name = "deleted_at") val deletedAt: Instant? = null,
)

@Entity(tableName = "task_exclusion", indices = [Index(value = ["user_local_id", "series_id", "slot_date"], unique = true)],
    foreignKeys = [ForeignKey(entity = UserEntity::class, parentColumns = ["user_local_id"], childColumns = ["user_local_id"], onDelete = ForeignKey.CASCADE)])
data class TaskExclusionEntity(
    @PrimaryKey @ColumnInfo(name = "sync_id") val syncId: UUID,
    @ColumnInfo(name = "user_local_id") val userLocalId: Long,
    @ColumnInfo(name = "series_id") val seriesId: UUID,
    @ColumnInfo(name = "slot_date") val slotDate: LocalDate,
    @ColumnInfo(name = "server_version") val serverVersion: Long? = null,
)
