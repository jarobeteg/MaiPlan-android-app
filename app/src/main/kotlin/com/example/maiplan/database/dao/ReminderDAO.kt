package com.example.maiplan.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.maiplan.database.entities.ReminderEntity
import java.util.UUID

@Dao
interface ReminderDAO {
    @Query("SELECT * FROM reminder WHERE user_local_id = :userLocalId")
    suspend fun getAllForUser(userLocalId: Long): List<ReminderEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: ReminderEntity): Int

    @Query(
        """
        SELECT * FROM reminder
        WHERE reminder_local_id = :reminderLocalId
          AND user_local_id = :userLocalId
        """
    )
    suspend fun getReminderByLocalId(
        reminderLocalId: Long,
        userLocalId: Long
    ): ReminderEntity?

    @Query(
        """
        SELECT * FROM reminder
        WHERE sync_id = :syncId
          AND user_local_id = :userLocalId
        """
    )
    suspend fun getReminderBySyncId(syncId: UUID, userLocalId: Long): ReminderEntity?

    @Query(
        """
        UPDATE reminder
        SET server_version = :serverVersion
        WHERE sync_id = :syncId
          AND user_local_id = :userLocalId
          AND (server_version IS NULL OR server_version < :serverVersion)
        """
    )
    suspend fun updateServerVersion(
        syncId: UUID,
        userLocalId: Long,
        serverVersion: Long
    ): Int
}
