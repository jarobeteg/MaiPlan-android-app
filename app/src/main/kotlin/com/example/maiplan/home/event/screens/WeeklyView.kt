package com.example.maiplan.home.event.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.maiplan.home.event.utils.CalendarEventUI
import com.example.maiplan.home.event.utils.to24hString
import java.time.DayOfWeek
import java.time.LocalDate

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun WeeklyView(
    selectedDate: LocalDate,
    eventsByDate: Map<LocalDate, List<CalendarEventUI>>,
    onDayClick: (LocalDate) -> Unit
) {
    val startOfWeek = selectedDate.with(DayOfWeek.MONDAY)

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val cardHeight = maxHeight
        val cardWidth = maxWidth * 0.75f

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeight)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(7) { i ->
                val day = startOfWeek.plusDays(i.toLong())
                val events = eventsByDate[day].orEmpty()
                WeeklyViewCard(day, events, cardWidth, cardHeight,
                    onClick = { onDayClick(day) })
            }
        }
    }
}

@Composable
fun WeeklyViewCard(
    day: LocalDate,
    events: List<CalendarEventUI>,
    width: Dp,
    height: Dp,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(width)
            .height(height)
            .clickable { onClick() }
            .border(
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary),
                shape = MaterialTheme.shapes.medium
            ),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Text(
                text = "${day.dayOfWeek.name.take(3)}, ${day.dayOfMonth}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onTertiary,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            events.take(5).forEach { event ->
                val start = event.startTime.to24hString()
                val end = event.endTime.to24hString()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(event.color, CircleShape)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.labelMedium,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onTertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    event.icon.let {
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = it,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onTertiary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

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

            if (events.size > 5) {
                Text(
                    text = "+${events.size - 5}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}