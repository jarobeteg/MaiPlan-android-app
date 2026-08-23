package com.example.maiplan.repository.note

import com.example.maiplan.database.entities.CategoryEntity
import com.example.maiplan.database.entities.NoteEntity
import com.example.maiplan.database.entities.ReminderEntity
import com.example.maiplan.network.api.NoteSync
import com.example.maiplan.network.sync.SyncRequest
import com.example.maiplan.network.sync.Syncable
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.category.CategoryLocalDataSource
import com.example.maiplan.repository.orEmptyList
import com.example.maiplan.repository.map
import com.example.maiplan.utils.common.UserSession
import com.example.maiplan.repository.reminder.ReminderLocalDataSource

data class NoteSaveOutcome(
    val reminderId: Int?,
    val reminderTime: Long?,
    val reminderTitle: String,
    val reminderMessage: String,
)

class NoteRepository(
    private val remote: NoteRemoteDataSource,
    private val local: NoteLocalDataSource,
    private val localCategory: CategoryLocalDataSource,
    private val localReminder: ReminderLocalDataSource,
) : Syncable {

    override suspend fun sync() {
        try {
            val pendingNotesResult = local.getPendingNotes(UserSession.userId!!)
            if (pendingNotesResult is Result.Success) {
                val changes = pendingNotesResult.data.map { it.toNoteSyncResolved() }
                val request = SyncRequest(UserSession.userId!!, changes)
                val response = remote.noteSync(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        body.acknowledged.forEach { local.noteUpsert(it.toNoteEntityResolved()) }
                        body.rejected.forEach { local.deleteNote(it.toNoteEntityResolved()) }
                    }
                }
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    private suspend fun NoteEntity.toNoteSyncResolved(): NoteSync {
        val categoryServerId = categoryId?.let { localCategory.getServerId(it) }
        val reminderServerId = reminderId?.let { localReminder.getServerId(it) }

        return NoteSync(
            noteId = noteId,
            serverId = serverId ?: 0,
            userId = userId,
            categoryId = categoryServerId ?: 0,
            reminderId = reminderServerId ?: 0,
            title = title,
            content = content ?: "",
            createdAt = createdAt,
            updatedAt = updatedAt,
            lastModified = lastModified,
            syncState = syncState,
            isDeleted = isDeleted,
            isPinned = isPinned
        )
    }

    private suspend fun NoteSync.toNoteEntityResolved(): NoteEntity {
        val localCategoryId = if (categoryId == 0) null else localCategory.getCategoryId(categoryId)
        val localReminderId = if (reminderId == 0) null else localReminder.getReminderId(reminderId)

        return NoteEntity(
            noteId = noteId,
            serverId = serverId,
            userId = userId,
            categoryId = localCategoryId,
            reminderId = localReminderId,
            title = title,
            content = content,
            createdAt = createdAt,
            updatedAt = updatedAt,
            lastModified = lastModified,
            syncState = syncState,
            isDeleted = isDeleted,
            isPinned = isPinned
        )
    }

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

    suspend fun softDeleteNote(noteId: Int, userId: Int): Result<Unit> {
        return local.softDeleteNoteWithReminder(noteId, userId)
    }

    suspend fun getReminder(reminderId: Int?): Result<ReminderEntity?> {
        if (reminderId == null) return Result.Success(null)
        return try {
            Result.Success(localReminder.getReminder(reminderId))
        } catch (exception: Exception) {
            Result.Error(exception)
        }
    }

    suspend fun getNotes(userId: Int, categoryId: Int? = null): Result<List<NoteEntity>> {
        return local.getNotes(userId, categoryId)
    }

    suspend fun getCategories(userId: Int): List<CategoryEntity> {
        return localCategory.getCategories(userId).orEmptyList()
    }
}
