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

/**
 * [EventViewModel] is responsible for managing event-related operations
 * and exposing the corresponding UI state. It interacts with [EventRepository]
 * to perform API calls and updates the UI via [LiveData].
 *
 * ## Responsibilities:
 * - Creating new events.
 * - Fetching a single event by its ID.
 * - Propagating success, error, and loading states to the UI layer.
 *
 * @property eventRepository The repository used to perform API operations related to events.
 *
 * @see EventRepository
 * @see Result
 * @see EventResponse
 */
class EventViewModel(private val eventRepository: EventRepository) : ViewModel() {
    private val _createEventResult = MutableLiveData<Result<Unit>>()
    /** Emits the result of the event creation operation. */
    val createEventResult: LiveData<Result<Unit>> get() = _createEventResult

    private val _getEventResult = MutableLiveData<Result<EventResponse>>()
    /** Emits the result of fetching a single event by ID. */
    val getEventResult: LiveData<Result<EventResponse>> get() = _getEventResult

    /**
     * Creates a new event using the provided [EventCreate] data.
     * The result is emitted via [createEventResult] to notify the UI.
     *
     * @param event The event data to be created.
     */
    fun createEvent(event: EventCreate) {
        viewModelScope.launch {
            _createEventResult.postValue(eventRepository.createEvent(event))
        }
    }

    /**
     * Retrieves an event by its unique ID.
     * The result is emitted via [getEventResult] for UI observation.
     *
     * @param eventId The unique identifier of the event to fetch.
     */
    fun getEvent(eventId: Int) {
        viewModelScope.launch {
            _getEventResult.postValue(eventRepository.getEvent(eventId))
        }
    }
}