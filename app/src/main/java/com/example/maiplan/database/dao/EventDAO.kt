package com.example.maiplan.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.example.maiplan.database.entities.EventEntity

@Dao
interface EventDAO {
    @Insert
    suspend fun eventInsert(event: EventEntity)

    @Upsert
    suspend fun eventUpsert(event: EventEntity)

    @Delete
    suspend fun deleteEvent(event: EventEntity)

    @Query("UPDATE event SET sync_state = 98, is_deleted = 1 WHERE event_id = :eventId AND user_id = :userId")
    suspend fun softDeleteEvent(eventId: Int, userId: Int)

    @Query("SELECT * FROM event WHERE user_id = :userId AND sync_state != 0")
    suspend fun getPendingEvents(userId: Int): List <EventEntity>

    @Query("SELECT * FROM event where event_id = :eventId")
    suspend fun getEvent(eventId: Int): EventEntity
}