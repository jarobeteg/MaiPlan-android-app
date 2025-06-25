package com.example.maiplan.home.event.screens

import android.annotation.SuppressLint
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.getString
import com.example.maiplan.R
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale
import kotlin.math.ceil

/**
 * Displays a monthly calendar view showing the days of the selected month.
 *
 * Layout includes:
 * - Weekday localized headers (Mon, Tue, etc.)
 * - Week numbers on the left.
 * - Calendar cells for each day of the month.
 *
 * @param selectedDate The [LocalDate] representing the month to display.
 *
 * @see WeekdayHeaders
 * @see CalendarGrid
 */
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun MonthlyView(selectedDate: LocalDate) {
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
            CalendarGrid(selectedDate, rowCount, daysInMonth, firstDayOfMonth, weekNumberWidth, cellSize, rowHeight)
        }
    }
}

/**
 * Displays the weekday localized headers (Mon, Tue, etc.) above the calendar grid.
 *
 * @param weekdays A list of localized weekday names.
 * @param weekNumberWidth The width reserved for the week number column.
 * @param cellSize The width of each day cell.
 * @param headerHeight The height of the header row.
 */
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

/**
 * Displays the grid of week rows, each containing the week number and day cells.
 *
 * @param selectedDate The currently selected date used to calculate the correct week number for each row.
 * @param rowCount The total number of rows (weeks) to render in the calendar grid.
 * @param daysInMonth The number of days in the selected month.
 * @param firstDayOfMonth The index of the first weekday of the month (0 = Sunday).
 * @param weekNumberWidth The width reserved for the week number column.
 * @param cellSize The width of each day cell.
 * @param rowHeight The height of each week row (day cell height).
 *
 * @see getWeekNumberForRow
 * @see WeekNumberColumn
 * @see DaysRow
 */
@Composable
fun CalendarGrid(
    selectedDate: LocalDate,
    rowCount: Int,
    daysInMonth: Int,
    firstDayOfMonth: Int,
    weekNumberWidth: Dp,
    cellSize: Dp,
    rowHeight: Dp
) {
    for (weekIndex in 0 until rowCount) {
        Row(modifier = Modifier.height(rowHeight)) {
            val weekNumber = getWeekNumberForRow(selectedDate, weekIndex)
            WeekNumberColumn(weekNumber, weekNumberWidth)
            DaysRow(weekIndex, daysInMonth, firstDayOfMonth, cellSize, rowHeight)
        }
    }
}

/**
 * Displays the week number for a single row.
 *
 * @param weekNumber The week number to display.
 * @param width The width reserved for the week number column.
 */
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

/**
 * Displays a single row of day cells for a given week.
 *
 * @param weekIndex The index of the current week row (starting from 0).
 * @param daysInMonth The total number of days in the month.
 * @param firstDayOfMonth The index of the first day of the month (0 = Sunday).
 * @param cellSize The width of each day cell.
 * @param rowHeight The height of each week row (day cell height).
 */
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

/**
 * Displays a single day cell in the calendar.
 *
 * @param dayNumber The day of the month to display (1–31).
 * @param cellSize The width of the cell.
 * @param rowHeight The height of the cell.
 */
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

/**
 * Returns the week number for a given row in a month-view calendar.
 *
 * Calculates the ISO week number (1–53) based on the row index and selected date.
 * If the date in the row falls outside the current month, it defaults to the first week's number.
 *
 * @param selectedDate A date within the displayed month.
 * @param weekIndex Zero-based index of the calendar row.
 * @return The localized week number for the row.
 */
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