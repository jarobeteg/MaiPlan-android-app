package com.example.maiplan.repository.event

import com.example.maiplan.network.api.EventApi
import com.example.maiplan.network.api.EventCreate
import com.example.maiplan.network.api.EventResponse
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.handleResponse

/**
 * Repository responsible for handling event-related network operations.
 *
 * Wraps API calls and returns a [com.example.maiplan.repository.Result] indicating success or failure,
 * abstracting network and error handling from the rest of the app.
 *
 * @property eventApi An instance of [com.example.maiplan.network.api.EventApi] for making event network requests.
 *
 * @see com.example.maiplan.network.api.EventApi
 * @see com.example.maiplan.repository.Result
 */
class EventRepository(private val remoteDataSource: EventRemoteDataSource) {

    /**
     * Creates a new event.
     *
     * @param event The [com.example.maiplan.network.api.EventCreate] object containing event data.
     * @return A [com.example.maiplan.repository.Result] indicating success or failure.
     *
     * @see com.example.maiplan.repository.Result
     * @see com.example.maiplan.network.api.EventCreate
     * @see com.example.maiplan.repository.handleResponse
     */
    suspend fun createEvent(event: EventCreate): Result<Unit> {
        return try {
            handleResponse(remoteDataSource.createEvent(event))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Retrieves a specific event by its Id.
     *
     * @param eventId The Id of the event.
     * @return A [Result] containing an [com.example.maiplan.network.api.EventResponse] or an error.
     *
     * @see Result
     * @see com.example.maiplan.network.api.EventResponse
     * @see handleResponse
     */
    suspend fun getEvent(eventId: Int): Result<EventResponse> {
        return try {
            handleResponse(remoteDataSource.getEvent(eventId))
        } catch (e: Exception){
            Result.Error(e)
        }
    }
}