package com.example.maiplan.repository.note

import android.content.Context
import androidx.room.withTransaction
import com.example.maiplan.database.MaiPlanDatabase
import com.example.maiplan.database.dao.NoteDAO
import com.example.maiplan.database.dao.ReminderDAO
import com.example.maiplan.database.dao.UserDAO
import com.example.maiplan.database.entities.UserEntity
import com.example.maiplan.database.entities.NoteEntity
import com.example.maiplan.database.entities.ReminderEntity
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.handleLocalResponse
import com.example.maiplan.utils.common.UserSession

class NoteLocalDataSource(private val context: Context) {
    companion object {
        private const val EMPTY_NOTE_TITLE_ERROR = 1
    }

    private val database: MaiPlanDatabase by lazy {
        MaiPlanDatabase.getDatabase(context)
    }

    private val noteDao: NoteDAO by lazy {
        database.noteDAO()
    }

    private val reminderDao: ReminderDAO by lazy {
        database.reminderDAO()
    }

    private val userDao: UserDAO by lazy {
        database.userDAO()
    }

    suspend fun getPendingNotes(userLocalId: Long): Result<List<NoteEntity>> {
        return handleLocalResponse {
            noteDao.getPendingNotes(userLocalId)
        }
    }

    suspend fun getNote(noteId: Int, userLocalId: Long): Result<NoteEntity> {
        return handleLocalResponse {
            noteDao.getNote(noteId, userLocalId)
        }
    }

    suspend fun getNotes(userLocalId: Long, categoryId: Int? = null): Result<List<NoteEntity>> {
        return handleLocalResponse {
            noteDao.getNotes(userLocalId, categoryId)
        }
    }

    suspend fun noteInsert(note: NoteEntity): Result<Unit> {
        if (note.title.isBlank()) {
            return Result.Failure(EMPTY_NOTE_TITLE_ERROR)
        }

        return handleLocalResponse {
            ensureLocalUserExists(note.userLocalId)
            noteDao.noteInsert(note)
            Unit
        }
    }

    suspend fun createNoteWithReminder(
        reminder: ReminderEntity?,
        note: NoteEntity,
    ): Result<Int?> {
        if (note.title.isBlank()) {
            return Result.Failure(EMPTY_NOTE_TITLE_ERROR)
        }

        return handleLocalResponse {
            database.withTransaction {
                ensureLocalUserExists(note.userLocalId)
                val reminderId = reminder?.let { reminderDao.reminderInsert(it).toInt() }
                noteDao.noteInsert(note.copy(reminderId = reminderId))
                reminderId
            }
        }
    }

    suspend fun noteUpdate(note: NoteEntity): Result<Unit> {
        if (note.title.isBlank()) {
            return Result.Failure(EMPTY_NOTE_TITLE_ERROR)
        }

        return handleLocalResponse {
            noteDao.noteUpdate(
                noteId = note.noteId,
                userLocalId = note.userLocalId,
                title = note.title,
                content = note.content,
                categoryId = note.categoryId,
                reminderId = note.reminderId,
            )
        }
    }

    suspend fun updateNoteWithReminder(
        reminder: ReminderEntity?,
        note: NoteEntity,
    ): Result<Int?> {
        if (note.title.isBlank()) {
            return Result.Failure(EMPTY_NOTE_TITLE_ERROR)
        }

        return handleLocalResponse {
            database.withTransaction {
                val existingReminderId = note.reminderId
                val finalReminderId = when {
                    existingReminderId == null && reminder != null -> {
                        reminderDao.reminderInsert(reminder.copy(reminderId = 0)).toInt()
                    }
                    existingReminderId != null && reminder != null -> {
                        reminderDao.reminderUpdate(
                            reminderId = existingReminderId,
                            userLocalId = note.userLocalId,
                            reminderTime = reminder.reminderTime,
                            message = reminder.message,
                        )
                        existingReminderId
                    }
                    existingReminderId != null -> {
                        reminderDao.softDeleteReminder(existingReminderId, note.userLocalId)
                        null
                    }
                    else -> null
                }

                noteDao.noteUpdate(
                    noteId = note.noteId,
                    userLocalId = note.userLocalId,
                    title = note.title,
                    content = note.content,
                    categoryId = note.categoryId,
                    reminderId = finalReminderId,
                )
                finalReminderId
            }
        }
    }

    suspend fun noteUpsert(note: NoteEntity): Result<Unit> {
        return handleLocalResponse {
            ensureLocalUserExists(note.userLocalId)
            noteDao.noteUpsert(note)
        }
    }

    suspend fun softDeleteNote(noteId: Int, userLocalId: Long): Result<Unit> {
        return handleLocalResponse {
            noteDao.softDeleteNote(noteId, userLocalId)
        }
    }

    suspend fun softDeleteNoteWithReminder(noteId: Int, userLocalId: Long): Result<Unit> {
        return handleLocalResponse {
            database.withTransaction {
                val note = noteDao.getNote(noteId, userLocalId)
                note.reminderId?.let { reminderDao.softDeleteReminder(it, userLocalId) }
                noteDao.softDeleteNote(noteId, userLocalId)
            }
        }
    }

    suspend fun deleteNote(note: NoteEntity): Result<Unit> {
        return handleLocalResponse {
            noteDao.deleteNote(note)
        }
    }

    private suspend fun ensureLocalUserExists(userLocalId: Long) {
        if (userDao.getUserByLocalId(userLocalId) != null) return
    }
}
