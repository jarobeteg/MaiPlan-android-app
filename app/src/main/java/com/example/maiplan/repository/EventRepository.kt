package com.example.maiplan.repository

import com.example.maiplan.network.api.EventApi
import com.example.maiplan.network.api.EventCreate
import com.example.maiplan.network.api.EventResponse

/**
 * Repository responsible for handling event-related network operations.
 *
 * Wraps API calls and returns a [Result] indicating success or failure,
 * abstracting network and error handling from the rest of the app.
 *
 * @property eventApi An instance of [EventApi] for making event network requests.
 *
 * @see EventApi
 * @see Result
 */
class EventRepository(private val eventApi: EventApi) {

    /**
     * Creates a new event.
     *
     * @param event The [EventCreate] object containing event data.
     * @return A [Result] indicating success or failure.
     *
     * @see Result
     * @see EventCreate
     * @see handleResponse
     */
    suspend fun createEvent(event: EventCreate): Result<Unit> {
        return try {
            handleResponse(eventApi.createEvent(event))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Retrieves a specific event by its Id.
     *
     * @param eventId The Id of the event.
     * @return A [Result] containing an [EventResponse] or an error.
     *
     * @see Result
     * @see EventResponse
     * @see handleResponse
     */
    suspend fun getEvent(eventId: Int): Result<EventResponse> {
        return try {
            handleResponse(eventApi.getEvent(eventId))
        } catch (e: Exception){
            Result.Error(e)
        }
    }
}