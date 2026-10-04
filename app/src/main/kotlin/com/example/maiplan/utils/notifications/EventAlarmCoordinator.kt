package com.example.maiplan.utils.notifications

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
