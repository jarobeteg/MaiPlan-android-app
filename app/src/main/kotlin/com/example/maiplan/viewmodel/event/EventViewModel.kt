package com.example.maiplan.viewmodel.event

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.maiplan.database.entities.EventEntity
import com.example.maiplan.database.entities.ReminderEntity
import com.example.maiplan.home.event.utils.CalendarEventUI
import com.example.maiplan.repository.event.EventRepository
import com.example.maiplan.repository.event.StoredEventWithReminder
import com.example.maiplan.repository.Result
import com.example.maiplan.utils.common.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

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
            val start = date.withDayOfMonth(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            val end = date.withDayOfMonth(date.lengthOfMonth())
                .plusDays(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli() - 1

            val userLocalId = UserSession.userLocalId ?: return@launch
            val events = eventRepository.getEventsForRange(start, end, userLocalId)
            val grouped = events.groupBy { it.date }

            _monthlyEvents.value = grouped
        }
    }


    fun getEventById(eventLocalId: Long): StateFlow<CalendarEventUI?> {
        return monthlyEvents
            .map { eventsByDate ->
                eventsByDate
                    .values
                    .flatten()
                    .firstOrNull { it.eventLocalId == eventLocalId }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = null
            )
    }

}
