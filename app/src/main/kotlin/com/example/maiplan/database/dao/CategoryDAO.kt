package com.example.maiplan.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.example.maiplan.database.entities.CategoryEntity

@Dao
interface CategoryDAO {
    @Query("SELECT * FROM category WHERE user_local_id = :userLocalId AND sync_state != 0")
    suspend fun getPendingCategories(userLocalId: Long): List<CategoryEntity>

    @Query("SELECT * FROM category WHERE category_id = :categoryId AND user_local_id = :userLocalId AND is_deleted = 0")
    suspend fun getCategory(categoryId: Int, userLocalId: Long): CategoryEntity

    @Query("SELECT * FROM category WHERE user_local_id = :userLocalId AND is_deleted = 0")
    suspend fun getCategories(userLocalId: Long): List<CategoryEntity>

    @Upsert
    suspend fun categoryUpsert(entity: CategoryEntity)

    @Insert
    suspend fun categoryInsert(entity: CategoryEntity)

    @Query("""
        UPDATE category
        SET
            name = :name,
            description = :description,
            color = :color,
            icon = :icon,
            sync_state = 2
        WHERE category_id = :categoryId AND user_local_id = :userLocalId
    """)
    suspend fun categoryUpdate(name: String, description: String, color: String, icon: String, categoryId: Int, userLocalId: Long)

    @Query("UPDATE category SET sync_state = 98, is_deleted = 1 WHERE category_id = :categoryId AND user_local_id = :userLocalId")
    suspend fun softDeleteCategory(categoryId: Int, userLocalId: Long)

    @Delete
    suspend fun deleteCategory(entity: CategoryEntity): Int

    @Query("SELECT category_id FROM category WHERE server_id = :serverId")
    suspend fun getCategoryId(serverId: Int): Int?

    @Query("SELECT server_id FROM category WHERE category_id = :localId")
    suspend fun getServerId(localId: Int): Int?
}