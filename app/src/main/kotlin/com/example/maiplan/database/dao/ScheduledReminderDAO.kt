package com.example.maiplan.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.maiplan.database.entities.ScheduledReminderEntity

@Dao
interface ScheduledReminderDAO {
    @Query("SELECT * FROM scheduled_reminder WHERE sourceKey = :source")
    suspend fun forSource(source: String): List<ScheduledReminderEntity>
    @Query("SELECT DISTINCT userLocalId FROM scheduled_reminder WHERE taskLocalId IS NOT NULL")
    suspend fun taskUserIds(): List<Long>
    @Query("DELETE FROM scheduled_reminder WHERE sourceKey = :source AND userLocalId = :userLocalId AND deliveredAt IS NULL")
    suspend fun removeTaskPending(source: String, userLocalId: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(reminder: ScheduledReminderEntity): Long

    @Query("SELECT * FROM scheduled_reminder WHERE alarmKey = :key")
    suspend fun get(key: String): ScheduledReminderEntity?

    @Query("SELECT * FROM scheduled_reminder WHERE deliveredAt IS NULL ORDER BY triggerAt")
    suspend fun pending(): List<ScheduledReminderEntity>

    @Query("SELECT * FROM scheduled_reminder WHERE userLocalId = :user ORDER BY triggerAt")
    suspend fun forUser(user: Long): List<ScheduledReminderEntity>

    @Query("SELECT DISTINCT userLocalId FROM scheduled_reminder WHERE deliveredAt IS NULL")
    suspend fun pendingUserIds(): List<Long>

    @Query("DELETE FROM scheduled_reminder WHERE alarmKey = :key AND deliveredAt IS NULL")
    suspend fun removePending(key: String)

    @Query("DELETE FROM scheduled_reminder WHERE sourceKey = :source AND deliveredAt IS NULL AND (:keepKey IS NULL OR alarmKey != :keepKey)")
    suspend fun replacePending(source: String, keepKey: String?)

    @Query("UPDATE scheduled_reminder SET title = :title, message = :message WHERE alarmKey = :key AND deliveredAt IS NULL")
    suspend fun updateContent(key: String, title: String, message: String)

    @Query("UPDATE scheduled_reminder SET deliveredAt = :time, lastError = NULL WHERE alarmKey = :key AND deliveredAt IS NULL")
    suspend fun markDelivered(key: String, time: Long)

    @Query("UPDATE scheduled_reminder SET scheduledAt = :time, lastError = :error WHERE alarmKey = :key AND deliveredAt IS NULL")
    suspend fun markScheduled(key: String, time: Long?, error: String?)
}
