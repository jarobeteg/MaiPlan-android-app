package com.example.maiplan.viewmodel.event

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.maiplan.database.entities.EventEntity
import com.example.maiplan.database.entities.ReminderEntity
import com.example.maiplan.home.event.utils.CalendarEventUI
import com.example.maiplan.network.api.EventCreate
import com.example.maiplan.network.api.EventResponse
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.event.EventRepository
import com.example.maiplan.utils.common.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class EventViewModel(private val eventRepository: EventRepository) : ViewModel() {
    private val _createEventResult = MutableLiveData<Result<Unit>>()
    val createEventResult: LiveData<Result<Unit>> get() = _createEventResult

    private val _getEventResult = MutableLiveData<Result<EventResponse>>()
    val getEventResult: LiveData<Result<EventResponse>> get() = _getEventResult

    private var _eventList = MutableLiveData<List<EventResponse>>()
    val eventList: LiveData<List<EventResponse>> get() = _eventList

    private val _monthlyEvents =
        MutableStateFlow<Map<LocalDate, List<CalendarEventUI>>>(emptyMap())
    val monthlyEvents = _monthlyEvents.asStateFlow()

    fun createEventWithReminder(reminder: ReminderEntity?, event: EventEntity) {
        viewModelScope.launch {
            eventRepository.createEventWithReminder(reminder, event)
        }
    }

    fun createEvent(event: EventCreate) {
        viewModelScope.launch {
            _createEventResult.postValue(eventRepository.createEvent(event))
        }
    }

    fun getEvent(eventId: Int) {
        viewModelScope.launch {
            _getEventResult.postValue(eventRepository.getEvent(eventId))
        }
    }

    fun getAllEvent(userId: Int) {
        viewModelScope.launch {
            when (val result = eventRepository.getAllEvents(userId)) {
                is Result.Success -> _eventList.postValue(result.data)
                else -> _eventList.postValue(emptyList())
            }
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

            println("start: $start")
            println("end: $end")

            val events = eventRepository.getEventsForRange(start, end, UserSession.userId)
            val grouped = events.groupBy { it.date }

            _monthlyEvents.value = grouped
        }
    }
}