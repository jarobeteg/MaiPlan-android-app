package com.example.maiplan.repository

import com.example.maiplan.network.EventApi
import com.example.maiplan.network.EventCreate
import com.example.maiplan.network.EventResponse

class EventRepository(private val eventApi: EventApi) {
    suspend fun createEvent(event: EventCreate): Result<Unit> {
        return try {
            handleResponse(eventApi.createEvent(event))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getEvent(eventId: Int): Result<EventResponse> {
        return try {
            handleResponse(eventApi.getEvent(eventId))
        } catch (e: Exception){
            Result.Error(e)
        }
    }
}