package com.example.maiplan.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.maiplan.database.entities.AuthEntity

@Dao
interface AuthDAO {

    @Query("SELECT * FROM auth WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): AuthEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: AuthEntity): Long
}