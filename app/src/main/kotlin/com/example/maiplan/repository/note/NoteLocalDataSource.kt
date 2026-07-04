package com.example.maiplan.repository.note

import android.content.Context
import com.example.maiplan.database.MaiPlanDatabase
import com.example.maiplan.database.dao.AuthDAO
import com.example.maiplan.database.dao.NoteDAO
import com.example.maiplan.database.entities.AuthEntity
import com.example.maiplan.database.entities.NoteEntity
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

    private val authDao: AuthDAO by lazy {
        database.authDAO()
    }

    suspend fun getPendingNotes(userId: Int): Result<List<NoteEntity>> {
        return handleLocalResponse {
            noteDao.getPendingNotes(userId)
        }
    }

    suspend fun getNote(noteId: Int, userId: Int): Result<NoteEntity> {
        return handleLocalResponse {
            noteDao.getNote(noteId, userId)
        }
    }

    suspend fun getNotes(userId: Int, categoryId: Int? = null): Result<List<NoteEntity>> {
        return handleLocalResponse {
            noteDao.getNotes(userId, categoryId)
        }
    }

    suspend fun noteInsert(note: NoteEntity): Result<Unit> {
        if (note.title.isBlank()) {
            return Result.Failure(EMPTY_NOTE_TITLE_ERROR)
        }

        return handleLocalResponse {
            ensureLocalUserExists(note.userId)
            noteDao.noteInsert(note)
            Unit
        }
    }

    suspend fun noteUpdate(note: NoteEntity): Result<Unit> {
        if (note.title.isBlank()) {
            return Result.Failure(EMPTY_NOTE_TITLE_ERROR)
        }

        return handleLocalResponse {
            noteDao.noteUpdate(
                noteId = note.noteId,
                userId = note.userId,
                title = note.title,
                content = note.content,
                categoryId = note.categoryId
            )
        }
    }

    suspend fun noteUpsert(note: NoteEntity): Result<Unit> {
        return handleLocalResponse {
            ensureLocalUserExists(note.userId)
            noteDao.noteUpsert(note)
        }
    }

    suspend fun softDeleteNote(noteId: Int, userId: Int): Result<Unit> {
        return handleLocalResponse {
            noteDao.softDeleteNote(noteId, userId)
        }
    }

    suspend fun deleteNote(note: NoteEntity): Result<Unit> {
        return handleLocalResponse {
            noteDao.deleteNote(note)
        }
    }

    private suspend fun ensureLocalUserExists(userId: Int) {
        if (authDao.getUserById(userId) != null) return

        val email = UserSession.email ?: throw IllegalStateException("No local auth row for note user $userId")
        val username = UserSession.username ?: throw IllegalStateException("No local auth row for note user $userId")

        authDao.authSync(
            AuthEntity(
                userId = userId,
                email = email,
                username = username,
                passwordHash = "pseudo",
                syncState = 0
            )
        )
    }
}
