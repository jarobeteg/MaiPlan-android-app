package com.example.maiplan.repository.note

import com.example.maiplan.database.entities.CategoryEntity
import com.example.maiplan.database.entities.NoteEntity
import com.example.maiplan.database.entities.ReminderEntity
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.category.CategoryLocalDataSource
import com.example.maiplan.repository.orEmptyList
import com.example.maiplan.repository.map
import com.example.maiplan.repository.reminder.ReminderLocalDataSource

data class NoteSaveOutcome(
    val reminderId: Int?,
    val reminderTime: Long?,
    val reminderTitle: String,
    val reminderMessage: String,
)

class NoteRepository(
    private val local: NoteLocalDataSource,
    private val localCategory: CategoryLocalDataSource,
    private val localReminder: ReminderLocalDataSource,
) {
    suspend fun createNote(note: NoteEntity): Result<Unit> {
        return local.noteInsert(note)
    }

    suspend fun updateNote(note: NoteEntity): Result<Unit> {
        return local.noteUpdate(note)
    }

    suspend fun createNoteWithReminder(
        reminder: ReminderEntity?,
        note: NoteEntity,
    ): Result<NoteSaveOutcome> {
        return local.createNoteWithReminder(reminder, note).map { reminderId ->
            NoteSaveOutcome(
                reminderId = reminderId,
                reminderTime = reminder?.reminderTime,
                reminderTitle = note.title,
                reminderMessage = reminder?.message.orEmpty(),
            )
        }
    }

    suspend fun updateNoteWithReminder(
        reminder: ReminderEntity?,
        note: NoteEntity,
    ): Result<NoteSaveOutcome> {
        return local.updateNoteWithReminder(reminder, note).map { reminderId ->
            NoteSaveOutcome(
                reminderId = reminderId,
                reminderTime = reminder?.reminderTime,
                reminderTitle = note.title,
                reminderMessage = reminder?.message.orEmpty(),
            )
        }
    }

    suspend fun softDeleteNote(noteId: Int, userLocalId: Long): Result<Unit> {
        return local.softDeleteNoteWithReminder(noteId, userLocalId)
    }

    suspend fun getReminder(reminderId: Int?): Result<ReminderEntity?> {
        if (reminderId == null) return Result.Success(null)
        return try {
            Result.Success(localReminder.getReminder(reminderId))
        } catch (exception: Exception) {
            Result.Error(exception)
        }
    }

    suspend fun getNotes(userLocalId: Long, categoryLocalId: Long? = null): Result<List<NoteEntity>> {
        return local.getNotes(userLocalId, categoryLocalId)
    }

    suspend fun getCategories(userLocalId: Long): List<CategoryEntity> {
        return localCategory.getCategories(userLocalId).orEmptyList()
    }
}
