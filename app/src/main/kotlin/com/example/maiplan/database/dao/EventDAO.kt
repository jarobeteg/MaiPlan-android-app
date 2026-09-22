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

    @Query(
        """
        SELECT * FROM event
        WHERE date BETWEEN :startMillis AND :endMillis
          AND deleted_at IS NULL
          AND user_local_id = :userLocalId
        ORDER BY date, start_time, event_local_id
        """
    )
    suspend fun getEventsForRange(
        startMillis: Long,
        endMillis: Long,
        userLocalId: Long
    ): List<EventEntity>

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
