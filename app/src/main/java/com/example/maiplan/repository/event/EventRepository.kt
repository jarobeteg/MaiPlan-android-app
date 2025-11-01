package com.example.maiplan.repository.event

import com.example.maiplan.network.api.EventCreate
import com.example.maiplan.network.api.EventResponse
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.handleRemoteResponse

class EventRepository(private val remoteDataSource: EventRemoteDataSource) {
    suspend fun createEvent(event: EventCreate): Result<Unit> {
        return try {
            handleRemoteResponse(remoteDataSource.createEvent(event))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getEvent(eventId: Int): Result<EventResponse> {
        return try {
            handleRemoteResponse(remoteDataSource.getEvent(eventId))
        } catch (e: Exception){
            Result.Error(e)
        }
    }

    suspend fun getAllEvents(userId: Int): Result<List<EventResponse>> {
        return try {
            handleRemoteResponse(remoteDataSource.getAllEvents(userId))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}