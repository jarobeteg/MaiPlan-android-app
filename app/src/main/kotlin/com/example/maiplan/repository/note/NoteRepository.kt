package com.example.maiplan.repository.note

import com.example.maiplan.database.entities.CategoryEntity
import com.example.maiplan.database.entities.NoteEntity
import com.example.maiplan.network.api.NoteSync
import com.example.maiplan.network.sync.SyncRequest
import com.example.maiplan.network.sync.Syncable
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.category.CategoryLocalDataSource
import com.example.maiplan.repository.orEmptyList
import com.example.maiplan.utils.common.UserSession

class NoteRepository(
    private val remote: NoteRemoteDataSource,
    private val local: NoteLocalDataSource,
    private val localCategory: CategoryLocalDataSource
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

        return NoteSync(
            noteId = noteId,
            serverId = serverId ?: 0,
            userId = userId,
            categoryId = categoryServerId ?: 0,
            reminderId = reminderId ?: 0,
            title = title,
            content = content ?: "",
            createdAt = createdAt,
            updatedAt = updatedAt,
            lastModified = lastModified,
            syncState = syncState,
            isDeleted = isDeleted
        )
    }

    private suspend fun NoteSync.toNoteEntityResolved(): NoteEntity {
        val localCategoryId = if (categoryId == 0) null else localCategory.getCategoryId(categoryId)

        return NoteEntity(
            noteId = noteId,
            serverId = serverId,
            userId = userId,
            categoryId = localCategoryId,
            reminderId = if (reminderId == 0) null else reminderId,
            title = title,
            content = content,
            createdAt = createdAt,
            updatedAt = updatedAt,
            lastModified = lastModified,
            syncState = syncState,
            isDeleted = isDeleted
        )
    }

    suspend fun createNote(note: NoteEntity): Result<Unit> {
        return local.noteInsert(note)
    }

    suspend fun updateNote(note: NoteEntity): Result<Unit> {
        return local.noteUpdate(note)
    }

    suspend fun softDeleteNote(noteId: Int, userId: Int): Result<Unit> {
        return local.softDeleteNote(noteId, userId)
    }

    suspend fun getNotes(userId: Int, categoryId: Int? = null): Result<List<NoteEntity>> {
        return local.getNotes(userId, categoryId)
    }

    suspend fun getCategories(userId: Int): List<CategoryEntity> {
        return localCategory.getCategories(userId).orEmptyList()
    }
}
