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

    @Query("""
        UPDATE auth
        SET
        email = :email,
        username = :username,
        balance = :balance,
        created_at = :createdAt,
        updated_at = :updatedAt,
        password_hash = :passwordHash,
        last_modified = :lastModified,
        sync_state = :syncState,
        is_deleted = :isDeleted
        WHERE server_id = :serverId
    """)
    suspend fun authDataUpdate(
        email: String,
        username: String,
        balance: Float,
        createdAt: Long,
        updatedAt: Long,
        passwordHash: String,
        lastModified: Long,
        syncState: Int,
        isDeleted: Int,
        serverId: Int
    ): Int

    @Query("""
        INSERT INTO auth
        VALUES
        (
        :email,
        :username,
        :balance,
        :createdAt,
        :updatedAt,
        :passwordHash,
        :lastModified,
        :syncState,
        :isDeleted,
        :serverId
        )
    """)
    suspend fun authDataInsert(
        email: String,
        username: String,
        balance: Float,
        createdAt: Long,
        updatedAt: Long,
        passwordHash: String,
        lastModified: Long,
        syncState: Int,
        isDeleted: Int,
        serverId: Int
    ): Int

    @Delete
    suspend fun deleteUser(entity: AuthEntity): Int
}