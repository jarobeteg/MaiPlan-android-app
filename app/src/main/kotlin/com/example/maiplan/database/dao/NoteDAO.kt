package com.example.maiplan.database.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.ColumnInfo
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.maiplan.database.entities.NoteEntity
import java.util.UUID
import kotlinx.coroutines.flow.Flow

data class NoteReminderRecord(
    @Embedded val note: NoteEntity,
    @ColumnInfo(name = "home_reminder_time") val reminderTime: Long,
)

@Dao
interface NoteDAO {
    @Query("""
        SELECT n.*, r.reminder_time AS home_reminder_time FROM note n
        JOIN reminder r ON r.reminder_local_id = n.reminder_local_id
          AND r.user_local_id = n.user_local_id AND r.deleted_at IS NULL AND r.status = 1
        WHERE n.user_local_id = :userLocalId AND n.deleted_at IS NULL
          AND r.reminder_time >= :fromInclusive AND r.reminder_time < :untilExclusive
        ORDER BY r.reminder_time, n.title COLLATE NOCASE, n.sync_id
    """)
    fun observeReminderNotes(userLocalId: Long, fromInclusive: Long, untilExclusive: Long): Flow<List<NoteReminderRecord>>

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
