package com.example.maiplan.repository.event

import androidx.compose.ui.graphics.Color
import com.example.maiplan.database.entities.EventEntity
import com.example.maiplan.database.entities.ReminderEntity
import com.example.maiplan.home.event.utils.CalendarEventUI
import com.example.maiplan.network.api.EventCreate
import com.example.maiplan.network.api.EventResponse
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.category.CategoryLocalDataSource
import com.example.maiplan.repository.handleLocalResponse
import com.example.maiplan.repository.handleRemoteResponse
import com.example.maiplan.repository.reminder.ReminderLocalDataSource
import com.example.maiplan.utils.common.IconData
import java.time.Instant
import java.time.ZoneId

class EventRepository(
    private val remote: EventRemoteDataSource,
    private val local: EventLocalDataSource,
    private val localCategory: CategoryLocalDataSource,
    private val localReminder: ReminderLocalDataSource
) {

    private suspend fun EventEntity.toCalendarEventUI(): CalendarEventUI {
        val categoryLocalId = requireNotNull(this.categoryLocalId) {
            "Event $eventId has no Category"
        }

        val category = requireNotNull(localCategory.getCategory(categoryLocalId, userLocalId)) {
            "Category $categoryLocalId was not found for Event $eventId"
        }

        var reminderTime = 0L
        var reminderMessage = ""

        if (this.reminderId != null) {
            val reminder = localReminder.getReminder(this.reminderId)
            reminderTime = reminder.reminderTime
            reminderMessage = reminder.message.toString()
        }

        return CalendarEventUI(
            eventId = this.eventId,
            title = this.title,
            description = this.description!!,
            date = Instant.ofEpochMilli(this.date).atZone(ZoneId.systemDefault()).toLocalDate(),
            startTime = Instant.ofEpochMilli(this.startTime!!).atZone(ZoneId.systemDefault())
                .toLocalTime(),
            endTime = Instant.ofEpochMilli(this.endTime!!).atZone(ZoneId.systemDefault())
                .toLocalTime(),
            color = Color(category.color.toULong()),
            icon = IconData.getIconByKey(category.icon),
            reminderId = this.reminderId ?: 0,
            categoryLocalId = this.categoryLocalId ?: 0,
            reminderTime = reminderTime,
            reminderMessage = reminderMessage
        )
    }

    suspend fun createEventWithReminder(reminder: ReminderEntity?, event: EventEntity): Result<Unit> {
        return try {
            handleLocalResponse { local.createEventWithReminder(reminder, event) }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun updateEventWithReminder(reminder: ReminderEntity?, event: EventEntity): Result<Unit> {
        return try {
            handleLocalResponse { local.updateEventWithReminder(reminder, event) }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun softDeleteReminder(reminderId: Int?, userLocalId: Long): Result<Unit> {
        if (reminderId != null) {
            return localReminder.softDeleteReminder(reminderId, userLocalId)
        }
        return Result.Idle
    }

    suspend fun softDeleteEvent(eventId: Int, userLocalId: Long): Result<Unit> {
        return local.softDeleteEvent(eventId, userLocalId)
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

    suspend fun getAllEvents(userLocalId: Long): Result<List<EventResponse>> {
        return try {
            handleRemoteResponse(remote.getAllEvents(userLocalId))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getEventsForRange(startMillis: Long, endMillis: Long, userLocalId: Long?): List<CalendarEventUI> {
        var result: List<CalendarEventUI>
        val events = local.getEventForRange(startMillis, endMillis, userLocalId!!)
        result = events.map { it.toCalendarEventUI() }
        return result
    }
}