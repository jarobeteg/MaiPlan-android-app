package com.example.maiplan.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.example.maiplan.database.entities.NoteEntity

@Dao
interface NoteDAO {
    @Query("SELECT * FROM note WHERE user_id = :userId AND sync_state != 0")
    suspend fun getPendingNotes(userId: Int): List<NoteEntity>

    @Query("SELECT * FROM note WHERE note_id = :noteId AND user_id = :userId AND is_deleted = 0")
    suspend fun getNote(noteId: Int, userId: Int): NoteEntity

    @Query("""
        SELECT * FROM note
        WHERE user_id = :userId
            AND is_deleted = 0
            AND (:categoryId IS NULL OR category_id = :categoryId)
        ORDER BY updated_at DESC, created_at DESC
    """)
    suspend fun getNotes(userId: Int, categoryId: Int? = null): List<NoteEntity>

    @Insert
    suspend fun noteInsert(entity: NoteEntity): Long

    @Upsert
    suspend fun noteUpsert(entity: NoteEntity)

    @Query("""
        UPDATE note
        SET
            title = :title,
            content = :content,
            category_id = :categoryId,
            updated_at = :updatedAt,
            last_modified = :updatedAt,
            sync_state = 2
        WHERE note_id = :noteId AND user_id = :userId
    """)
    suspend fun noteUpdate(
        noteId: Int,
        userId: Int,
        title: String,
        content: String?,
        categoryId: Int?,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE note
        SET
            is_deleted = 1,
            sync_state = 98,
            updated_at = :deletedAt,
            last_modified = :deletedAt
        WHERE note_id = :noteId AND user_id = :userId
    """)
    suspend fun softDeleteNote(noteId: Int, userId: Int, deletedAt: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteNote(entity: NoteEntity): Int
}
