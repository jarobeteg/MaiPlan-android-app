package com.example.maiplan.repository.event

import com.example.maiplan.network.api.EventApi
import com.example.maiplan.network.api.EventCreate
import com.example.maiplan.network.api.EventResponse
import retrofit2.Response

class EventRemoteDataSource(private val eventApi: EventApi) {

    suspend fun createEvent(event: EventCreate): Response<Unit> {
        return eventApi.createEvent(event)
    }

    suspend fun getEvent(eventId: Int): Response<EventResponse> {
        return eventApi.getEvent(eventId)
    }
}