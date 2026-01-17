package com.example.maiplan.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.example.maiplan.database.entities.CategoryEntity

@Dao
interface CategoryDAO {
    @Query("SELECT * FROM category WHERE user_id = :userId AND sync_state != 0")
    suspend fun getPendingCategories(userId: Int): List<CategoryEntity>

    @Query("SELECT * FROM category WHERE category_id = :categoryId AND user_id = :userId AND is_deleted = 0")
    suspend fun getCategory(categoryId: Int, userId: Int): CategoryEntity

    @Query("SELECT * FROM category WHERE user_id = :userId AND is_deleted = 0")
    suspend fun getCategories(userId: Int): List<CategoryEntity>

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
        WHERE category_id = :categoryId AND user_id = :userId
    """)
    suspend fun categoryUpdate(name: String, description: String, color: String, icon: String, categoryId: Int, userId: Int)

    @Query("UPDATE category SET sync_state = 98, is_deleted = 1 WHERE category_id = :categoryId AND user_id = :userId")
    suspend fun softDeleteCategory(categoryId: Int, userId: Int)

    @Delete
    suspend fun deleteCategory(entity: CategoryEntity): Int

    @Query("SELECT category_id FROM category WHERE server_id = :serverId")
    suspend fun getCategoryId(serverId: Int): Int?

    @Query("SELECT server_id FROM category WHERE category_id = :localId")
    suspend fun getServerId(localId: Int): Int?
}