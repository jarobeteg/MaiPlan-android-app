package com.example.maiplan.viewmodel.reminder

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.maiplan.network.api.ReminderCreate
import com.example.maiplan.network.api.ReminderResponse
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.reminder.ReminderRepository
import kotlinx.coroutines.launch

class ReminderViewModel(private val reminderRepository: ReminderRepository) : ViewModel() {
    private val _createReminderResult = MutableLiveData<Result<Int>>()
    val createReminderResult: LiveData<Result<Int>> get() = _createReminderResult

    private val _getReminderResult = MutableLiveData<Result<ReminderResponse>>()
    val getReminderResult: LiveData<Result<ReminderResponse>> get() = _getReminderResult

    private val _reminderList = MutableLiveData<List<ReminderResponse>>()
    val reminderList: LiveData<List<ReminderResponse>> get() = _reminderList

    fun createReminder(reminder: ReminderCreate) {
        viewModelScope.launch {
            _createReminderResult.postValue(reminderRepository.createReminder(reminder))
        }
    }

    suspend fun createReminderSuspending(reminder: ReminderCreate): Result<Int> {
        return reminderRepository.createReminder(reminder)
    }

    fun getReminder(reminderId: Int) {
        viewModelScope.launch {
            _getReminderResult.postValue(reminderRepository.getReminder(reminderId))
        }
    }

    fun getAllReminder(userId: Int) {
        viewModelScope.launch {
            when (val result = reminderRepository.getAllReminders(userId)) {
                is Result.Success -> _reminderList.postValue(result.data)
                else -> _reminderList.postValue(emptyList())
            }
        }
    }
}