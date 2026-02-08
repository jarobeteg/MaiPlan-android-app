package com.example.maiplan.home.event.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.getString
import androidx.navigation.NavHostController
import com.example.maiplan.R
import com.example.maiplan.components.DatePickerDialogComponent
import com.example.maiplan.components.isTablet
import com.example.maiplan.home.event.utils.LocalDateSaver
import com.example.maiplan.home.navigation.HomeNavigationBar
import com.example.maiplan.viewmodel.event.EventViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale

@Composable
fun EventScreen(
    eventViewModel: EventViewModel,
    rootNavController: NavHostController,
    localNavController: NavHostController, // use this to navigate from view to update or view details screen
    onCreateEventClick: () -> Unit,
    onUpdateEventClick: (Int) -> Unit
) {
    var selectedDate by rememberSaveable(stateSaver = LocalDateSaver) {
        mutableStateOf(LocalDate.now())
    }
    LaunchedEffect(selectedDate.year, selectedDate.month) {
        eventViewModel.loadMonth(selectedDate)
    }

    val context = LocalContext.current
    val eventsByDate by eventViewModel.monthlyEvents.collectAsState()
    var selectedView by rememberSaveable { mutableIntStateOf(0) }
    var showDatePicker by remember { mutableStateOf(false) }

    val formattedTitle = getFormattedTitle(context, selectedView, selectedDate)
    val views = listOf(
        getString(context, R.string.monthly),
        getString(context, R.string.weekly),
        getString(context, R.string.daily)
    )

    Scaffold(
        topBar = {
            EventTopBar(
                formattedTitle,
                views,
                onViewSelected = { index -> selectedView = index },
                onDatePickerClick = { showDatePicker = true },
                onCreateEventClick = onCreateEventClick
            )},
        bottomBar = { HomeNavigationBar(rootNavController, context) }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            when (selectedView) {
                0 -> MonthlyView(selectedDate, eventsByDate,
                    onDayClick = { clickedDate ->
                        selectedDate = clickedDate
                        selectedView = 2
                    })
                1 -> WeeklyView(selectedDate, eventsByDate,
                    onDayClick = { clickedDate ->
                        selectedDate = clickedDate
                        selectedView = 2
                    })
                2 -> DailyView(selectedDate, eventsByDate, onUpdateEventClick)
                else -> MonthlyView(selectedDate, eventsByDate,
                    onDayClick = { clickedDate ->
                        selectedDate = clickedDate
                        selectedView = 2
                    })
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
fun getFormattedTitle(context: Context, selectedView: Int, selectedDate: LocalDate): String {
    return when (selectedView) {
        0 -> selectedDate.format(DateTimeFormatter.ofPattern("MMM yyyy"))
        1 -> "${getString(context, R.string.week)} ${selectedDate.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear())}"
        2 -> selectedDate.format(DateTimeFormatter.ofPattern("EE, MMM d"))
        else -> ""
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventTopBar(
    formattedTitle: String,
    views: List<String>,
    onViewSelected: (Int) -> Unit,
    onDatePickerClick: () -> Unit,
    onCreateEventClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var buttonWidth by remember { mutableIntStateOf(0) }

    val isTablet = isTablet()
    val iconSize = if (isTablet) 32.dp else 24.dp
    val fontSize = if (isTablet) 24.sp else 16.sp
    val barHeight = if (isTablet) 112.dp else 112.dp

    TopAppBar(
        modifier = Modifier.height(barHeight),
        title = {
            Box(
                modifier = Modifier.fillMaxHeight(),
                contentAlignment = Alignment.CenterStart
            ) {
                Box(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.primary)
                        .border(2.dp, MaterialTheme.colorScheme.secondary, MaterialTheme.shapes.medium)
                ) {
                    OutlinedButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier
                            .height(if (isTablet) 50.dp else 40.dp)
                            .onGloballyPositioned { coordinates ->
                                buttonWidth = coordinates.size.width
                            },
                        border = null,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = formattedTitle,
                                style = if (isTablet) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = fontSize,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown Arrow",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier
                                    .size(if (isTablet) 32.dp else 24.dp)
                                    .rotate(if (expanded) 0f else 90f)
                            )
                        }
                    }

                    EventDropdownMenu(
                        expanded = expanded,
                        views = views,
                        buttonWidth = buttonWidth,
                        onItemSelected = { index ->
                            onViewSelected(index)
                            expanded = false
                        },
                        onDismiss = { expanded = false }
                    )
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        actions = {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(end = if (isTablet) 16.dp else 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(if (isTablet) 16.dp else 8.dp)
            ) {
                IconButton(
                    onClick = onDatePickerClick,
                    modifier = Modifier.size(iconSize)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                IconButton(
                    onClick = onCreateEventClick,
                    modifier = Modifier.size(iconSize)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    )
}

@Composable
fun EventDropdownMenu(
    expanded: Boolean,
    views: List<String>,
    buttonWidth: Int,
    onItemSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val isTablet = isTablet()
    val fontSize = if (isTablet) 24.sp else 16.sp
    val itemHeight = if (isTablet) 64.dp else 48.dp

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = {
            onDismiss()
        },
        modifier = Modifier
            .width(with(LocalDensity.current) { buttonWidth.toDp() })
            .background(MaterialTheme.colorScheme.primary)
            .clip(MaterialTheme.shapes.medium)
    ) {
        views.forEachIndexed { index, view ->
            DropdownMenuItem(
                text = { Text(
                    text = view,
                    fontSize = fontSize,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(vertical = if (isTablet) 8.dp else 4.dp)
                ) },
                onClick = {
                    onItemSelected(index)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight)
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}