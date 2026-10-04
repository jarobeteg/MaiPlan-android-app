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
import com.example.maiplan.utils.notifications.ReminderCoordinator
import com.example.maiplan.utils.notifications.ReminderAlarmScheduler
import com.example.maiplan.utils.notifications.enqueueEventAlarmRecovery
import java.time.LocalDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
    private val coordinator = ReminderCoordinator(context.applicationContext)
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
        val result = withContext(NonCancellable + Dispatchers.IO) {
            local.createEventWithReminder(reminder, event).let { armSaved(it, event.userLocalId) }
        }
        if (result is Result.Success) {
            requestSyncAfterSuccess(result)
        }
        return result
    }

    suspend fun updateEventWithReminder(
        reminder: ReminderEntity?,
        event: EventEntity
    ): Result<StoredEventWithReminder> {
        val result = withContext(NonCancellable + Dispatchers.IO) {
            local.updateEventWithReminder(reminder, event).let { armSaved(it, event.userLocalId) }
        }
        if (result is Result.Success) {
            requestSyncAfterSuccess(result)
        }
        return result
    }

    suspend fun softDeleteEventWithReminder(
        eventLocalId: Long,
        userLocalId: Long
    ): Result<Unit> {
        val result = local.softDeleteEventWithReminder(eventLocalId, userLocalId)
        if (result is Result.Success) {
            try {
                ReminderAlarmScheduler.cancel(context, "event:$eventLocalId")
                coordinator.recover(userLocalId)
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

    private suspend fun armSaved(result: Result<StoredEventWithReminder>, user: Long): Result<StoredEventWithReminder> {
        if (result !is Result.Success) return result
        val warning = try {
            coordinator.recover(user)
            coordinator.warning("event:${result.data.event.eventLocalId}")
        } catch (error: Exception) {
            Log.e("EventRepository", "Event saved; alarm registration needs recovery", error)
            enqueueEventAlarmRecovery(context)
            "The event was saved, but its reminder could not be armed. Please reopen the app to retry."
        }
        return Result.Success(result.data.copy(reminderWarning = warning))
    }
}
