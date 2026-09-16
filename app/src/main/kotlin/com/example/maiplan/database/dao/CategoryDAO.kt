package com.example.maiplan.database.dao

import com.example.maiplan.database.entities.CategoryEntity
import androidx.room.OnConflictStrategy
import kotlinx.coroutines.flow.Flow
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Query
import androidx.room.Dao
import java.util.UUID

@Dao
interface CategoryDAO {
    @Query(value = """
       SELECT * FROM category
       WHERE user_local_id = :userLocalId
       AND deleted_at IS NULL
       ORDER BY name COLLATE NOCASE, category_local_id
    """)
    fun observeActiveCategories(userLocalId: Long): Flow<List<CategoryEntity>>

    @Query(value = """
       SELECT * FROM category
       WHERE user_local_id = :userLocalId
       AND deleted_at IS NULL
       ORDER BY name COLLATE NOCASE, category_local_id
    """)
    suspend fun getActiveCategories(userLocalId: Long): List<CategoryEntity>

    @Query(value = """
        SELECT * FROM category
        WHERE category_local_id = :categoryLocalId
        AND user_local_id = :userLocalId
    """)
    suspend fun getCategoryByLocalId(categoryLocalId: Long, userLocalId: Long): CategoryEntity?

    @Query(value = """
        SELECT * FROM category
        WHERE sync_id = :syncId
        AND user_local_id = :userLocalId
    """)
    suspend fun getCategoryBySyncId(syncId: UUID, userLocalId: Long): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Update
    suspend fun updateCategory(category: CategoryEntity): Int

    @Query(value = """
        UPDATE category
        SET server_version = :serverVersion
        WHERE sync_id = :syncId
        AND user_local_id = :userLocalId
        AND (server_version IS NULL OR server_version < :serverVersion)
    """)
    suspend fun updateServerVersion(
        syncId: UUID,
        userLocalId: Long,
        serverVersion: Long
    ): Int

    @Query(value = """
        DELETE FROM category
        WHERE category_local_id = :categoryLocalId
        AND user_local_id = :userLocalId
    """)
    suspend fun hardDeleteCategory(categoryLocalId: Long, userLocalId: Long): Int
}