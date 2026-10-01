package com.example.maiplan.viewmodel.event

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.maiplan.database.entities.EventEntity
import com.example.maiplan.database.entities.ReminderEntity
import com.example.maiplan.home.event.utils.CalendarEventUI
import com.example.maiplan.repository.event.EventRepository
import com.example.maiplan.repository.event.EventEditSnapshot
import com.example.maiplan.repository.event.StoredEventWithReminder
import com.example.maiplan.repository.Result
import com.example.maiplan.utils.common.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class EventViewModel(private val eventRepository: EventRepository) : ViewModel() {
    private val _saveEventResult = MutableLiveData<Result<StoredEventWithReminder>>(Result.Idle)
    val saveEventResult: LiveData<Result<StoredEventWithReminder>> get() = _saveEventResult

    private val _monthlyEvents =
        MutableStateFlow<Map<LocalDate, List<CalendarEventUI>>>(emptyMap())
    val monthlyEvents = _monthlyEvents.asStateFlow()

    fun createEventWithReminder(reminder: ReminderEntity?, event: EventEntity) {
        viewModelScope.launch {
            _saveEventResult.postValue(Result.Loading)
            _saveEventResult.postValue(eventRepository.createEventWithReminder(reminder, event))
        }
    }

    fun updateEventWithReminder(reminder: ReminderEntity?, event: EventEntity) {
        viewModelScope.launch {
            _saveEventResult.postValue(Result.Loading)
            _saveEventResult.postValue(eventRepository.updateEventWithReminder(reminder, event))
        }
    }

    fun clearSaveResult() {
        _saveEventResult.postValue(Result.Idle)
    }

    fun softDeleteEventWithReminder(
        eventLocalId: Long,
        userLocalId: Long,
        selectedDate: LocalDate
    ) {
        viewModelScope.launch {
            eventRepository.softDeleteEventWithReminder(eventLocalId, userLocalId)
            loadMonth(selectedDate)
        }
    }

    fun loadMonth(date: LocalDate) {
        viewModelScope.launch {
            val monthStart = date.withDayOfMonth(1)
            val monthEndExclusive = monthStart.plusMonths(1)

            val userLocalId = UserSession.userLocalId ?: return@launch
            _monthlyEvents.value = eventRepository.getEventsForMonth(
                monthStart, monthEndExclusive, userLocalId
            )
        }
    }


    suspend fun getEventForEdit(eventLocalId: Long, userLocalId: Long): EventEditSnapshot? =
        eventRepository.getEventForEdit(eventLocalId, userLocalId)

}
