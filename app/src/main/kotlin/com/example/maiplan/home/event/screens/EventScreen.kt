package com.example.maiplan.home.event.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.maiplan.R
import com.example.maiplan.home.event.utils.CalendarEventUI
import com.example.maiplan.home.event.utils.LocalDateSaver
import com.example.maiplan.home.navigation.HomeNavigationBar
import com.example.maiplan.utils.LocalAdaptiveLayout
import com.example.maiplan.utils.notifications.AlarmScheduler
import com.example.maiplan.viewmodel.event.EventViewModel
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun EventScreen(
    eventViewModel: EventViewModel,
    rootNavController: NavHostController,
    localNavController: NavHostController,
    onCreateEventClick: () -> Unit,
    onUpdateEventClick: (Int) -> Unit,
    onDeleteClick: (Int?, Int, LocalDate) -> Unit,
) {
    var selectedDate by rememberSaveable(stateSaver = LocalDateSaver) {
        mutableStateOf(LocalDate.now())
    }
    LaunchedEffect(selectedDate.year, selectedDate.month) {
        eventViewModel.loadMonth(selectedDate)
    }

    val context = LocalContext.current
    val eventsByDate by eventViewModel.monthlyEvents.collectAsState()
    val adaptiveLayout = LocalAdaptiveLayout.current
    var showDatePicker by remember { mutableStateOf(false) }

    EventScreenBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                EventTopBar(
                    monthTitle = selectedDate.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                    onDatePickerClick = { showDatePicker = true },
                )
            },
            bottomBar = { HomeNavigationBar(rootNavController, context) },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = onCreateEventClick,
                    modifier = Modifier.padding(end = 14.dp, bottom = 12.dp),
                    containerColor = EventPrimary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(17.dp),
                    icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                    text = {
                        Text(
                            text = stringResource(R.string.event_new_action),
                            fontWeight = FontWeight.SemiBold,
                        )
                    },
                )
            },
        ) { innerPadding ->
            if (adaptiveLayout.useTwoPaneLayout) {
                Row(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    MonthCalendarSection(
                        selectedDate = selectedDate,
                        eventsByDate = eventsByDate,
                        onDateSelected = { selectedDate = it },
                        onPreviousMonth = { selectedDate = selectedDate.withDayOfMonth(1).minusMonths(1) },
                        onNextMonth = { selectedDate = selectedDate.withDayOfMonth(1).plusMonths(1) },
                        modifier = Modifier
                            .weight(1.05f)
                            .fillMaxHeight(),
                    )
                    DayEventsSection(
                        events = eventsByDate[selectedDate].orEmpty(),
                        onUpdateEventClick = onUpdateEventClick,
                        onDeleteClick = onDeleteClick,
                        selectedDate = selectedDate,
                        modifier = Modifier
                            .weight(0.95f)
                            .fillMaxHeight(),
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    MonthCalendarSection(
                        selectedDate = selectedDate,
                        eventsByDate = eventsByDate,
                        onDateSelected = { selectedDate = it },
                        onPreviousMonth = { selectedDate = selectedDate.withDayOfMonth(1).minusMonths(1) },
                        onNextMonth = { selectedDate = selectedDate.withDayOfMonth(1).plusMonths(1) },
                        modifier = Modifier.weight(0.94f),
                    )
                    DayEventsSection(
                        events = eventsByDate[selectedDate].orEmpty(),
                        onUpdateEventClick = onUpdateEventClick,
                        onDeleteClick = onDeleteClick,
                        selectedDate = selectedDate,
                        modifier = Modifier.weight(1.06f),
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        EventDatePickerDialog(
            onDateSelected = {
                selectedDate = it
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
        )
    }
}

@Composable
fun MonthCalendarSection(
    selectedDate: LocalDate,
    eventsByDate: Map<LocalDate, List<CalendarEventUI>>,
    onDateSelected: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dark = isSystemInDarkTheme()
    val surface = if (dark) Color(0xFF181C2B) else Color.White
    val ink = if (dark) Color(0xFFF3F5FA) else EventInk
    val muted = if (dark) Color(0xFFAEB7C8) else EventMuted
    val firstDayOfMonth = selectedDate.withDayOfMonth(1)
    val days = List(firstDayOfMonth.dayOfWeek.value - 1) { null } +
        (1..selectedDate.lengthOfMonth()).map { firstDayOfMonth.withDayOfMonth(it) }
    val paddedDays = days + List((7 - days.size % 7) % 7) { null }
    val weeks = paddedDays.chunked(7)
    val weekdays = listOf(R.string.mon, R.string.tue, R.string.wed, R.string.thu, R.string.fri, R.string.sat, R.string.sun)

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = surface,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, if (dark) Color.White.copy(alpha = 0.08f) else EventBorder),
        shadowElevation = if (dark) 0.dp else 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.event_calendar_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ink,
                    )
                    Text(
                        text = stringResource(R.string.event_calendar_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = muted,
                    )
                }
                IconButton(onClick = onPreviousMonth) {
                    Icon(Icons.Rounded.ChevronLeft, contentDescription = null, tint = EventPrimary)
                }
                IconButton(onClick = onNextMonth) {
                    Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = EventPrimary)
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                weekdays.forEach { day ->
                    Text(
                        text = stringResource(day),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = muted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val cellHeight = maxHeight / weeks.size
                Column(Modifier.fillMaxSize()) {
                    weeks.forEach { week ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(cellHeight),
                        ) {
                            week.forEach { date ->
                                CalendarDayCell(
                                    date = date,
                                    selectedDate = selectedDate,
                                    events = date?.let { eventsByDate[it] }.orEmpty(),
                                    onDateSelected = onDateSelected,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    date: LocalDate?,
    selectedDate: LocalDate,
    events: List<CalendarEventUI>,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier,
) {
    if (date == null) {
        Spacer(modifier)
        return
    }
    val dark = isSystemInDarkTheme()
    val selected = date == selectedDate
    val today = date == LocalDate.now()
    val ink = if (dark) Color(0xFFF3F5FA) else EventInk

    Box(
        modifier = modifier
            .fillMaxHeight()
            .padding(2.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onDateSelected(date) }
            .background(
                when {
                    selected -> EventPrimary
                    today -> EventPrimary.copy(alpha = if (dark) 0.24f else 0.10f)
                    else -> Color.Transparent
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected || today) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) Color.White else ink,
            )
            if (events.isNotEmpty()) {
                Row(
                    modifier = Modifier.padding(top = 3.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    events.take(3).forEach { event ->
                        Box(
                            Modifier
                                .size(4.dp)
                                .background(
                                    if (selected) Color.White.copy(alpha = 0.9f) else event.color,
                                    CircleShape,
                                ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DayEventsSection(
    events: List<CalendarEventUI>,
    onUpdateEventClick: (Int) -> Unit,
    onDeleteClick: (Int?, Int, LocalDate) -> Unit,
    selectedDate: LocalDate,
    modifier: Modifier = Modifier,
) {
    val dark = isSystemInDarkTheme()
    val surface = if (dark) Color(0xFF181C2B) else Color.White
    val ink = if (dark) Color(0xFFF3F5FA) else EventInk
    val muted = if (dark) Color(0xFFAEB7C8) else EventMuted

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = surface,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, if (dark) Color.White.copy(alpha = 0.08f) else EventBorder),
        shadowElevation = if (dark) 0.dp else 2.dp,
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.event_agenda_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ink,
                    )
                    Text(
                        text = selectedDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)),
                        style = MaterialTheme.typography.bodySmall,
                        color = muted,
                    )
                }
                Surface(
                    color = EventPrimary.copy(alpha = if (dark) 0.24f else 0.10f),
                    shape = RoundedCornerShape(20.dp),
                ) {
                    Text(
                        text = stringResource(R.string.event_agenda_count, events.size),
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (dark) Color(0xFFD7E5F5) else EventPrimary,
                    )
                }
            }

            if (events.isEmpty()) {
                EventEmptyState(Modifier.weight(1f))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 92.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(events, key = { it.eventId }) { event ->
                        SwipeableEventCard(
                            event = event,
                            selectedDate = selectedDate,
                            onEdit = onUpdateEventClick,
                            onDelete = onDeleteClick,
                        )
                    }
                    item {
                        Text(
                            text = stringResource(R.string.event_swipe_hint),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = muted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EventEmptyState(modifier: Modifier = Modifier) {
    val dark = isSystemInDarkTheme()
    val ink = if (dark) Color(0xFFF3F5FA) else EventInk
    val muted = if (dark) Color(0xFFAEB7C8) else EventMuted
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            modifier = Modifier.size(58.dp),
            color = EventPrimary.copy(alpha = if (dark) 0.22f else 0.09f),
            shape = CircleShape,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Rounded.EventAvailable,
                    contentDescription = null,
                    tint = EventPrimaryLight,
                    modifier = Modifier.size(28.dp),
                )
            }
        }
        Spacer(Modifier.height(13.dp))
        Text(
            text = stringResource(R.string.event_empty_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = ink,
        )
        Spacer(Modifier.height(5.dp))
        Text(
            text = stringResource(R.string.event_empty_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = muted,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

@Composable
private fun SwipeableEventCard(
    event: CalendarEventUI,
    selectedDate: LocalDate,
    onEdit: (Int) -> Unit,
    onDelete: (Int?, Int, LocalDate) -> Unit,
) {
    val context = LocalContext.current
    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = { it * 0.45f },
    )

    LaunchedEffect(dismissState, event.eventId) {
        snapshotFlow { dismissState.currentValue }.collectLatest { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    dismissState.snapTo(SwipeToDismissBoxValue.Settled)
                    onEdit(event.eventId)
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    dismissState.snapTo(SwipeToDismissBoxValue.Settled)
                    val reminderId = event.reminderId.takeIf { it != 0 }
                    onDelete(reminderId, event.eventId, selectedDate)
                    reminderId?.let { AlarmScheduler.cancelAlarm(context, it) }
                }
                SwipeToDismissBoxValue.Settled -> Unit
            }
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val editing = dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd
            val deleting = dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart
            val backgroundColor = when {
                deleting -> Color(0xFFE5484D)
                editing -> EventTeal
                else -> Color.Transparent
            }
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(17.dp))
                    .background(backgroundColor)
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (editing) Arrangement.Start else Arrangement.End,
            ) {
                if (deleting || editing) {
                    Icon(
                        imageVector = if (deleting) Icons.Rounded.DeleteOutline else Icons.Rounded.Edit,
                        contentDescription = null,
                        tint = Color.White,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(if (deleting) R.string.delete else R.string.edit),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        },
    ) {
        EventCard(event = event, onClick = { onEdit(event.eventId) })
    }
}

@Composable
fun EventCard(event: CalendarEventUI, onClick: () -> Unit = {}) {
    val dark = isSystemInDarkTheme()
    val cardColor = if (dark) Color(0xFF212638) else Color(0xFFF8FAFD)
    val ink = if (dark) Color(0xFFF3F5FA) else EventInk
    val muted = if (dark) Color(0xFFAEB7C8) else EventMuted

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = cardColor,
        shape = RoundedCornerShape(17.dp),
        border = BorderStroke(1.dp, if (dark) Color.White.copy(alpha = 0.07f) else EventBorder),
    ) {
        Row(
            modifier = Modifier.padding(13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(46.dp),
                color = event.color.copy(alpha = if (dark) 0.28f else 0.13f),
                shape = RoundedCornerShape(14.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(event.icon, contentDescription = null, tint = event.color, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (event.description.isNotBlank()) {
                    Text(
                        text = event.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "${event.startTime} – ${event.endTime}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = EventPrimaryLight,
                )
            }
            if (event.reminderTime != 0L) {
                Icon(
                    Icons.Rounded.NotificationsNone,
                    contentDescription = null,
                    tint = muted,
                    modifier = Modifier.size(19.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventTopBar(monthTitle: String, onDatePickerClick: () -> Unit) {
    val dark = isSystemInDarkTheme()
    CenterAlignedTopAppBar(
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = monthTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (dark) Color(0xFFF3F5FA) else EventInk,
                )
                Text(
                    text = stringResource(R.string.event_calendar_title),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (dark) Color(0xFFAEB7C8) else EventMuted,
                )
            }
        },
        actions = {
            IconButton(onClick = onDatePickerClick) {
                Icon(
                    Icons.Rounded.CalendarMonth,
                    contentDescription = stringResource(R.string.event_open_calendar),
                    tint = EventPrimaryLight,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
    )
}
