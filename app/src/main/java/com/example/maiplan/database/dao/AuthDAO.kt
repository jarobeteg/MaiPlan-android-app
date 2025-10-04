package com.example.maiplan.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.maiplan.database.entities.AuthEntity

@Dao
interface AuthDAO {
    @Query("SELECT * FROM auth WHERE email = :email AND sync_state != 0")
    suspend fun getPendingUser(email: String): AuthEntity?

    @Upsert
    suspend fun authSync(entity: AuthEntity)

    @Delete
    suspend fun deleteUser(entity: AuthEntity): Int
}