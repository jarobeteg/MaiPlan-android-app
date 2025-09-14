package com.example.maiplan.viewmodel.event

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.maiplan.network.api.EventCreate
import com.example.maiplan.network.api.EventResponse
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.event.EventRepository
import kotlinx.coroutines.launch

class EventViewModel(private val eventRepository: EventRepository) : ViewModel() {
    private val _createEventResult = MutableLiveData<Result<Unit>>()
    val createEventResult: LiveData<Result<Unit>> get() = _createEventResult

    private val _getEventResult = MutableLiveData<Result<EventResponse>>()
    val getEventResult: LiveData<Result<EventResponse>> get() = _getEventResult

    private var _eventList = MutableLiveData<List<EventResponse>>()
    val eventList: LiveData<List<EventResponse>> get() = _eventList

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
}