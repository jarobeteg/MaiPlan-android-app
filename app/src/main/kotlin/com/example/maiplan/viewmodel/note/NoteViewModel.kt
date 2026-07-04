package com.example.maiplan.viewmodel.note

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.maiplan.database.entities.CategoryEntity
import com.example.maiplan.database.entities.NoteEntity
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.note.NoteRepository
import com.example.maiplan.repository.orEmptyList
import kotlinx.coroutines.launch

class NoteViewModel(private val noteRepository: NoteRepository) : ViewModel() {
    private val _noteList = MutableLiveData<List<NoteEntity>>()
    val noteList: LiveData<List<NoteEntity>> get() = _noteList

    private val _categoryList = MutableLiveData<List<CategoryEntity>>()
    val categoryList: LiveData<List<CategoryEntity>> get() = _categoryList

    private val _createNoteResult = MutableLiveData<Result<Unit>>(Result.Idle)
    val createNoteResult: LiveData<Result<Unit>> get() = _createNoteResult

    private val _updateNoteResult = MutableLiveData<Result<Unit>>(Result.Idle)
    val updateNoteResult: LiveData<Result<Unit>> get() = _updateNoteResult

    private val _deleteNoteResult = MutableLiveData<Result<Unit>>(Result.Idle)
    val deleteNoteResult: LiveData<Result<Unit>> get() = _deleteNoteResult

    fun loadNotes(userId: Int, categoryId: Int? = null) {
        viewModelScope.launch {
            refreshNotes(userId, categoryId)
        }
    }

    fun loadCategories(userId: Int) {
        viewModelScope.launch {
            _categoryList.postValue(noteRepository.getCategories(userId))
        }
    }

    fun getNote(noteId: Int): NoteEntity? {
        return _noteList.value?.find { it.noteId == noteId }
    }

    fun createNote(note: NoteEntity) {
        viewModelScope.launch {
            _createNoteResult.postValue(Result.Loading)
            val result = noteRepository.createNote(note)
            if (result is Result.Success) refreshNotes(note.userId)
            _createNoteResult.postValue(result)
        }
    }

    fun updateNote(note: NoteEntity) {
        viewModelScope.launch {
            _updateNoteResult.postValue(Result.Loading)
            val result = noteRepository.updateNote(note)
            if (result is Result.Success) refreshNotes(note.userId)
            _updateNoteResult.postValue(result)
        }
    }

    fun softDeleteNote(noteId: Int, userId: Int) {
        viewModelScope.launch {
            val result = noteRepository.softDeleteNote(noteId, userId)
            if (result is Result.Success) refreshNotes(userId)
            _deleteNoteResult.postValue(result)
        }
    }

    fun clearCreateResult() {
        _createNoteResult.postValue(Result.Idle)
    }

    fun clearUpdateResult() {
        _updateNoteResult.postValue(Result.Idle)
    }

    private suspend fun refreshNotes(userId: Int, categoryId: Int? = null) {
        val result = noteRepository.getNotes(userId, categoryId)
        _noteList.postValue(result.orEmptyList())
    }
}
