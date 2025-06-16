package com.example.maiplan.repository.event

import com.example.maiplan.network.api.EventApi
import com.example.maiplan.network.api.EventCreate
import com.example.maiplan.network.api.EventResponse
import retrofit2.Response

/**
 * Remote data source responsible for communicating directly with the event-related API endpoints via [EventApi].
 *
 * Exposes suspend functions that return raw [Response] objects, to be handled by the repository layer.
 * This class abstracts away the direct interaction with Retrofit from other parts of the application.
 *
 * @property eventApi An instance of [EventApi] used to perform network operations for event resources.
 *
 * @see EventApi
 */
class EventRemoteDataSource(private val eventApi: EventApi) {

    /**
     * Sends a request to create a new event.
     *
     * @param event The [EventCreate] object containing event details such as title, date, time, etc.
     * @return A raw [Response] object indicating success or failure of the creation operation.
     */
    suspend fun createEvent(event: EventCreate): Response<Unit> {
        return eventApi.createEvent(event)
    }

    /**
     * Fetches the details of a specific event by its ID.
     *
     * @param eventId The unique identifier of the event.
     * @return A raw [Response] containing an [EventResponse] object on success or an error body on failure.
     */
    suspend fun getEvent(eventId: Int): Response<EventResponse> {
        return eventApi.getEvent(eventId)
    }
}