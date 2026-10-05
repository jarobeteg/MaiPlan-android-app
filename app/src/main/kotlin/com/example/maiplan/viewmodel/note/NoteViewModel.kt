package com.example.maiplan.viewmodel.note

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.maiplan.database.entities.CategoryEntity
import com.example.maiplan.database.entities.NoteEntity
import com.example.maiplan.database.entities.ReminderEntity
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.note.NoteRepository
import com.example.maiplan.repository.note.NoteSaveOutcome
import com.example.maiplan.repository.orEmptyList
import kotlinx.coroutines.launch

class NoteViewModel(private val noteRepository: NoteRepository) : ViewModel() {
    private val _noteList = MutableLiveData<List<NoteEntity>>()
    val noteList: LiveData<List<NoteEntity>> get() = _noteList

    private val _categoryList = MutableLiveData<List<CategoryEntity>>()
    val categoryList: LiveData<List<CategoryEntity>> get() = _categoryList

    private val _createNoteResult = MutableLiveData<Result<NoteSaveOutcome>>(Result.Idle)
    val createNoteResult: LiveData<Result<NoteSaveOutcome>> get() = _createNoteResult

    private val _updateNoteResult = MutableLiveData<Result<NoteSaveOutcome>>(Result.Idle)
    val updateNoteResult: LiveData<Result<NoteSaveOutcome>> get() = _updateNoteResult

    private val _deleteNoteResult = MutableLiveData<Result<Unit>>(Result.Idle)
    val deleteNoteResult: LiveData<Result<Unit>> get() = _deleteNoteResult

    private val _pinNoteResult = MutableLiveData<Result<Unit>>(Result.Idle)
    val pinNoteResult: LiveData<Result<Unit>> get() = _pinNoteResult
    private val pinningNoteIds = mutableSetOf<Long>()

    fun loadNotes(userLocalId: Long, categoryLocalId: Long? = null) {
        viewModelScope.launch {
            refreshNotes(userLocalId, categoryLocalId)
        }
    }

    fun loadCategories(userLocalId: Long) {
        viewModelScope.launch {
            _categoryList.postValue(noteRepository.getCategories(userLocalId))
        }
    }

    fun getNote(noteLocalId: Long): NoteEntity? {
        return _noteList.value?.find { it.noteLocalId == noteLocalId }
    }

    fun createNote(note: NoteEntity) {
        viewModelScope.launch {
            _createNoteResult.postValue(Result.Loading)
            val result = noteRepository.createNoteWithReminder(null, note)
            if (result is Result.Success) refreshNotes(note.userLocalId)
            _createNoteResult.postValue(result)
        }
    }

    fun updateNote(note: NoteEntity) {
        viewModelScope.launch {
            _updateNoteResult.postValue(Result.Loading)
            val result = noteRepository.updateNoteWithReminder(null, note)
            if (result is Result.Success) refreshNotes(note.userLocalId)
            _updateNoteResult.postValue(result)
        }
    }

    fun createNoteWithReminder(reminder: ReminderEntity?, note: NoteEntity) {
        viewModelScope.launch {
            _createNoteResult.postValue(Result.Loading)
            val result = noteRepository.createNoteWithReminder(reminder, note)
            if (result is Result.Success) refreshNotes(note.userLocalId)
            _createNoteResult.postValue(result)
        }
    }

    fun updateNoteWithReminder(reminder: ReminderEntity?, note: NoteEntity) {
        viewModelScope.launch {
            _updateNoteResult.postValue(Result.Loading)
            val result = noteRepository.updateNoteWithReminder(reminder, note)
            if (result is Result.Success) refreshNotes(note.userLocalId)
            _updateNoteResult.postValue(result)
        }
    }

    suspend fun getNoteReminder(reminderLocalId: Long?, userLocalId: Long): Result<ReminderEntity?> {
        return noteRepository.getReminder(reminderLocalId, userLocalId)
    }

    fun softDeleteNote(noteLocalId: Long, userLocalId: Long) {
        viewModelScope.launch {
            val result = noteRepository.softDeleteNote(noteLocalId, userLocalId)
            if (result is Result.Success) refreshNotes(userLocalId)
            _deleteNoteResult.postValue(result)
        }
    }

    fun setNotePinned(note: NoteEntity, isPinned: Boolean) {
        if (!pinningNoteIds.add(note.noteLocalId)) return
        viewModelScope.launch {
            try {
                val result = noteRepository.setNotePinned(note.noteLocalId, note.userLocalId, isPinned)
                if (result is Result.Success) refreshNotes(note.userLocalId)
                _pinNoteResult.postValue(result)
            } finally {
                pinningNoteIds.remove(note.noteLocalId)
            }
        }
    }

    fun clearPinResult() {
        _pinNoteResult.postValue(Result.Idle)
    }

    fun clearCreateResult() {
        _createNoteResult.postValue(Result.Idle)
    }

    fun clearUpdateResult() {
        _updateNoteResult.postValue(Result.Idle)
    }

    private suspend fun refreshNotes(userLocalId: Long, categoryLocalId: Long? = null) {
        val result = noteRepository.getNotes(userLocalId, categoryLocalId)
        _noteList.postValue(result.orEmptyList())
    }
}
