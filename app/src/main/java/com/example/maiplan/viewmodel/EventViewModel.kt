package com.example.maiplan.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.maiplan.network.EventCreate
import com.example.maiplan.network.EventResponse
import com.example.maiplan.repository.EventRepository
import com.example.maiplan.repository.Result
import kotlinx.coroutines.launch

class EventViewModel(private val eventRepository: EventRepository) : ViewModel() {
    private val _createEventResult = MutableLiveData<Result<Unit>>()
    val createEventResult: LiveData<Result<Unit>> get() = _createEventResult

    private val _getEventResult = MutableLiveData<Result<EventResponse>>()
    val getEventResult: LiveData<Result<EventResponse>> get() = _getEventResult

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
}