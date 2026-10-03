package com.example.maiplan.utils.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.net.toUri
import com.example.maiplan.database.MaiPlanDatabase
import com.example.maiplan.database.entities.EventEntity
import com.example.maiplan.repository.event.EventOccurrence
import com.example.maiplan.repository.event.occurrencesIntersecting
import com.example.maiplan.repository.event.resolveLocal
import com.example.maiplan.repository.event.ruleDatesOnOrAfter
import java.time.*
import java.time.temporal.ChronoUnit

data class NextEventAlarm(
    val eventLocalId: Long,
    val occurrenceDate: LocalDate,
    val triggerAtMillis: Long,
)

fun eventReminderTrigger(event: EventEntity, occurrence: EventOccurrence): Long? {
    val timedStart = occurrence.start
    if (timedStart != null) {
        val minutes = event.reminderOffsetMinutes ?: return null
        return timedStart.toInstant().minus(minutes.toLong(), ChronoUnit.MINUTES).toEpochMilli()
    }
    val lead = event.reminderLeadDays ?: return null
    val minute = event.reminderMinuteOfDay ?: return null
    val clock = LocalTime.of(minute / 60, minute % 60)
    return resolveLocal(
        occurrence.originalStartDate.minusDays(lead.toLong()).atTime(clock), occurrence.zone
    ).toInstant().toEpochMilli()
}

fun nextEventReminderAfter(event: EventEntity, after: Instant): NextEventAlarm? {
    val zone = ZoneId.of(event.zoneId)
    val firstDay = maxOf(event.startDate, after.atZone(zone).toLocalDate())
    return event.ruleDatesOnOrAfter(firstDay)
        .mapNotNull { date ->
            val occurrence = event.occurrencesIntersecting(date, date.plusDays(1))
                .firstOrNull { it.originalStartDate == date } ?: return@mapNotNull null
            eventReminderTrigger(event, occurrence)?.let { trigger ->
                NextEventAlarm(event.eventLocalId, date, trigger)
            }
        }
        .firstOrNull { it.triggerAtMillis > after.toEpochMilli() }
}

class EventAlarmCoordinator(private val context: Context) {
    private val database by lazy { MaiPlanDatabase.getDatabase(context.applicationContext) }
    private val manager by lazy {
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    suspend fun reconcileSeries(
        userLocalId: Long,
        eventLocalId: Long,
        after: Instant = Instant.now(),
    ) {
        val event = database.eventDAO().getEventByLocalId(eventLocalId, userLocalId)
        if (event == null || event.deletedAt != null ||
            (event.reminderOffsetMinutes == null && event.reminderLeadDays == null)) {
            cancel(eventLocalId)
            return
        }
        val next = nextEventReminderAfter(event, after)
        if (next == null) cancel(eventLocalId) else schedule(next)
    }

    suspend fun reconcileAll(userLocalId: Long) {
        val reminders = database.reminderDAO().getAllForUser(userLocalId)
        val activeReminders = reminders.filter { it.deletedAt == null }
            .associateBy { it.reminderLocalId }
        reminders
            .filter { it.deletedAt != null }
            .forEach { AlarmScheduler.cancelAlarm(context, it.reminderLocalId) }
        database.eventDAO().getAllEventIdsForUser(userLocalId).forEach {
            reconcileSeries(userLocalId, it)
            val event = database.eventDAO().getEventByLocalId(it, userLocalId)
            val reminder = event?.reminderLocalId?.let(activeReminders::get)
            if (event != null && event.deletedAt == null &&
                reminder != null) {
                AlarmScheduler.attemptSchedule(context, ReminderData(
                    reminderLocalId = reminder.reminderLocalId,
                    reminderTime = reminder.reminderTime,
                    reminderTitle = event.title,
                    reminderMessage = reminder.message.orEmpty(),
                ))
            } else if (reminder != null) {
                AlarmScheduler.cancelAlarm(context, reminder.reminderLocalId)
            }
        }
        database.noteDAO().getNotes(userLocalId).forEach { note ->
            val reminder = note.reminderLocalId?.let(activeReminders::get) ?: return@forEach
            AlarmScheduler.attemptSchedule(context, ReminderData(
                reminderLocalId = reminder.reminderLocalId,
                reminderTime = reminder.reminderTime,
                reminderTitle = note.title,
                reminderMessage = reminder.message.orEmpty(),
            ))
        }
    }

    fun cancel(eventLocalId: Long) {
        val pending = pendingIntent(eventLocalId, null, false) ?: return
        manager.cancel(pending)
        pending.cancel()
    }

    private fun schedule(next: NextEventAlarm) {
        val pending = requireNotNull(pendingIntent(next.eventLocalId, next, true))
        val exact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || manager.canScheduleExactAlarms()
        if (exact) {
            try {
                manager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, next.triggerAtMillis, pending
                )
                return
            } catch (_: SecurityException) { /* inexact fallback below */ }
        }
        manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.triggerAtMillis, pending)
    }

    private fun pendingIntent(
        eventLocalId: Long, next: NextEventAlarm?, create: Boolean
    ): PendingIntent? {
        val intent = Intent(context, EventReminderReceiver::class.java).apply {
            data = "maiplan://event-reminder/".plus(eventLocalId).toUri()
            putExtra("event_local_id", eventLocalId)
            if (next != null) {
                putExtra("occurrence_date", next.occurrenceDate.toString())
                putExtra("trigger_at", next.triggerAtMillis)
            }
        }
        return PendingIntent.getBroadcast(
            context, 0, intent,
            (if (create) PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_NO_CREATE) or
                PendingIntent.FLAG_IMMUTABLE,
        )
    }

    suspend fun deliverIfCurrent(eventLocalId: Long, date: LocalDate, trigger: Long) {
        val userSyncId = com.example.maiplan.utils.SessionManager(context)
            .getActiveUserSyncId() ?: return
        val user = database.userDAO().getActiveUserBySyncId(userSyncId) ?: return
        val event = database.eventDAO().getEventByLocalId(eventLocalId, user.userLocalId)
        if (event == null || event.deletedAt != null) {
            cancel(eventLocalId)
            return
        }
        val occurrence = event.occurrencesIntersecting(date, date.plusDays(1))
            .firstOrNull { it.originalStartDate == date }
        val now = Instant.now()
        if (occurrence != null && eventReminderTrigger(event, occurrence) == trigger &&
            trigger <= now.toEpochMilli() && now.toEpochMilli() - trigger <= 86_400_000L) {
            NotificationHelper.createNotificationChannel(context)
            NotificationHelper.showNotification(
                context, event.title, event.relativeReminderMessage.orEmpty(),
                (eventLocalId xor (eventLocalId ushr 32)).toInt() xor date.toEpochDay().toInt(),
            )
        }
        reconcileSeries(user.userLocalId, eventLocalId,
            Instant.ofEpochMilli(maxOf(now.toEpochMilli(), trigger) + 1))
    }
}
