package com.example.maiplan.home.event.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.getString
import com.example.maiplan.R
import com.example.maiplan.home.event.utils.CalendarEventUI
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale
import kotlin.math.ceil

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun MonthlyView(
    selectedDate: LocalDate,
    eventsByDate: Map <LocalDate, List<CalendarEventUI>>
) {
    val context = LocalContext.current
    val daysInMonth = selectedDate.lengthOfMonth()
    val firstDayOfMonth = (selectedDate.withDayOfMonth(1).dayOfWeek.value + 6) % 7

    val weekdays = listOf(
        getString(context, R.string.mon),
        getString(context, R.string.tue),
        getString(context, R.string.wed),
        getString(context, R.string.thu),
        getString(context, R.string.fri),
        getString(context, R.string.sat),
        getString(context, R.string.sun)
    )

    val totalCells = firstDayOfMonth + daysInMonth
    val rowCount = ceil(totalCells / 7.0).toInt()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val weekNumberWidth = 12.dp
        val cellSize = (maxWidth - weekNumberWidth) / 7
        val headerHeight = 18.dp
        val rowHeight = (maxHeight - headerHeight) / rowCount

        Column(modifier = Modifier.fillMaxSize()) {
            WeekdayHeaders(weekdays, weekNumberWidth, cellSize, headerHeight)
            CalendarGrid(selectedDate, rowCount, daysInMonth, firstDayOfMonth, weekNumberWidth, cellSize, rowHeight, eventsByDate)
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
    selectedDate: LocalDate,
    rowCount: Int,
    daysInMonth: Int,
    firstDayOfMonth: Int,
    weekNumberWidth: Dp,
    cellSize: Dp,
    rowHeight: Dp,
    eventsByDate: Map <LocalDate, List<CalendarEventUI>>
) {
    for (weekIndex in 0 until rowCount) {
        Row(modifier = Modifier.height(rowHeight)) {
            val weekNumber = getWeekNumberForRow(selectedDate, weekIndex)
            WeekNumberColumn(weekNumber, weekNumberWidth)
            DaysRow(selectedDate, weekIndex, daysInMonth, firstDayOfMonth, cellSize, rowHeight, eventsByDate)
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
fun DaysRow(selectedDate: LocalDate, weekIndex: Int, daysInMonth: Int, firstDayOfMonth: Int, cellSize: Dp, rowHeight: Dp, eventsByDate: Map <LocalDate, List<CalendarEventUI>>) {
    for (dayIndex in 0 until 7) {
        val dayNumber = weekIndex * 7 + dayIndex - firstDayOfMonth + 1
        if (dayNumber in 1..daysInMonth) {
            val cellDate = selectedDate.withDayOfMonth(dayNumber)
            val events = eventsByDate[cellDate].orEmpty()
            DayCell(selectedDate, dayNumber, cellSize, rowHeight, events)
        } else {
            Spacer(modifier = Modifier.width(cellSize))
        }
    }
}

@Composable
fun DayCell(selectedDate: LocalDate, dayNumber: Int, cellSize: Dp, rowHeight: Dp, events: List<CalendarEventUI>) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 3.dp, vertical = 2.dp)
        ) {

            Text(
                text = dayNumber.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiary
            )

            Spacer(modifier = Modifier.height(2.dp))

            events.take(3).forEach { event ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 1.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(event.color, CircleShape)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onTertiary
                    )

                    event.icon.let {
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = it,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onTertiary
                        )
                    }
                }
            }

            if (events.size > 3) {
                Text(
                    text = "+${events.size - 3}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

fun getWeekNumberForRow(selectedDate: LocalDate, weekIndex: Int): Int {
    val weekFields = WeekFields.of(Locale.getDefault())
    val firstOfMonth = selectedDate.withDayOfMonth(1)
    val firstDayOffset = (firstOfMonth.dayOfWeek.value + 6) % 7
    val dateInRow = firstOfMonth.plusDays((weekIndex * 7 - firstDayOffset).toLong())

    return if (dateInRow.month == selectedDate.month) {
        dateInRow.get(weekFields.weekOfYear())
    } else {
        firstOfMonth.get(weekFields.weekOfYear())
    }
}