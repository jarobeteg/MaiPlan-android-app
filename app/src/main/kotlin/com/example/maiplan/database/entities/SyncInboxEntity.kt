package com.example.maiplan.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import java.util.UUID

@Entity(tableName = "sync_inbox", primaryKeys = ["user_local_id", "entity_type", "entity_sync_id"],
    foreignKeys = [ForeignKey(entity = UserEntity::class, parentColumns = ["user_local_id"],
        childColumns = ["user_local_id"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["user_local_id", "applied"])])
data class SyncInboxEntity(
    @ColumnInfo(name = "user_local_id") val userLocalId: Long,
    @ColumnInfo(name = "entity_type") val entityType: String,
    @ColumnInfo(name = "entity_sync_id") val entitySyncId: UUID,
    @ColumnInfo(name = "server_version") val serverVersion: Long,
    val operation: String,
    val sequence: Long,
    @ColumnInfo(name = "data_json") val dataJson: String?,
    val applied: Boolean = false,
)
