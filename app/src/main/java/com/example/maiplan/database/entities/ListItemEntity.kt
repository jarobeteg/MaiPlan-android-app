package com.example.maiplan.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "list_item",
    foreignKeys = [
        ForeignKey(
            entity = ListEntity::class,
            parentColumns = ["list_id"],
            childColumns = ["list_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["list_id"], name = "idx_list_item"),
        Index(value = ["list_id", "name"], unique = true, name = "uq_item_name")
    ]
)
data class ListItemEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "item_id")
    val itemId: Int = 0,

    @ColumnInfo(name = "list_id")
    val listId: Int,

    val name: String,

    val quantity: Int? = null,

    val status: Boolean = false,

    @ColumnInfo(name = "last_modified")
    val lastModified: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "needs_sync")
    val needsSync: Boolean = true,

    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,

    @ColumnInfo(name = "server_id")
    val serverId: Int? = null
)
