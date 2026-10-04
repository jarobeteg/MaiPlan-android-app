package com.example.maiplan.repository.note

import android.content.Context
import androidx.room.withTransaction
import com.example.maiplan.database.MaiPlanDatabase
import com.example.maiplan.database.entities.NoteEntity
import com.example.maiplan.database.entities.OutboxEntity
import com.example.maiplan.database.entities.ReminderEntity
import com.example.maiplan.network.sync.NoteMutationPayload
import com.example.maiplan.network.sync.TideEntityType
import com.example.maiplan.network.sync.TideOperation
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.handleLocalResponse
import com.example.maiplan.repository.reminder.ReminderMutationWriter
import com.example.maiplan.utils.notifications.ReminderPlanWriter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.time.Instant
import java.util.UUID

data class StoredNoteWithReminder(
    val note: NoteEntity,
    val reminder: ReminderEntity?
)

class NoteLocalDataSource(
    context: Context,
    private val databaseOverride: MaiPlanDatabase? = null
) {
    private val database: MaiPlanDatabase by lazy {
        databaseOverride ?: MaiPlanDatabase.getDatabase(context.applicationContext)
    }
    private val noteDao by lazy { database.noteDAO() }
    private val categoryDao by lazy { database.categoryDAO() }
    private val reminderDao by lazy { database.reminderDAO() }
    private val userDao by lazy { database.userDAO() }
    private val outboxDao by lazy { database.outboxDAO() }
    private val reminderWriter by lazy { ReminderMutationWriter(database) }
    private val gson: Gson by lazy { GsonBuilder().serializeNulls().create() }

    suspend fun getNote(noteLocalId: Long, userLocalId: Long): Result<NoteEntity> {
        return handleLocalResponse {
            checkNotNull(noteDao.getNoteByLocalId(noteLocalId, userLocalId)) {
                "Note $noteLocalId was not found"
            }
        }
    }

    suspend fun getNotes(
        userLocalId: Long,
        categoryLocalId: Long? = null
    ): Result<List<NoteEntity>> {
        return handleLocalResponse { noteDao.getNotes(userLocalId, categoryLocalId) }
    }

    suspend fun createNoteWithReminder(
        reminder: ReminderEntity?,
        note: NoteEntity
    ): Result<StoredNoteWithReminder> {
        if (note.title.isBlank()) return Result.Failure(EMPTY_NOTE_TITLE_ERROR)

        return handleLocalResponse {
            database.withTransaction {
                ensureLocalUserExists(note.userLocalId)
                val now = Instant.now()
                val storedReminder = reminder?.let {
                    reminderWriter.create(it, note.userLocalId, now)
                }
                val created = note.copy(
                    noteLocalId = 0L,
                    reminderLocalId = storedReminder?.reminderLocalId,
                    title = note.title.trim(),
                    serverVersion = null,
                    createdAt = now,
                    updatedAt = now,
                    deletedAt = null
                )
                val noteLocalId = noteDao.insertNote(created)
                val storedNote = created.copy(noteLocalId = noteLocalId)
                ReminderPlanWriter(database).note(storedNote, storedReminder)
                enqueue(storedNote, TideOperation.CREATE, null, now)
                StoredNoteWithReminder(storedNote, storedReminder)
            }
        }
    }

    suspend fun updateNoteWithReminder(
        reminder: ReminderEntity?,
        note: NoteEntity
    ): Result<StoredNoteWithReminder> {
        if (note.title.isBlank()) return Result.Failure(EMPTY_NOTE_TITLE_ERROR)

        return handleLocalResponse {
            database.withTransaction {
                val existing = checkNotNull(
                    noteDao.getNoteByLocalId(note.noteLocalId, note.userLocalId)
                ) { "Note ${note.noteLocalId} was not found" }
                check(existing.deletedAt == null) { "A deleted Note cannot be updated" }

                val now = Instant.now()
                val reminderToDelete = if (reminder == null) existing.reminderLocalId else null
                val storedReminder = when {
                    existing.reminderLocalId == null && reminder != null -> {
                        reminderWriter.create(reminder, note.userLocalId, now)
                    }
                    existing.reminderLocalId != null && reminder != null -> {
                        reminderWriter.update(
                            reminder.copy(reminderLocalId = existing.reminderLocalId),
                            note.userLocalId,
                            now
                        )
                    }
                    existing.reminderLocalId != null -> null
                    else -> null
                }

                val updated = existing.copy(
                    categoryLocalId = note.categoryLocalId,
                    reminderLocalId = storedReminder?.reminderLocalId,
                    title = note.title.trim(),
                    content = note.content,
                    isPinned = note.isPinned,
                    updatedAt = now
                )
                check(noteDao.updateNote(updated) == 1) {
                    "Note update affected an unexpected number of rows"
                }
                enqueue(updated, TideOperation.UPDATE, existing.serverVersion, now)
                reminderToDelete?.let {
                    reminderWriter.delete(it, note.userLocalId, now)
                }
                ReminderPlanWriter(database).note(updated, storedReminder)
                StoredNoteWithReminder(updated, storedReminder)
            }
        }
    }

    suspend fun softDeleteNoteWithReminder(
        noteLocalId: Long,
        userLocalId: Long
    ): Result<Unit> {
        return handleLocalResponse {
            database.withTransaction {
                val existing = checkNotNull(
                    noteDao.getNoteByLocalId(noteLocalId, userLocalId)
                ) { "Note $noteLocalId was not found" }

                if (existing.deletedAt == null) {
                    val now = Instant.now()
                    val tombstone = existing.copy(updatedAt = now, deletedAt = now)
                    check(noteDao.updateNote(tombstone) == 1) {
                        "Note deletion affected an unexpected number of rows"
                    }
                    enqueue(tombstone, TideOperation.DELETE, existing.serverVersion, now)
                    ReminderPlanWriter(database).note(tombstone, null)
                    existing.reminderLocalId?.let {
                        reminderWriter.delete(it, userLocalId, now)
                    }
                }
                Unit
            }
        }
    }

    private suspend fun enqueue(
        note: NoteEntity,
        operation: String,
        baseVersion: Long?,
        now: Instant
    ) {
        val payload = if (operation == TideOperation.DELETE) null else {
            val categorySyncId = note.categoryLocalId?.let { localId ->
                checkNotNull(categoryDao.getCategoryByLocalId(localId, note.userLocalId)) {
                    "Category $localId was not found for Note ${note.noteLocalId}"
                }.syncId
            }
            val reminderSyncId = note.reminderLocalId?.let { localId ->
                checkNotNull(reminderDao.getReminderByLocalId(localId, note.userLocalId)) {
                    "Reminder $localId was not found for Note ${note.noteLocalId}"
                }.syncId
            }
            gson.toJson(
                NoteMutationPayload(
                    categorySyncId = categorySyncId,
                    reminderSyncId = reminderSyncId,
                    title = note.title,
                    content = note.content,
                    isPinned = note.isPinned
                )
            )
        }

        outboxDao.insertMutation(
            OutboxEntity(
                mutationId = UUID.randomUUID(),
                userLocalId = note.userLocalId,
                entityType = TideEntityType.NOTE,
                entitySyncId = note.syncId,
                operation = operation,
                baseVersion = baseVersion,
                payloadJson = payload,
                createdAt = now
            )
        )
    }

    private suspend fun ensureLocalUserExists(userLocalId: Long) {
        checkNotNull(userDao.getUserByLocalId(userLocalId)) {
            "Cannot save a Note for a missing local user"
        }
    }

    private companion object {
        const val EMPTY_NOTE_TITLE_ERROR = 1
    }
}
