package com.example.maiplan.repository.event

import com.example.maiplan.database.entities.EventEntity
import com.example.maiplan.database.entities.ReminderEntity
import com.example.maiplan.network.api.EventCreate
import com.example.maiplan.network.api.EventResponse
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.handleLocalResponse
import com.example.maiplan.repository.handleRemoteResponse

class EventRepository(
    private val remote: EventRemoteDataSource,
    private val local: EventLocalDataSource
) {
    suspend fun createEventWithReminder(reminder: ReminderEntity?, event: EventEntity): Result<Unit> {
        return try {
            handleLocalResponse { local.createEventWithReminder(reminder, event) }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun createEvent(event: EventCreate): Result<Unit> {
        return try {
            handleRemoteResponse(remote.createEvent(event))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getEvent(eventId: Int): Result<EventResponse> {
        return try {
            handleRemoteResponse(remote.getEvent(eventId))
        } catch (e: Exception){
            Result.Error(e)
        }
    }

    suspend fun getAllEvents(userId: Int): Result<List<EventResponse>> {
        return try {
            handleRemoteResponse(remote.getAllEvents(userId))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}