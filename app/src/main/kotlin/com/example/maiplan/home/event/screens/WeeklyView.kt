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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.maiplan.components.isTablet
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
    val isTablet = isTablet()

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val cardHeight = maxHeight
        val cardWidth = if (isTablet) maxWidth * 0.45f else maxWidth * 0.75f

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeight)
                .padding(horizontal = if (isTablet) 16.dp else 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(if (isTablet) 20.dp else 12.dp)
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
    val isTablet = isTablet()

    val headerFontSize = if (isTablet) 54.sp else 16.sp
    val contentFontSize = if (isTablet) 32.sp else 16.sp
    val eventTitleSize = if (isTablet) 48.sp else 12.sp
    val moreTextSize = if (isTablet) 18.sp else 10.sp
    val dotSize = if (isTablet) 32.dp else 6.dp
    val iconSize = if (isTablet) 56.dp else 12.dp
    val padding = if (isTablet) 16.dp else 8.dp

    Card(
        modifier = Modifier
            .width(width)
            .height(height)
            .clickable { onClick() }
            .border(
                border = BorderStroke(if (isTablet) 3.dp else 2.dp, MaterialTheme.colorScheme.secondary),
                shape = MaterialTheme.shapes.medium
            ),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(if (isTablet) 8.dp else 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Text(
                text = "${day.dayOfWeek.name.take(3)}, ${day.dayOfMonth}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onTertiary,
                fontSize = headerFontSize,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(if (isTablet) 12.dp else 4.dp))

            events.take(5).forEach { event ->
                val start = event.startTime.to24hString()
                val end = event.endTime.to24hString()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = if (isTablet) 8.dp else 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(dotSize)
                            .background(event.color, CircleShape)
                    )

                    Spacer(modifier = Modifier.width(if (isTablet) 10.dp else 4.dp))

                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.labelMedium,
                        fontSize = eventTitleSize,
                        color = MaterialTheme.colorScheme.onTertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                        fontWeight = FontWeight.SemiBold
                    )

                    event.icon.let {
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = it,
                            contentDescription = null,
                            modifier = Modifier.size(iconSize),
                            tint = MaterialTheme.colorScheme.onTertiary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

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

            if (events.size > 5) {
                Text(
                    text = "+${events.size - 5}",
                    fontSize = moreTextSize,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}