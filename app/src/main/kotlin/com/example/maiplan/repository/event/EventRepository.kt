package com.example.maiplan.repository.event

import androidx.compose.ui.graphics.Color
import com.example.maiplan.database.entities.EventEntity
import com.example.maiplan.database.entities.ReminderEntity
import com.example.maiplan.home.event.utils.CalendarEventUI
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.category.CategoryLocalDataSource
import com.example.maiplan.repository.reminder.ReminderLocalDataSource
import com.example.maiplan.utils.common.IconData
import java.time.Instant
import java.time.ZoneId

class EventRepository(
    private val local: EventLocalDataSource,
    private val localCategory: CategoryLocalDataSource,
    private val localReminder: ReminderLocalDataSource,
    private val requestSync: () -> Unit = {}
) {
    private suspend fun EventEntity.toCalendarEventUI(): CalendarEventUI {
        val categoryLocalId = requireNotNull(categoryLocalId) {
            "Event $eventLocalId has no Category"
        }
        val category = requireNotNull(localCategory.getCategory(categoryLocalId, userLocalId)) {
            "Category $categoryLocalId was not found for Event $eventLocalId"
        }
        val reminder = reminderLocalId?.let {
            localReminder.getReminder(it, userLocalId)
        }
        val eventZone = ZoneId.of(zoneId)

        return CalendarEventUI(
            eventLocalId = eventLocalId,
            title = title,
            description = description.orEmpty(),
            date = Instant.ofEpochMilli(date).atZone(eventZone).toLocalDate(),
            startTime = Instant.ofEpochMilli(requireNotNull(startTime)).atZone(eventZone).toLocalTime(),
            endTime = Instant.ofEpochMilli(requireNotNull(endTime)).atZone(eventZone).toLocalTime(),
            zoneId = zoneId,
            color = Color(category.color.toULong()),
            icon = IconData.getIconByKey(category.icon),
            reminderLocalId = reminderLocalId,
            categoryLocalId = categoryLocalId,
            reminderTime = reminder?.reminderTime,
            reminderMessage = reminder?.message.orEmpty()
        )
    }

    suspend fun createEventWithReminder(
        reminder: ReminderEntity?,
        event: EventEntity
    ): Result<StoredEventWithReminder> {
        return local.createEventWithReminder(reminder, event).also(::requestSyncAfterSuccess)
    }

    suspend fun updateEventWithReminder(
        reminder: ReminderEntity?,
        event: EventEntity
    ): Result<StoredEventWithReminder> {
        return local.updateEventWithReminder(reminder, event).also(::requestSyncAfterSuccess)
    }

    suspend fun softDeleteEventWithReminder(
        eventLocalId: Long,
        userLocalId: Long
    ): Result<Unit> {
        return local.softDeleteEventWithReminder(eventLocalId, userLocalId)
            .also(::requestSyncAfterSuccess)
    }

    suspend fun getEventsForRange(
        startMillis: Long,
        endMillis: Long,
        userLocalId: Long
    ): List<CalendarEventUI> {
        return local.getEventsForRange(startMillis, endMillis, userLocalId)
            .map { it.toCalendarEventUI() }
    }

    private fun requestSyncAfterSuccess(result: Result<*>) {
        if (result is Result.Success) runCatching(requestSync)
    }
}
