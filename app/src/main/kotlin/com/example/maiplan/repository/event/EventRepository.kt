package com.example.maiplan.repository.event

import androidx.compose.ui.graphics.Color
import com.example.maiplan.database.entities.EventEntity
import com.example.maiplan.database.entities.ReminderEntity
import com.example.maiplan.database.entities.toEventEntity
import com.example.maiplan.database.entities.toEventSync
import com.example.maiplan.home.event.utils.CalendarEventUI
import com.example.maiplan.network.api.EventCreate
import com.example.maiplan.network.api.EventResponse
import com.example.maiplan.network.api.EventSync
import com.example.maiplan.network.sync.SyncRequest
import com.example.maiplan.network.sync.Syncable
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.category.CategoryLocalDataSource
import com.example.maiplan.repository.handleLocalResponse
import com.example.maiplan.repository.handleRemoteResponse
import com.example.maiplan.repository.reminder.ReminderLocalDataSource
import com.example.maiplan.utils.common.IconData
import com.example.maiplan.utils.common.UserSession
import java.time.Instant
import java.time.ZoneId

class EventRepository(
    private val remote: EventRemoteDataSource,
    private val local: EventLocalDataSource,
    private val localCategory: CategoryLocalDataSource,
    private val localReminder: ReminderLocalDataSource
) : Syncable {

    override suspend fun sync() {
        try {
            val pendingEventsResult = local.getPendingEvents(UserSession.userId!!)
            if (pendingEventsResult is Result.Success) {
                val events: List<EventEntity> = pendingEventsResult.data
                val changes: MutableList<EventSync> = mutableListOf()

                for (event in events) {
                    val eventSync = event.toEventSyncResolved()

                    if (eventSync.categoryId == 0 || eventSync.reminderId == 0) {
                        continue
                    }

                    changes.add(eventSync)
                }

                val request: SyncRequest<EventSync> = SyncRequest(UserSession.userId!!, changes)
                val response = remote.eventSync(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        if (body.acknowledged.isNotEmpty()) body.acknowledged.map { local.eventUpsert(it.toEventEntityResolved()) }
                        if (body.rejected.isNotEmpty()) body.rejected.map { local.deleteEvent(it.toEventEntityResolved()) }
                    }
                }
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    private suspend fun EventEntity.toEventSyncResolved(): EventSync {
        val categoryServerId = categoryId?.let { localCategory.getServerId(it) }
        val reminderServerId = reminderId?.let { localReminder.getServerId(it) }

        return EventSync(
            eventId = eventId,
            serverId = serverId ?: 0,
            userId = this.userId,
            categoryId = categoryServerId ?: 0,
            reminderId = reminderServerId ?: 0,
            title = this.title,
            description = this.description ?: "",
            date = this.date,
            startTime = this.startTime ?: 0,
            endTime = this.endTime ?: 0,
            priority = this.priority,
            location = this.location ?: "",
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            lastModified = this.lastModified,
            syncState = this.syncState,
            isDeleted = this.isDeleted
        )
    }

    private suspend fun EventSync.toEventEntityResolved(): EventEntity {
        val eventEntity = local.getEvent(this.eventId)
        val categoryId = eventEntity.categoryId
        val reminderId = eventEntity.reminderId

        return EventEntity(
            eventId = eventId,
            serverId = serverId,
            userId = this.userId,
            categoryId = categoryId,
            reminderId = reminderId,
            title = this.title,
            description = this.description,
            date = this.date,
            startTime = this.startTime,
            endTime = this.endTime,
            priority = this.priority,
            location = this.location,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            lastModified = this.lastModified,
            syncState = this.syncState,
            isDeleted = this.isDeleted
        )
    }

    private suspend fun EventEntity.toCalendarEventUI(): CalendarEventUI {
        val category = localCategory.getCategory(this.categoryId, this.userId)

        return CalendarEventUI(
            eventId = this.eventId,
            title = this.title,
            description = this.description!!,
            date = Instant.ofEpochMilli(this.date).atZone(ZoneId.systemDefault()).toLocalDate(),
            startTime = Instant.ofEpochMilli(this.startTime!!).atZone(ZoneId.systemDefault()).toLocalTime(),
            endTime = Instant.ofEpochMilli(this.endTime!!).atZone(ZoneId.systemDefault()).toLocalTime(),
            color = Color(category.color.toULong()),
            icon = IconData.getIconByKey(category.icon)
        )
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

    suspend fun getEventsForRange(startMillis: Long, endMillis: Long, userId: Int?): List<CalendarEventUI> {
        var result: List<CalendarEventUI>
        val events = local.getEventForRange(startMillis, endMillis, userId!!)
        result = events.map { it.toCalendarEventUI() }
        return result
    }
}