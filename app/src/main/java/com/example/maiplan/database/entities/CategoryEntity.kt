package com.example.maiplan.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.maiplan.network.api.CategorySync

@Entity(
    tableName = "category",
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
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "category_id")
    val categoryId: Int = 0,

    @ColumnInfo(name = "user_id")
    val userId: Int,

    val name: String,

    val description: String,

    val color: String,

    val icon: String,

    @ColumnInfo(name = "last_modified")
    val lastModified: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "sync_state")
    val syncState: Int = 0,

    @ColumnInfo(name = "is_deleted")
    val isDeleted: Int = 0,

    @ColumnInfo(name = "server_id")
    val serverId: Int? = null
)

fun CategoryEntity.toCategorySync(): CategorySync {
    return CategorySync(
        categoryId = this.categoryId,
        serverId = this.serverId ?: 0,
        userId = this.userId,
        name = this.name,
        description = this.description,
        color = this.color,
        icon = this.icon,
        lastModified = this.lastModified,
        syncState = this.syncState,
        isDeleted = this.isDeleted
    )
}

fun CategorySync.toCategoryEntity(): CategoryEntity {
    return CategoryEntity(
        categoryId = this.categoryId,
        userId = this.userId,
        name = this.name,
        description = this.description,
        color = this.color,
        icon = this.color,
        lastModified = this.lastModified,
        syncState = this.syncState,
        isDeleted = this.isDeleted,
        serverId = this.serverId
    )
}