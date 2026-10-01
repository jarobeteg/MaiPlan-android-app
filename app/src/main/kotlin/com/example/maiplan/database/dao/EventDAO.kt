package com.example.maiplan.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.maiplan.database.entities.EventEntity
import java.util.UUID

@Dao
interface EventDAO {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertEvent(event: EventEntity): Long

    @Update
    suspend fun updateEvent(event: EventEntity): Int

    @Query(
        """
        SELECT * FROM event
        WHERE event_local_id = :eventLocalId
          AND user_local_id = :userLocalId
        """
    )
    suspend fun getEventByLocalId(eventLocalId: Long, userLocalId: Long): EventEntity?

    @Query(
        """
        SELECT * FROM event
        WHERE sync_id = :syncId
          AND user_local_id = :userLocalId
        """
    )
    suspend fun getEventBySyncId(syncId: UUID, userLocalId: Long): EventEntity?

    @Query("""
        SELECT * FROM event WHERE user_local_id = :userLocalId
          AND deleted_at IS NULL AND recurrence_frequency IS NULL
          AND start_time IS NOT NULL AND end_time IS NOT NULL
          AND start_date < :endExclusiveEpochDay AND end_date >= :startEpochDay
    """)
    suspend fun getTimedOneOffOverlapping(
        userLocalId: Long, startEpochDay: Long, endExclusiveEpochDay: Long
    ): List<EventEntity>

    @Query("""
        SELECT * FROM event WHERE user_local_id = :userLocalId
          AND deleted_at IS NULL AND recurrence_frequency IS NULL
          AND start_time IS NULL AND end_time IS NULL
          AND start_date < :endExclusiveEpochDay AND end_date >= :startEpochDay
    """)
    suspend fun getDateOnlyOneOffOverlapping(
        userLocalId: Long, startEpochDay: Long, endExclusiveEpochDay: Long
    ): List<EventEntity>

    @Query("""
        SELECT * FROM event WHERE user_local_id = :userLocalId
          AND deleted_at IS NULL AND recurrence_frequency IS NOT NULL
    """)
    suspend fun getActiveSeries(userLocalId: Long): List<EventEntity>

    @Query("SELECT event_local_id FROM event WHERE user_local_id = :userLocalId")
    suspend fun getAllEventIdsForUser(userLocalId: Long): List<Long>

    @Query(
        """
        UPDATE event
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
