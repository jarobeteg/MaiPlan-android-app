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
 * [ReminderViewModel] is responsible for managing reminder-related operations
 * and exposing their results to the UI using [LiveData].
 * It acts as an intermediary between the UI and [ReminderRepository].
 *
 * ## Responsibilities:
 * - Creating new reminders.
 * - Fetching a specific reminder by its ID.
 * - Propagating operation results to the UI, including success, error, and loading states.
 *
 * @property reminderRepository The repository used for performing API operations related to reminders.
 *
 * @see ReminderRepository
 * @see Result
 * @see ReminderResponse
 */
class ReminderViewModel(private val reminderRepository: ReminderRepository) : ViewModel() {
    private val _createReminderResult = MutableLiveData<Result<Unit>>()
    /** Emits the result of the reminder creation operation. */
    val createReminderResult: LiveData<Result<Unit>> get() = _createReminderResult

    private val _getReminderResult = MutableLiveData<Result<ReminderResponse>>()
    /** Emits the result of fetching a specific reminder by ID. */
    val getReminderResult: LiveData<Result<ReminderResponse>> get() = _getReminderResult

    /**
     * Creates a new reminder using the given [ReminderCreate] data.
     * The result is posted to [createReminderResult] for UI observation.
     *
     * @param reminder The reminder data to be created.
     */
    fun createReminder(reminder: ReminderCreate) {
        viewModelScope.launch {
            _createReminderResult.postValue(reminderRepository.createReminder(reminder))
        }
    }

    /**
     * Retrieves a reminder by its unique ID.
     * The result is posted to [getReminderResult] for the UI to consume.
     *
     * @param reminderId The unique identifier of the reminder to fetch.
     */
    fun getReminder(reminderId: Int) {
        viewModelScope.launch {
            _getReminderResult.postValue(reminderRepository.getReminder(reminderId))
        }
    }
}