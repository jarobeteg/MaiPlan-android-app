package com.example.maiplan.repository.note

import com.example.maiplan.database.entities.CategoryEntity
import com.example.maiplan.database.entities.NoteEntity
import com.example.maiplan.database.entities.ReminderEntity
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.category.CategoryLocalDataSource
import com.example.maiplan.repository.map
import com.example.maiplan.repository.orEmptyList
import com.example.maiplan.repository.reminder.ReminderLocalDataSource

data class NoteSaveOutcome(
    val reminderLocalId: Long?,
    val reminderTime: Long?,
    val reminderTitle: String,
    val reminderMessage: String
)

class NoteRepository(
    private val local: NoteLocalDataSource,
    private val localCategory: CategoryLocalDataSource,
    private val localReminder: ReminderLocalDataSource,
    private val requestSync: () -> Unit = {}
) {
    suspend fun createNote(note: NoteEntity): Result<NoteSaveOutcome> {
        return createNoteWithReminder(null, note)
    }

    suspend fun updateNote(note: NoteEntity): Result<NoteSaveOutcome> {
        return updateNoteWithReminder(null, note)
    }

    suspend fun createNoteWithReminder(
        reminder: ReminderEntity?,
        note: NoteEntity
    ): Result<NoteSaveOutcome> {
        return local.createNoteWithReminder(reminder, note)
            .also(::requestSyncAfterSuccess)
            .map { stored -> stored.toSaveOutcome() }
    }

    suspend fun updateNoteWithReminder(
        reminder: ReminderEntity?,
        note: NoteEntity
    ): Result<NoteSaveOutcome> {
        return local.updateNoteWithReminder(reminder, note)
            .also(::requestSyncAfterSuccess)
            .map { stored -> stored.toSaveOutcome() }
    }

    suspend fun softDeleteNote(noteLocalId: Long, userLocalId: Long): Result<Unit> {
        return local.softDeleteNoteWithReminder(noteLocalId, userLocalId)
            .also(::requestSyncAfterSuccess)
    }

    suspend fun getReminder(
        reminderLocalId: Long?,
        userLocalId: Long
    ): Result<ReminderEntity?> {
        if (reminderLocalId == null) return Result.Success(null)
        return try {
            Result.Success(localReminder.getReminder(reminderLocalId, userLocalId))
        } catch (exception: Exception) {
            Result.Error(exception)
        }
    }

    suspend fun getNotes(
        userLocalId: Long,
        categoryLocalId: Long? = null
    ): Result<List<NoteEntity>> {
        return local.getNotes(userLocalId, categoryLocalId)
    }

    suspend fun getCategories(userLocalId: Long): List<CategoryEntity> {
        return localCategory.getCategories(userLocalId).orEmptyList()
    }

    private fun StoredNoteWithReminder.toSaveOutcome(): NoteSaveOutcome {
        return NoteSaveOutcome(
            reminderLocalId = reminder?.reminderLocalId,
            reminderTime = reminder?.reminderTime,
            reminderTitle = note.title,
            reminderMessage = reminder?.message.orEmpty()
        )
    }

    private fun requestSyncAfterSuccess(result: Result<*>) {
        if (result is Result.Success) runCatching(requestSync)
    }
}
