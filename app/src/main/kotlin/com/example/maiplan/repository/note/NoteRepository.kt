package com.example.maiplan.repository.note

import android.content.Context
import android.util.Log
import com.example.maiplan.utils.notifications.ReminderCoordinator
import com.example.maiplan.utils.notifications.ReminderAlarmScheduler
import com.example.maiplan.utils.notifications.enqueueEventAlarmRecovery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
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
    val reminderMessage: String,
    val reminderWarning: String? = null,
)

class NoteRepository(
    private val context: Context,
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
        return withContext(NonCancellable + Dispatchers.IO) {
            armSaved(local.createNoteWithReminder(reminder, note), note.userLocalId)
        }.also(::requestSyncAfterSuccess)
    }

    suspend fun updateNoteWithReminder(
        reminder: ReminderEntity?,
        note: NoteEntity
    ): Result<NoteSaveOutcome> {
        return withContext(NonCancellable + Dispatchers.IO) {
            armSaved(local.updateNoteWithReminder(reminder, note), note.userLocalId)
        }.also(::requestSyncAfterSuccess)
    }

    suspend fun softDeleteNote(noteLocalId: Long, userLocalId: Long): Result<Unit> {
        return local.softDeleteNoteWithReminder(noteLocalId, userLocalId)
            .also {
                if (it is Result.Success) {
                    ReminderAlarmScheduler.cancel(context, "note:$noteLocalId")
                    enqueueEventAlarmRecovery(context)
                }
            }
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

    private suspend fun armSaved(result: Result<StoredNoteWithReminder>, user: Long): Result<NoteSaveOutcome> {
        if (result !is Result.Success) return result.map { it.toSaveOutcome() }
        val coordinator = ReminderCoordinator(context)
        val warning = try {
            coordinator.recover(user)
            coordinator.warning("note:${result.data.note.noteLocalId}")
        } catch (error: Exception) {
            Log.e("NoteRepository", "Note saved; alarm registration needs recovery", error)
            enqueueEventAlarmRecovery(context)
            "The note was saved, but its reminder could not be armed. Please reopen the app to retry."
        }
        return Result.Success(result.data.toSaveOutcome().copy(reminderWarning = warning))
    }

    private fun requestSyncAfterSuccess(result: Result<*>) {
        if (result is Result.Success) runCatching(requestSync)
    }
}
