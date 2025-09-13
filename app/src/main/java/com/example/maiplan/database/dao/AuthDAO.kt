package com.example.maiplan.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.maiplan.database.entities.AuthEntity

@Dao
interface AuthDAO {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: AuthEntity): Long

    @Query("SELECT * FROM auth WHERE needs_sync = 1")
    suspend fun getPendingUsers(): List<AuthEntity>

    @Query("UPDATE auth SET needs_sync = 0 WHERE user_id IN (:userIds)")
    suspend fun markUsersAsSyncedInternal(userIds: List<Int>): Int

    suspend fun markUsersAsSynced(userIds: List<Int>): Boolean {
        return markUsersAsSyncedInternal(userIds) == userIds.size
    }
}