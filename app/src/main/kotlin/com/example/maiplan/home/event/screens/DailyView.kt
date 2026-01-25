package com.example.maiplan.home.event.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import com.example.maiplan.home.event.utils.CalendarEventUI
import com.example.maiplan.home.event.utils.to24hString
import com.example.maiplan.utils.toEpochMillis
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun DailyView(
    selectedDate: LocalDate,
    eventsByDate: Map<LocalDate, List<CalendarEventUI>>,
    onEditEvent: (Int) -> Unit
) {
    val eventsForDay = eventsByDate[selectedDate].orEmpty()
    val defaultHourHeight = 72.dp
    val perEventHeight = 100.dp

    val eventsByHour = remember(eventsForDay, selectedDate) {
        buildEventsByStartHour(eventsForDay, selectedDate)
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(24) { hour ->
            val hourEvents = eventsByHour[hour].orEmpty()
            val rowHeight = max(defaultHourHeight, perEventHeight * hourEvents.size + 16.dp)

            DailyHourRow(
                hour = hour,
                rowHeight = rowHeight,
                events = hourEvents,
                onEventClick = onEditEvent
            )
        }
    }
}

fun buildEventsByStartHour(
    events: List<CalendarEventUI>,
    selectedDate: LocalDate,
    zone: ZoneId = ZoneId.systemDefault()
): Map<Int, List<CalendarEventUI>> {

    return events
        .mapNotNull { event ->
            val startDateTime = Instant
                .ofEpochMilli(event.startTime.toEpochMillis(selectedDate))
                .atZone(zone)
                .toLocalDateTime()

            if (startDateTime.toLocalDate() != selectedDate) return@mapNotNull null

            startDateTime.hour to event
        }
        .groupBy(
            keySelector = { it.first },
            valueTransform = { it.second }
        )
}

@Composable
fun DailyHourRow(
    hour: Int,
    rowHeight: Dp,
    events: List<CalendarEventUI>,
    onEventClick: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(rowHeight)
            .padding(horizontal = 8.dp)
    ) {
        Text(
            text = "%02d:00".format(hour),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier
                .width(48.dp)
                .padding(top = 4.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 8.dp)
        ) {
            Divider()

            events.forEach { event ->
                    DailyEventCard(
                        event = event,
                        onClick = onEventClick
                    )
                }
        }
    }
}

@Composable
fun DailyEventCard(
    event: CalendarEventUI,
    onClick: (Int) -> Unit
) {
    val start = event.startTime.to24hString()
    val end = event.endTime.to24hString()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable { onClick(event.eventId) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(event.color, CircleShape)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = event.title,
                style = MaterialTheme.typography.labelMedium,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onTertiary,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Icon(
                imageVector = event.icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onTertiary
            )
        }

        Text(
            text = "$start - $end",
            style = MaterialTheme.typography.labelMedium,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onTertiary,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )

        Text(
            text = event.description,
            style = MaterialTheme.typography.labelMedium,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onTertiary,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
    }
}
