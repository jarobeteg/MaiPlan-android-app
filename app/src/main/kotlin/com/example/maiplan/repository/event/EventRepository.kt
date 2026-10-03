package com.example.maiplan.repository.event

import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Event
import com.example.maiplan.database.entities.EventEntity
import com.example.maiplan.database.entities.ReminderEntity
import com.example.maiplan.home.event.utils.CalendarEventUI
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.category.CategoryLocalDataSource
import com.example.maiplan.repository.reminder.ReminderLocalDataSource
import com.example.maiplan.utils.common.IconData
import com.example.maiplan.utils.notifications.AlarmScheduler
import com.example.maiplan.utils.notifications.EventAlarmCoordinator
import com.example.maiplan.utils.notifications.ReminderData
import com.example.maiplan.utils.notifications.enqueueEventAlarmRecovery
import java.time.LocalDate
import kotlinx.coroutines.CancellationException

data class EventEditSnapshot(
    val event: EventEntity,
    val reminder: ReminderEntity?,
)

class EventRepository(
    private val context: Context,
    private val local: EventLocalDataSource,
    private val localCategory: CategoryLocalDataSource,
    private val localReminder: ReminderLocalDataSource,
    private val requestSync: () -> Unit = {}
) {
    private val coordinator = EventAlarmCoordinator(context.applicationContext)
    private suspend fun EventEntity.toCalendarEventUI(day: EventDayEntry): CalendarEventUI {
        val category = categoryLocalId?.let { localCategory.getCategory(it, userLocalId) }
        val reminder = reminderLocalId?.let {
            localReminder.getReminder(it, userLocalId)
        }
        return CalendarEventUI(
            day = day,
            eventLocalId = eventLocalId,
            title = title,
            description = description.orEmpty(),
            date = day.visibleDate,
            startTime = day.occurrence.start?.toLocalTime(),
            endTime = day.occurrence.end?.toLocalTime(),
            zoneId = zoneId,
            color = category?.let { Color(it.color.toULong()) } ?: Color(0xFF64748B),
            icon = category?.let { IconData.getIconByKey(it.icon) } ?: Icons.Rounded.Event,
            reminderLocalId = reminderLocalId,
            categoryLocalId = categoryLocalId,
            reminderTime = reminder?.reminderTime,
            reminderMessage = reminder?.message.orEmpty(),
            hasRelativeReminder = reminderOffsetMinutes != null || reminderLeadDays != null,
            recurrenceFrequency = recurrenceFrequency,
        )
    }

    suspend fun createEventWithReminder(
        reminder: ReminderEntity?,
        event: EventEntity
    ): Result<StoredEventWithReminder> {
        val result = local.createEventWithReminder(reminder, event)
        if (result is Result.Success) {
            try {
                scheduleAbsolute(result.data)
                coordinator.reconcileSeries(event.userLocalId, result.data.event.eventLocalId)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Log.e("EventRepository", "Could not schedule new event reminder", error)
                enqueueEventAlarmRecovery(context)
            }
            requestSyncAfterSuccess(result)
        }
        return result
    }

    suspend fun updateEventWithReminder(
        reminder: ReminderEntity?,
        event: EventEntity
    ): Result<StoredEventWithReminder> {
        val result = local.updateEventWithReminder(reminder, event)
        if (result is Result.Success) {
            try {
                result.data.removedReminderLocalId?.let { AlarmScheduler.cancelAlarm(context, it) }
                scheduleAbsolute(result.data)
                coordinator.reconcileSeries(event.userLocalId, result.data.event.eventLocalId)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Log.e("EventRepository", "Could not update event reminder alarm", error)
                enqueueEventAlarmRecovery(context)
            }
            requestSyncAfterSuccess(result)
        }
        return result
    }

    suspend fun softDeleteEventWithReminder(
        eventLocalId: Long,
        userLocalId: Long
    ): Result<Long?> {
        val result = local.softDeleteEventWithReminder(eventLocalId, userLocalId)
        if (result is Result.Success) {
            try {
                result.data?.let { AlarmScheduler.cancelAlarm(context, it) }
                coordinator.reconcileSeries(userLocalId, eventLocalId)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Log.e("EventRepository", "Could not cancel event reminder alarm", error)
                enqueueEventAlarmRecovery(context)
            }
            requestSyncAfterSuccess(result)
        }
        return result
    }

    suspend fun getEventsForMonth(
        monthStart: LocalDate,
        monthEndExclusive: LocalDate,
        userLocalId: Long,
    ): Map<LocalDate, List<CalendarEventUI>> {
        val rows = local.getEventsForMonth(userLocalId, monthStart, monthEndExclusive)
        val entries = mutableListOf<CalendarEventUI>()
        for (event in rows) {
            for (occurrence in event.occurrencesIntersecting(monthStart, monthEndExclusive)) {
                for (day in occurrence.visibleDays(monthStart, monthEndExclusive)) {
                    entries += event.toCalendarEventUI(day)
                }
            }
        }
        return entries.groupBy { it.date }
    }

    suspend fun getEventForEdit(eventLocalId: Long, userLocalId: Long): EventEditSnapshot? {
        val event = local.getEvent(eventLocalId, userLocalId)
            ?.takeIf { it.deletedAt == null } ?: return null
        val reminder = event.reminderLocalId?.let {
            localReminder.getReminder(it, userLocalId)
        }
        return EventEditSnapshot(event, reminder)
    }

    private fun requestSyncAfterSuccess(result: Result<*>) {
        if (result is Result.Success) runCatching(requestSync)
    }

    private fun scheduleAbsolute(stored: StoredEventWithReminder) {
        val reminder = stored.reminder ?: return
        AlarmScheduler.attemptSchedule(context, ReminderData(
            reminderLocalId = reminder.reminderLocalId,
            reminderTime = reminder.reminderTime,
            reminderTitle = stored.event.title,
            reminderMessage = reminder.message.orEmpty(),
        ))
    }
}
