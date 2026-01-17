package com.example.maiplan.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.example.maiplan.database.entities.ReminderEntity

@Dao
interface ReminderDAO {
    @Insert
    suspend fun reminderInsert(reminder: ReminderEntity): Long

    @Upsert
    suspend fun reminderUpsert(reminder: ReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)

    @Query("UPDATE reminder SET sync_state = 98, is_deleted = 1 WHERE reminder_id = :reminderId AND user_id = :userId")
    suspend fun softDeleteReminder(reminderId: Int, userId: Int)

    @Query("SELECT * FROM reminder WHERE user_id = :userId AND sync_state != 0")
    suspend fun getPendingReminders(userId: Int): List<ReminderEntity>

    @Query("SELECT reminder_id FROM reminder WHERE server_id = :serverId")
    suspend fun getReminderId(serverId: Int): Int?

    @Query("SELECT server_id FROM reminder WHERE reminder_id = :localId")
    suspend fun getServerId(localId: Int): Int?
}