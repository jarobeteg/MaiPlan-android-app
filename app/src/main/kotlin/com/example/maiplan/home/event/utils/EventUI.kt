package com.example.maiplan.home.event.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import java.time.LocalDate
import java.time.LocalTime

data class CalendarEventUI(
    val eventId: Int,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val title: String,
    val color: Color,
    val icon: ImageVector
)