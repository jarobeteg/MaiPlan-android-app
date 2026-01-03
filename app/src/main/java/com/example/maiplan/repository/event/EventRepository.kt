package com.example.maiplan.repository.event

import com.example.maiplan.database.entities.EventEntity
import com.example.maiplan.database.entities.ReminderEntity
import com.example.maiplan.database.entities.toEventSEntity
import com.example.maiplan.database.entities.toEventSync
import com.example.maiplan.network.api.EventCreate
import com.example.maiplan.network.api.EventResponse
import com.example.maiplan.network.api.EventSync
import com.example.maiplan.network.sync.SyncRequest
import com.example.maiplan.network.sync.Syncable
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.handleLocalResponse
import com.example.maiplan.repository.handleRemoteResponse
import com.example.maiplan.utils.common.UserSession

class EventRepository(
    private val remote: EventRemoteDataSource,
    private val local: EventLocalDataSource
) : Syncable {

    override suspend fun sync() {
        try {
            val pendingEventsResult = local.getPendingEvents(UserSession.userId!!)
            if (pendingEventsResult is Result.Success) {
                val events: List<EventEntity> = pendingEventsResult.data
                val changes: MutableList<EventSync> = mutableListOf()
                events.map { changes.add(it.toEventSync()) }
                val request: SyncRequest<EventSync> = SyncRequest(UserSession.userId!!, changes)
                val response = remote.eventSync(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        if (body.acknowledged.isNotEmpty()) body.acknowledged.map { local.eventUpsert(it.toEventSEntity()) }
                        if (body.rejected.isNotEmpty()) body.rejected.map { local.deleteEvent(it.toEventSEntity()) }
                    }
                }
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

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