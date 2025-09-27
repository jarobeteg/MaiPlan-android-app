package com.example.maiplan.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.maiplan.database.entities.AuthEntity

@Dao
interface AuthDAO {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: AuthEntity)

    @Query("SELECT * FROM auth WHERE email = :email AND sync_state != 0")
    suspend fun getPendingUser(email: String): AuthEntity?

    @Update
    suspend fun authSync(entity: AuthEntity): Int

    @Delete
    suspend fun deleteUser(entity: AuthEntity): Int
}