package com.example.maiplan.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.maiplan.database.entities.NoteEntity
import java.util.UUID

@Dao
interface NoteDAO {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity): Int

    @Query(
        """
        SELECT * FROM note
        WHERE note_local_id = :noteLocalId
          AND user_local_id = :userLocalId
        """
    )
    suspend fun getNoteByLocalId(noteLocalId: Long, userLocalId: Long): NoteEntity?

    @Query(
        """
        SELECT * FROM note
        WHERE sync_id = :syncId
          AND user_local_id = :userLocalId
        """
    )
    suspend fun getNoteBySyncId(syncId: UUID, userLocalId: Long): NoteEntity?

    @Query(
        """
        SELECT * FROM note
        WHERE user_local_id = :userLocalId
          AND deleted_at IS NULL
          AND (:categoryLocalId IS NULL OR category_local_id = :categoryLocalId)
        ORDER BY is_pinned DESC, updated_at DESC, created_at DESC
        """
    )
    suspend fun getNotes(
        userLocalId: Long,
        categoryLocalId: Long? = null
    ): List<NoteEntity>

    @Query(
        """
        UPDATE note
        SET server_version = :serverVersion
        WHERE sync_id = :syncId
          AND user_local_id = :userLocalId
          AND (server_version IS NULL OR server_version < :serverVersion)
        """
    )
    suspend fun updateServerVersion(
        syncId: UUID,
        userLocalId: Long,
        serverVersion: Long
    ): Int
}
