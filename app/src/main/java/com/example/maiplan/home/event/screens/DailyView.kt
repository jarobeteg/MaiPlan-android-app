package com.example.maiplan.home.event.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate

/**
 * Displays a vertical list of 24 hour time card slots for the selected day.
 *
 * Each hour is represented by a [DailyViewHourCard], allowing users to view events for that hour.
 *
 * @param selectedDate The [LocalDate] representing the currently selected day.
 *
 * @see DailyViewHourCard
 */
@Composable
fun DailyView(selectedDate: LocalDate) {
    LazyColumn(
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(24) { hour ->
            DailyViewHourCard(selectedDate, hour)
        }
    }
}

/**
 * Displays a card representing a single hour card within a day.
 *
 * The card shows the time range (for example, "Monday, January 1 - 8.00 to 9:00")
 * based on the given [selectedDate] and [hour].
 *
 * @param selectedDate The [LocalDate] for which this hour card belongs.
 * @param hour The starting hour (0-23) for this card.
 */
@Composable
fun DailyViewHourCard(selectedDate: LocalDate, hour: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "${selectedDate.dayOfWeek}, ${selectedDate.month} ${selectedDate.dayOfMonth} - $hour:00 to ${hour + 1}:00",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
