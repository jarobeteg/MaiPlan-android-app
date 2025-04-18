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

/**
 * [EventViewModel] handles operations related to events,
 * including creating a new event and retrieving an event by its Id.
 *
 * @property eventRepository The repository responsible for event-related API calls.
 *
 * @see EventRepository
 * @see Result
 * @see EventResponse
 */
class EventViewModel(private val eventRepository: EventRepository) : ViewModel() {
    private val _createEventResult = MutableLiveData<Result<Unit>>()
    /** Exposes the result of creating a new event. */
    val createEventResult: LiveData<Result<Unit>> get() = _createEventResult

    private val _getEventResult = MutableLiveData<Result<EventResponse>>()
    /** Exposes the result of fetching an event. */
    val getEventResult: LiveData<Result<EventResponse>> get() = _getEventResult

    /**
     * Creates a new event.
     *
     * @param event The [EventCreate] object containing event details.
     *
     * @see EventCreate
     */
    fun createEvent(event: EventCreate) {
        viewModelScope.launch {
            _createEventResult.postValue(eventRepository.createEvent(event))
        }
    }

    /**
     * Fetches a single event by its Id.
     *
     * @param eventId The Id of the event to retrieve.
     *
     */
    fun getEvent(eventId: Int) {
        viewModelScope.launch {
            _getEventResult.postValue(eventRepository.getEvent(eventId))
        }
    }
}