package com.example.maiplan.database.entities

import com.example.maiplan.utils.common.OutboxStatus
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import java.time.Instant
import java.util.UUID

@Entity(
    tableName = "outbox",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["user_local_id"],
            childColumns = ["user_local_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["mutation_id"], unique = true),
        Index(value = ["user_local_id", "status", "created_at"]),
        Index(value = ["entity_type", "entity_sync_id"])
    ]
)
data class OutboxEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "outbox_local_id")
    val outboxLocalId: Long = 0L,

    @ColumnInfo(name = "mutation_id")
    val mutationId: UUID,

    @ColumnInfo(name = "user_local_id")
    val userLocalId: Long,

    @ColumnInfo(name = "entity_type")
    val entityType: String,

    @ColumnInfo(name = "entity_sync_id")
    val entitySyncId: UUID,

    val operation: String,

    @ColumnInfo(name = "base_version")
    val baseVersion: Long? = null,

    @ColumnInfo(name = "payload_json")
    val payloadJson: String? = null,

    val status: String = OutboxStatus.PENDING,

    @ColumnInfo(name = "created_at")
    val createdAt: Instant = Instant.now(),

    @ColumnInfo(name = "last_attempt_at")
    val lastAttemptAt: Instant? = null,

    @ColumnInfo(name = "attempt_count")
    val attemptCount: Int = 0,
    @ColumnInfo(name = "dependency_priority") val dependencyPriority: Int = 0,

    @ColumnInfo(name = "last_error")
    val lastError: String? = null,

    @ColumnInfo(name = "conflict_server_version")
    val conflictServerVersion: Long? = null,

    @ColumnInfo(name = "conflict_server_data_json")
    val conflictServerDataJson: String? = null
)
