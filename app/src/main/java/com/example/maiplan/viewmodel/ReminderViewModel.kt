package com.example.maiplan.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.maiplan.network.ReminderCreate
import com.example.maiplan.network.ReminderResponse
import com.example.maiplan.repository.ReminderRepository
import com.example.maiplan.repository.Result
import kotlinx.coroutines.launch

class ReminderViewModel(private val reminderRepository: ReminderRepository) : ViewModel() {
    private val _createReminderResult = MutableLiveData<Result<Unit>>()
    val createReminderResult: LiveData<Result<Unit>> get() = _createReminderResult

    private val _getReminderResult = MutableLiveData<Result<ReminderResponse>>()
    val getReminderResult: LiveData<Result<ReminderResponse>> get() = _getReminderResult

    fun createReminder(reminder: ReminderCreate) {
        viewModelScope.launch {
            _createReminderResult.postValue(reminderRepository.createReminder(reminder))
        }
    }

    fun getReminder(reminderId: Int) {
        viewModelScope.launch {
            _getReminderResult.postValue(reminderRepository.getReminder(reminderId))
        }
    }
}