package com.example.maiplan.database.dao

import androidx.room.Dao
import androidx.room.Insert
import com.example.maiplan.database.entities.ReminderEntity

@Dao
interface ReminderDAO {
    @Insert
    suspend fun reminderInsert(reminder: ReminderEntity): Long
}