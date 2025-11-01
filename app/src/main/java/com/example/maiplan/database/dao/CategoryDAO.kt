package com.example.maiplan.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.maiplan.database.entities.CategoryEntity

@Dao
interface CategoryDAO {
    @Query("SELECT * FROM category WHERE category_id = :categoryId AND sync_state != 0")
    suspend fun getPendingCategory(categoryId: Int): CategoryEntity?

    @Query("SELECT * FROM category WHERE category_id = :categoryId")
    suspend fun getCategory(categoryId: Int): CategoryEntity?

    @Upsert
    suspend fun categoryUpsert(entity: CategoryEntity)

    @Delete
    suspend fun deleteCategory(entity: CategoryEntity): Int
}