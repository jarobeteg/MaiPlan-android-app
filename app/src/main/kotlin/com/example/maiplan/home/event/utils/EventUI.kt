package com.example.maiplan.home.event.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import java.time.LocalDate
import java.time.LocalTime

data class CalendarEventUI(
    val eventId: Int,
    val reminderId: Int,
    val categoryId: Int,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val title: String,
    val description: String,
    val reminderTime: Long,
    val reminderMessage: String,
    val color: Color,
    val icon: ImageVector
)

fun CalendarEventUI.overlapsHour(hour: Int): Boolean {
    val eventStart = date.atTime(startTime)
    val eventEnd = date.atTime(endTime)

    val hourStart = date.atTime(hour, 0)
    val hourEnd = hourStart.plusHours(1)

    return eventStart < hourEnd && eventEnd > hourStart
}

fun LocalTime.to24hString(): String {
    val h = this.hour
    val m = this.minute
    return "%02d:%02d".format(h, m)
}
