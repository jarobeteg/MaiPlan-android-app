package com.example.maiplan.screens.home.event

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.getString
import com.example.maiplan.R
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale

@Composable
fun MonthlyView(selectedDate: LocalDate, context: Context) {
    val daysInMonth = selectedDate.lengthOfMonth()
    val firstDayOfMonth = selectedDate.withDayOfMonth(1).dayOfWeek.value % 7

    val weekFields = WeekFields.of(Locale.getDefault())
    val startWeek = selectedDate.withDayOfMonth(1).get(weekFields.weekOfWeekBasedYear())
    val endWeek = selectedDate.withDayOfMonth(daysInMonth).get(weekFields.weekOfWeekBasedYear())

    val weekNumbers = (startWeek..endWeek).toList()
    val weekdays = listOf(
        getString(context, R.string.mon),
        getString(context, R.string.tue),
        getString(context, R.string.wed),
        getString(context, R.string.thu),
        getString(context, R.string.fri),
        getString(context, R.string.sat),
        getString(context, R.string.sun)
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val weekNumberWidth = 12.dp
        val cellSize = (maxWidth - weekNumberWidth) / 7
        val headerHeight = 18.dp
        val rowHeight = (maxHeight - headerHeight) / weekNumbers.size

        Column(modifier = Modifier.fillMaxSize()) {
            WeekdayHeaders(weekdays, weekNumberWidth, cellSize, headerHeight)
            CalendarGrid(weekNumbers, daysInMonth, firstDayOfMonth, weekNumberWidth, cellSize, rowHeight)
        }
    }
}

@Composable
fun WeekdayHeaders(weekdays: List<String>, weekNumberWidth: Dp, cellSize: Dp, headerHeight: Dp) {
    Row(modifier = Modifier.height(headerHeight)) {
        Spacer(modifier = Modifier.width(weekNumberWidth))
        weekdays.forEach { day ->
            Box(
                modifier = Modifier.width(cellSize).fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiary
                )
            }
        }
    }
}

@Composable
fun CalendarGrid(
    weekNumbers: List<Int>,
    daysInMonth: Int,
    firstDayOfMonth: Int,
    weekNumberWidth: Dp,
    cellSize: Dp,
    rowHeight: Dp
) {
    weekNumbers.forEachIndexed { weekIndex, weekNumber ->
        Row(modifier = Modifier.height(rowHeight)) {
            WeekNumberColumn(weekNumber, weekNumberWidth)
            DaysRow(weekIndex, daysInMonth, firstDayOfMonth, cellSize, rowHeight)
        }
    }
}

@Composable
fun WeekNumberColumn(weekNumber: Int, width: Dp) {
    Box(
        modifier = Modifier.width(width).fillMaxHeight(),
        contentAlignment = Alignment.TopCenter
    ) {
        Text(
            text = weekNumber.toString(),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onTertiary
        )
    }
}

@Composable
fun DaysRow(weekIndex: Int, daysInMonth: Int, firstDayOfMonth: Int, cellSize: Dp, rowHeight: Dp) {
    for (dayIndex in 0 until 7) {
        val dayNumber = weekIndex * 7 + dayIndex - firstDayOfMonth + 1
        if (dayNumber in 1..daysInMonth) {
            DayCell(dayNumber, cellSize, rowHeight)
        } else {
            Spacer(modifier = Modifier.width(cellSize))
        }
    }
}

@Composable
fun DayCell(dayNumber: Int, cellSize: Dp, rowHeight: Dp) {
    Card(
        modifier = Modifier
            .width(cellSize)
            .height(rowHeight)
            .padding(0.5.dp),
        shape = RoundedCornerShape(2.dp),
        colors = CardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiary,
            disabledContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
            disabledContentColor = MaterialTheme.colorScheme.onTertiary
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                text = "$dayNumber",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}