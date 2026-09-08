package com.example.maiplan.database.dao

import com.example.maiplan.database.entities.UserEntity
import androidx.room.OnConflictStrategy
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Dao
import java.time.Instant
import java.util.UUID

@Dao
interface UserDAO {
    @Query(value = "SELECT * FROM user WHERE user_local_id = :userLocalId AND deleted_at is NULL")
    suspend fun getActiveUserByLocalId(userLocalId: Long): UserEntity?

    @Query(value = "SELECT * FROM user WHERE user_local_id = :userLocalId")
    suspend fun getUserByLocalId(userLocalId: Long): UserEntity?

    @Query(value = "SELECT * FROM user WHERE email = :email AND deleted_at is NULL")
    suspend fun getActiveUserByEmail(email: String): UserEntity?

    @Query(value = "SELECT * FROM user WHERE email = :email")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query(value = "SELECT * FROM user WHERE sync_id = :syncId AND deleted_at is NULL")
    suspend fun getActiveUserBySyncId(syncId: UUID): UserEntity?

    @Query(value = "SELECT * FROM user WHERE sync_id = :syncId")
    suspend fun getUserBySyncId(syncId: UUID): UserEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Query(value = """
        UPDATE user 
        SET username = :username, updated_at = :updatedAt
        WHERE user_local_id = :userLocalId AND deleted_at IS NULL
        """)
    suspend fun updateUserLocally(
        username: String,
        updatedAt: Instant,
        userLocalId: Long
    ): Int

    @Query(value = """
        UPDATE user
        SET updated_at = :updatedAt, deleted_at = :deletedAt
        WHERE user_local_id = :userLocalId AND deleted_at IS NULL
    """)
    suspend fun markUserDeletedLocally(
        updatedAt: Instant,
        deletedAt: Instant,
        userLocalId: Long
    ): Int

    @Query(value = """
        UPDATE user
        SET 
            username = :username, 
            server_version = :serverVersion, 
            created_at = :createdAt,
            updated_at = :updatedAt,
            deleted_at = :deletedAt
        WHERE 
            sync_id = :syncId
        AND
            (server_version IS NULL
        OR
            server_version < :serverVersion)
    """)
    suspend fun applyServerUser(
        username: String,
        serverVersion: Long,
        createdAt: Instant,
        updatedAt: Instant,
        deletedAt: Instant?,
        syncId: UUID
    ): Int

    @Query(value = """
        UPDATE user 
        SET server_version = :serverVersion, updated_at = :updatedAt, deleted_at = :deletedAt
        WHERE 
            sync_id = :syncId
        AND
            (server_version IS NULL
        OR
            server_version < :serverVersion)
        """)
    suspend fun applyServerUserDeletion(
        serverVersion: Long,
        updatedAt: Instant,
        deletedAt: Instant,
        syncId: UUID
    ): Int

    @Query ("DELETE FROM user WHERE sync_id = :syncId")
    suspend fun hardDeleteUser(syncId: UUID): Int
}