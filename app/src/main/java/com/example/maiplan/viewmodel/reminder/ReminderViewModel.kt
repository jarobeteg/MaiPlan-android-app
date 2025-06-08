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

/**
 * [ReminderViewModel] handles operations related to reminders,
 * including creating a new reminder and retrieving a reminder by its Id.
 *
 * @property reminderRepository The repository responsible for reminder-related API calls.
 *
 * @see com.example.maiplan.repository.reminder.ReminderRepository
 * @see com.example.maiplan.repository.Result
 * @see com.example.maiplan.network.api.ReminderResponse
 */
class ReminderViewModel(private val reminderRepository: ReminderRepository) : ViewModel() {
    private val _createReminderResult = MutableLiveData<Result<Unit>>()
    /** Exposes the result of creating a new reminder. */
    val createReminderResult: LiveData<Result<Unit>> get() = _createReminderResult

    private val _getReminderResult = MutableLiveData<Result<ReminderResponse>>()
    /** Exposes the result of fetching a reminder. */
    val getReminderResult: LiveData<Result<ReminderResponse>> get() = _getReminderResult

    /**
     * Creates a new reminder.
     *
     * @param reminder The [com.example.maiplan.network.api.ReminderCreate] object containing reminder details.
     *
     * @see com.example.maiplan.network.api.ReminderCreate
     */
    fun createReminder(reminder: ReminderCreate) {
        viewModelScope.launch {
            _createReminderResult.postValue(reminderRepository.createReminder(reminder))
        }
    }

    /**
     * Fetches a single reminder by its Id.
     *
     * @param reminderId The Id of the reminder to retrieve.
     */
    fun getReminder(reminderId: Int) {
        viewModelScope.launch {
            _getReminderResult.postValue(reminderRepository.getReminder(reminderId))
        }
    }
}