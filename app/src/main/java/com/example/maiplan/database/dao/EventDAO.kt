package com.example.maiplan.database.dao

import androidx.room.Dao
import androidx.room.Insert
import com.example.maiplan.database.entities.EventEntity

@Dao
interface EventDAO {
    @Insert
    suspend fun eventInsert(event: EventEntity)
}