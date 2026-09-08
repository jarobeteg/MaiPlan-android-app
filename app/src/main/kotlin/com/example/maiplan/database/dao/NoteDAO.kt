package com.example.maiplan.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.example.maiplan.database.entities.NoteEntity

@Dao
interface NoteDAO {
    @Query("SELECT * FROM note WHERE user_local_id = :userLocalId AND sync_state != 0")
    suspend fun getPendingNotes(userLocalId: Long): List<NoteEntity>

    @Query("SELECT * FROM note WHERE note_id = :noteId AND user_local_id = :userLocalId AND is_deleted = 0")
    suspend fun getNote(noteId: Int, userLocalId: Long): NoteEntity

    @Query("""
        SELECT * FROM note
        WHERE user_local_id = :userLocalId
            AND is_deleted = 0
            AND (:categoryId IS NULL OR category_id = :categoryId)
        ORDER BY updated_at DESC, created_at DESC
    """)
    suspend fun getNotes(userLocalId: Long, categoryId: Int? = null): List<NoteEntity>

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
            reminder_id = :reminderId,
            updated_at = :updatedAt,
            last_modified = :updatedAt,
            sync_state = 2
        WHERE note_id = :noteId AND user_local_id = :userLocalId
    """)
    suspend fun noteUpdate(
        noteId: Int,
        userLocalId: Long,
        title: String,
        content: String?,
        categoryId: Int?,
        reminderId: Int?,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE note
        SET
            is_deleted = 1,
            sync_state = 98,
            updated_at = :deletedAt,
            last_modified = :deletedAt
        WHERE note_id = :noteId AND user_local_id = :userLocalId
    """)
    suspend fun softDeleteNote(noteId: Int, userLocalId: Long, deletedAt: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteNote(entity: NoteEntity): Int
}
