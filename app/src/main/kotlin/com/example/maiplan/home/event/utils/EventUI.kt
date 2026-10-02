package com.example.maiplan.home.event.utils

import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.maiplan.repository.event.EventDayEntry
import java.time.LocalDate
import java.time.LocalTime

data class CalendarEventUI(
    val day: EventDayEntry,
    val eventLocalId: Long,
    val reminderLocalId: Long?,
    val categoryLocalId: Long?,
    val date: LocalDate,
    val startTime: LocalTime?,
    val endTime: LocalTime?,
    val zoneId: String,
    val title: String,
    val description: String,
    val reminderTime: Long?,
    val reminderMessage: String,
    val hasRelativeReminder: Boolean,
    val recurrenceFrequency: String?,
    val color: Color,
    val icon: ImageVector
) {
    val listKey: String get() = "${day.occurrence.key}:${day.visibleDate}"
    val isTimed: Boolean get() = day.occurrence.isTimed
    val isRecurring: Boolean get() = recurrenceFrequency != null
}

fun CalendarEventUI.overlapsHour(hour: Int): Boolean {
    val startClock = startTime ?: return false
    val endClock = endTime ?: return false
    val eventStart = date.atTime(startClock)
    val eventEnd = date.atTime(endClock)

    val hourStart = date.atTime(hour, 0)
    val hourEnd = hourStart.plusHours(1)

    return eventStart < hourEnd && eventEnd > hourStart
}

fun LocalTime.to24hString(): String {
    val h = this.hour
    val m = this.minute
    return "%02d:%02d".format(h, m)
}

val LocalDateSaver = Saver<LocalDate, String>(
    save = { it.toString() },
    restore = { LocalDate.parse(it) }
)
