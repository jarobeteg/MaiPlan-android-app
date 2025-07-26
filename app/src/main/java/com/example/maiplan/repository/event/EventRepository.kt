package com.example.maiplan.repository.event

import com.example.maiplan.network.api.EventApi
import com.example.maiplan.network.api.EventCreate
import com.example.maiplan.network.api.EventResponse
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.handleResponse

/**
 * Repository responsible for handling event-related network operations.
 *
 * Acts as an intermediary between the [EventRemoteDataSource] and the rest of the application.
 * Wraps API calls with standardized [Result] types to simplify error and success handling.
 *
 * @property remoteDataSource An instance of [EventRemoteDataSource] used to perform event-related API calls.
 *
 * @see EventRemoteDataSource
 * @see EventApi
 * @see Result
 * @see handleResponse
 */
class EventRepository(private val remoteDataSource: EventRemoteDataSource) {

    /**
     * Creates a new event.
     *
     * @param event The [EventCreate] object containing event data like title, category, time, etc.
     * @return A [Result] indicating success (with `Unit`) or failure ([Exception]).
     */
    suspend fun createEvent(event: EventCreate): Result<Unit> {
        return try {
            handleResponse(remoteDataSource.createEvent(event))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Retrieves a specific event by its unique ID.
     *
     * @param eventId The ID of the event to retrieve.
     * @return A [Result] containing an [EventResponse] on success, or an [Exception] on failure.
     */
    suspend fun getEvent(eventId: Int): Result<EventResponse> {
        return try {
            handleResponse(remoteDataSource.getEvent(eventId))
        } catch (e: Exception){
            Result.Error(e)
        }
    }

    /**
     * Fetches all events associated with a specific user.
     *
     * @param userId The unique identifier of the user.
     * @return A [Result] containing a list of [EventResponse] objects on success, or an [Exception] on failure.
     */
    suspend fun getAllEvents(userId: Int): Result<List<EventResponse>> {
        return try {
            handleResponse(remoteDataSource.getAllEvents(userId))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}