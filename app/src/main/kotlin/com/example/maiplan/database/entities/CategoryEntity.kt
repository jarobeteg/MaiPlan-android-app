package com.example.maiplan.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.util.UUID

@Entity(
    tableName = "category",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["user_local_id"],
            childColumns = ["user_local_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["sync_id"], unique = true),
        Index(value = ["user_local_id"])
    ]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "category_local_id")
    val categoryLocalId: Long = 0L,

    @ColumnInfo(name = "user_local_id")
    val userLocalId: Long,

    val name: String,

    val description: String = "",

    val color: String,

    val icon: String,

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
