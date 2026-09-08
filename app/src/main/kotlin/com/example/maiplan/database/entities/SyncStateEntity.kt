package com.example.maiplan.database.entities

import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Entity
import java.util.UUID

@Entity(
    tableName = "sync_state",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["sync_id"],
            childColumns = ["user_sync_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
    )
data class SyncStateEntity (
    @PrimaryKey
    @ColumnInfo(name = "user_sync_id")
    val userSyncId: UUID,

    val cursor: String? = null
)