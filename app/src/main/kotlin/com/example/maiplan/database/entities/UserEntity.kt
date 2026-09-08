package com.example.maiplan.database.entities

import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import java.time.Instant
import java.util.UUID

@Entity(
    tableName = "user",
    indices = [
        Index(value = ["sync_id"], unique = true),
        Index(value = ["email"], unique = true)
    ]
)

data class UserEntity (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "user_local_id")
    val userLocalId: Long = 0L,

    val email: String,

    val username: String,

    @ColumnInfo(name = "sync_id")
    val syncId: UUID,

    @ColumnInfo(name = "server_version")
    val serverVersion: Long? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Instant = Instant.now(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant = Instant.now(),

    @ColumnInfo(name = "deleted_at")
    val deletedAt: Instant? = null
)