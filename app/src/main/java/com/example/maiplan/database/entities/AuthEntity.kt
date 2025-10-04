package com.example.maiplan.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.maiplan.network.api.AuthSync

@Entity(
    tableName = "auth",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["username"], unique = true),
        Index(value = ["last_modified"]),
        Index(value = ["sync_state"]),
        Index(value = ["server_id"])
    ]
)
data class AuthEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "user_id")
    val userId: Int,

    val email: String,

    val username: String,

    val balance: Float = 0.00f,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "password_hash")
    val passwordHash: String,

    @ColumnInfo(name = "last_modified")
    val lastModified: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "sync_state")
    val syncState: Int = 0,

    @ColumnInfo(name = "is_deleted")
    val isDeleted: Int = 0,

    @ColumnInfo(name = "server_id")
    val serverId: Int? = null
)

fun AuthEntity.toAuthSync(): AuthSync {
    return AuthSync(
        userId = this.userId,
        serverId = this.serverId ?: 0,
        email = this.email,
        username = this.username,
        balance = this.balance,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        passwordHash = this.passwordHash,
        lastModified = this.lastModified,
        syncState = this.syncState,
        isDeleted = this.isDeleted
    )
}

fun AuthSync.toAuthEntity(): AuthEntity {
    return AuthEntity(
        userId = this.userId,
        email = this.email,
        username = this.username,
        balance = this.balance,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        passwordHash = this.passwordHash,
        lastModified = this.lastModified,
        syncState = this.syncState,
        isDeleted = this.isDeleted,
        serverId = this.serverId
    )
}