package com.example.maiplan.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "list",
    foreignKeys = [
        ForeignKey(
            entity = AuthEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["user_id"], name = "idx_list_user"),
        Index(value = ["user_id", "title"], unique = true, name = "uq_list_title")
    ]
)
data class ListEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "list_id")
    val listId: Int = 0,

    @ColumnInfo(name = "user_id")
    val userId: Int,

    val title: String,

    @ColumnInfo(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
