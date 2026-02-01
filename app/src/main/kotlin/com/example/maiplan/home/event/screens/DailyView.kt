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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import com.example.maiplan.components.isTablet
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
    val isTablet = isTablet()

    val defaultHourHeight = if (isTablet) 120.dp else 72.dp
    val perEventHeight = if (isTablet) 160.dp else 100.dp

    val eventsByHour = remember(eventsForDay, selectedDate) {
        buildEventsByStartHour(eventsForDay, selectedDate)
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(24) { hour ->
            val hourEvents = eventsByHour[hour].orEmpty()
            val rowHeight = max(defaultHourHeight, perEventHeight * hourEvents.size + (if (isTablet) perEventHeight else 16.dp))

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
    val isTablet = isTablet()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(rowHeight)
            .padding(horizontal = if (isTablet) 16.dp else 8.dp)
    ) {
        Text(
            text = "%02d:00".format(hour),
            fontSize = if (isTablet) 32.sp else 16.sp,
            style = if (isTablet) MaterialTheme.typography.titleMedium else MaterialTheme.typography.labelMedium,
            modifier = Modifier
                .width(if (isTablet) 92.dp else 48.dp)
                .padding(top = if (isTablet) 16.dp else 4.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = if (isTablet) 16.dp else 8.dp)
        ) {
            HorizontalDivider(thickness = if (isTablet) 3.dp else 1.dp)

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
    val isTablet = isTablet()
    val start = event.startTime.to24hString()
    val end = event.endTime.to24hString()

    val contentFontSize = if (isTablet) 32.sp else 16.sp
    val eventTitleSize = if (isTablet) 48.sp else 12.sp
    val dotSize = if (isTablet) 32.dp else 6.dp
    val iconSize = if (isTablet) 56.dp else 12.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = if (isTablet) 4.dp else 2.dp)
            .clickable { onClick(event.eventId) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        shape = RoundedCornerShape(if (isTablet) 12.dp else 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(if (isTablet) 16.dp else 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .background(event.color, CircleShape)
            )

            Spacer(modifier = Modifier.width(if (isTablet) 12.dp else 8.dp))

            Text(
                text = event.title,
                style = if (isTablet) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.labelMedium,
                fontSize = eventTitleSize,
                color = MaterialTheme.colorScheme.onTertiary,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Bold
            )

            Icon(
                imageVector = event.icon,
                contentDescription = null,
                modifier = Modifier.size(iconSize),
                tint = MaterialTheme.colorScheme.onTertiary
            )
        }

        Text(
            text = "$start - $end",
            style = MaterialTheme.typography.labelMedium,
            fontSize = contentFontSize,
            color = MaterialTheme.colorScheme.onTertiary,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )

        Text(
            text = event.description,
            style = MaterialTheme.typography.labelMedium,
            fontSize = contentFontSize,
            color = MaterialTheme.colorScheme.onTertiary,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
    }
}
