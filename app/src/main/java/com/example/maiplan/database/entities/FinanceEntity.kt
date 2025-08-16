package com.example.maiplan.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

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
        Index(value = ["user_id"], name = "idx_finance_user"),
        Index(value = ["category_id"], name = "idx_finance_category"),
        Index(value = ["reminder_id"], name = "idx_finance_reminder"),
        Index(value = ["type"], name = "idx_finance_type"),
        Index(value = ["expense_date"], name = "idx_finance_expense_date"),
        Index(value = ["user_id", "expense_date"], name = "idx_finance_user_date"),
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
    val expenseAmount: Double,

    @ColumnInfo(name = "expense_date")
    val expenseDate: LocalDateTime? = null,

    val description: String? = null,

    @ColumnInfo(name = "last_modified")
    val lastModified: LocalDateTime = LocalDateTime.now(),

    @ColumnInfo(name = "needs_sync")
    val needsSync: Boolean = true,

    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,

    @ColumnInfo(name = "server_id")
    val serverId: Int? = null
)
