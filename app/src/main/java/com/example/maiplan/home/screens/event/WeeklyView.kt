package com.example.maiplan.home.screens.event

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate

@Composable
fun WeeklyView(selectedDate: LocalDate) {
    val startOfWeek = selectedDate.with(DayOfWeek.MONDAY)

    LazyRow(
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(7) { i ->
            val day = startOfWeek.plusDays(i.toLong())
            WeeklyViewCard(day)
        }
    }
}

@Composable
fun WeeklyViewCard(day: LocalDate) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .border(
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary),
                shape = MaterialTheme.shapes.medium
            ),
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
                text = "${day.dayOfWeek}, ${day.month} ${day.dayOfMonth}",
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}
