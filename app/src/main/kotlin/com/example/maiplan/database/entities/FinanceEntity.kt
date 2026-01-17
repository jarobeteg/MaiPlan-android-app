package com.example.maiplan.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "finance",
    foreignKeys = [
        ForeignKey(
            entity = AuthEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["category_id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = ReminderEntity::class,
            parentColumns = ["reminder_id"],
            childColumns = ["reminder_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["category_id"]),
        Index(value = ["reminder_id"]),
        Index(value = ["type"]),
        Index(value = ["expense_date"]),
        Index(value = ["user_id", "expense_date"]),
        Index(value = ["last_modified"]),
        Index(value = ["sync_state"]),
        Index(value = ["server_id"])
    ]
)
data class FinanceEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "finance_id")
    val financeId: Int = 0,

    @ColumnInfo(name = "user_id")
    val userId: Int,

    @ColumnInfo(name = "category_id")
    val categoryId: Int? = null,

    @ColumnInfo(name = "reminder_id")
    val reminderId: Int? = null,

    val type: Boolean,

    @ColumnInfo(name = "expense_amount")
    val expenseAmount: Float,

    @ColumnInfo(name = "expense_date")
    val expenseDate: Long? = null,

    val description: String? = null,

    @ColumnInfo(name = "last_modified")
    val lastModified: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "sync_state")
    val syncState: Int = 0,

    @ColumnInfo(name = "is_deleted")
    val isDeleted: Int = 0,

    @ColumnInfo(name = "server_id")
    val serverId: Int? = null
)
