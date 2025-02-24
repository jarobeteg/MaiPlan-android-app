package com.example.maiplan.screens.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.getString
import com.example.maiplan.R
import com.example.maiplan.components.DatePickerDialogComponent
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventScreen() {
    val context = LocalContext.current
    var selectedView by remember { mutableIntStateOf(0) }
    var expanded by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val rotationAngle by animateFloatAsState(
        targetValue = if (expanded) 90f else 0f,
        label = "Dropdown Arrow Rotation"
    )

    val formattedTitle = when (selectedView) {
        0 -> selectedDate.format(DateTimeFormatter.ofPattern("MMM yyyy"))
        1 -> "${getString(context, R.string.week)} ${selectedDate.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear())}"
        2 -> selectedDate.format(DateTimeFormatter.ofPattern("EE, MMM d"))
        else -> ""
    }

    val views = listOf(getString(context, R.string.monthly), getString(context, R.string.weekly), getString(context, R.string.daily))

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    var buttonWidth by remember { mutableIntStateOf(0) }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF4A6583))
                            .border(2.dp, Color(0xFF2D3E50), RoundedCornerShape(12.dp))
                    ) {
                        OutlinedButton(
                            onClick = { expanded = !expanded },
                            modifier = Modifier
                                .height(40.dp)
                                .onGloballyPositioned { coordinates ->
                                    buttonWidth = coordinates.size.width
                                },
                            border = null,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text(
                                text = formattedTitle,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown Arrow",
                                tint = Color.White,
                                modifier = Modifier.rotate(rotationAngle)
                            )
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier
                                .width(with(LocalDensity.current) { buttonWidth.toDp() })
                                .background(Color(0xFF4A6583))
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            views.forEachIndexed { index, view ->
                                DropdownMenuItem(
                                    text = { Text(view, color = Color.White) },
                                    onClick = {
                                        selectedView = index
                                        expanded = false
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF4A6583))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF4A6583),
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Select Date", tint = Color.White)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            when (selectedView) {
                0 -> MonthlyView(selectedDate)
                1 -> WeeklyView(selectedDate)
                2 -> DailyView(selectedDate)
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialogComponent(
            onDateSelected = { date ->
                selectedDate = date
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@Composable
fun MonthlyView(selectedDate: LocalDate) {
    val daysInMonth = selectedDate.lengthOfMonth()
    val firstDayOfMonth = selectedDate.withDayOfMonth(1).dayOfWeek.value % 7

    val weekFields = WeekFields.of(Locale.getDefault())
    val startWeek = selectedDate.withDayOfMonth(1).get(weekFields.weekOfWeekBasedYear())
    val endWeek = selectedDate.withDayOfMonth(daysInMonth).get(weekFields.weekOfWeekBasedYear())

    val weekNumbers = (startWeek..endWeek).toList()
    val weekdays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val weekNumberWidth = 16.dp
        val cellSize = (maxWidth - weekNumberWidth) / 7
        val headerHeight = 24.dp
        val rowHeight = (maxHeight - headerHeight) / weekNumbers.size

        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.height(headerHeight)) {
                Spacer(modifier = Modifier.width(weekNumberWidth))
                weekdays.forEach { day ->
                    Box(
                        modifier = Modifier
                            .width(cellSize)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                            color = Color.Gray
                        )
                    }
                }
            }

            weekNumbers.forEachIndexed { weekIndex, weekNumber ->
                Row(modifier = Modifier.height(rowHeight)) {
                    Box(
                        modifier = Modifier
                            .width(weekNumberWidth)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Text(
                            text = weekNumber.toString(),
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 10.sp),
                            color = Color.Gray
                        )
                    }

                    for (dayIndex in 0 until 7) {
                        val dayNumber = weekIndex * 7 + dayIndex - firstDayOfMonth + 1
                        if (dayNumber in 1..daysInMonth) {
                            Card(
                                modifier = Modifier
                                    .width(cellSize)
                                    .height(rowHeight)
                                    .padding(0.5.dp),
                                shape = RoundedCornerShape(2.dp),
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.TopCenter
                                ) {
                                    Text(
                                        text = "$dayNumber",
                                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp)
                                    )
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.width(cellSize))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyView(selectedDate: LocalDate) {
    val startOfWeek = selectedDate.with(DayOfWeek.MONDAY)
    LazyRow(
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(7) { i ->
            val day = startOfWeek.plusDays(i.toLong())
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .border(border = BorderStroke(2.dp, Color(0xFF2D3E50)), shape = RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(text = "${day.dayOfWeek}, ${day.month} ${day.dayOfMonth}", style = MaterialTheme.typography.headlineSmall)
                }
            }
        }
    }
}

@Composable
fun DailyView(selectedDate: LocalDate) {
    LazyColumn(
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(24) { hour ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(text = "${selectedDate.dayOfWeek}, ${selectedDate.month} ${selectedDate.dayOfMonth} - ${hour}:00 to ${hour + 1}:00", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewActivityScreen() {
    EventScreen()
}