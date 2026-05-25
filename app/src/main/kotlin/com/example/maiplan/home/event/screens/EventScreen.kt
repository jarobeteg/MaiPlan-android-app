package com.example.maiplan.home.event.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.maiplan.R
import com.example.maiplan.components.AdjustableSpacer
import com.example.maiplan.components.DatePickerDialogComponent
import com.example.maiplan.components.getMonthText
import com.example.maiplan.home.event.utils.CalendarEventUI
import com.example.maiplan.home.event.utils.LocalDateSaver
import com.example.maiplan.home.navigation.HomeNavigationBar
import com.example.maiplan.utils.LocalUiScale
import com.example.maiplan.utils.notifications.AlarmScheduler
import com.example.maiplan.viewmodel.event.EventViewModel
import java.time.LocalDate

@Composable
fun EventScreen(
    eventViewModel: EventViewModel,
    rootNavController: NavHostController,
    localNavController: NavHostController, // use this to navigate from view to update or view details screen
    onCreateEventClick: () -> Unit,
    onUpdateEventClick: (Int) -> Unit,
    onDeleteClick: (Int?, Int, LocalDate) -> Unit
) {
    val ui = LocalUiScale.current

    var selectedDate by rememberSaveable(stateSaver = LocalDateSaver) {
        mutableStateOf(LocalDate.now())
    }
    LaunchedEffect(selectedDate.year, selectedDate.month) {
        eventViewModel.loadMonth(selectedDate)
    }

    val context = LocalContext.current
    val eventsByDate by eventViewModel.monthlyEvents.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    val closeDatePicker = { showDatePicker = false }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        topBar = {
            EventTopBar(
                title = getMonthText(selectedDate.month.value),
                onDatePickerClick = { showDatePicker = true },
                onCreateEventClick = onCreateEventClick
            )},
        bottomBar = { HomeNavigationBar(rootNavController, context) }
    ) { innerPadding ->
        if (isLandscape) {
            Row(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                MonthCalendarSection(
                    selectedDate = selectedDate,
                    eventsByDate = eventsByDate,
                    onDateSelected = { selectedDate = it },
                    modifier = Modifier
                        .weight(ui.dimensions.calendarSectionWeight)
                        .wrapContentSize()
                )

                VerticalDivider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(ui.dimensions.generalDividerThickness)
                )


                DayEventsSection(
                    events = eventsByDate[selectedDate] ?: emptyList(),
                    onUpdateEventClick = onUpdateEventClick,
                    onDeleteClick = onDeleteClick,
                    selectedDate = selectedDate,
                    modifier = Modifier
                        .weight(ui.dimensions.eventSectionWeight)
                        .fillMaxHeight()
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {

                MonthCalendarSection(
                    selectedDate = selectedDate,
                    eventsByDate = eventsByDate,
                    onDateSelected = { selectedDate = it },
                    modifier = Modifier
                        .weight(ui.dimensions.calendarSectionWeight)
                        .wrapContentSize()
                )

                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ui.dimensions.generalDividerThickness)
                )

                DayEventsSection(
                    events = eventsByDate[selectedDate] ?: emptyList(),
                    onUpdateEventClick = onUpdateEventClick,
                    onDeleteClick = onDeleteClick,
                    selectedDate = selectedDate,
                    modifier = Modifier
                        .weight(ui.dimensions.eventSectionWeight)
                )
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialogComponent(
            onDateSelected = { date ->
                selectedDate = date
                closeDatePicker()
            },
            onDismiss = {
                closeDatePicker()
            }
        )
    }
}

@Composable
fun MonthCalendarSection(
    selectedDate: LocalDate,
    eventsByDate: Map<LocalDate, List<CalendarEventUI>>,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val ui = LocalUiScale.current

    val firstDayOfMonth = selectedDate.withDayOfMonth(1)
    val daysInMonth = selectedDate.lengthOfMonth()
    val startOffset = firstDayOfMonth.dayOfWeek.value - 1

    val monthDays =
        List(startOffset) { null } +
                (1..daysInMonth).map { firstDayOfMonth.withDayOfMonth(it) }

    val remainder = monthDays.size % 7
    val paddedDays =
        if (remainder == 0) monthDays
        else monthDays + List(7 - remainder) { null }

    val weeks = paddedDays.chunked(7)

    val weekdays = listOf(
        stringResource(R.string.mon),
        stringResource(R.string.tue),
        stringResource(R.string.wed),
        stringResource(R.string.thu),
        stringResource(R.string.fri),
        stringResource(R.string.sat),
        stringResource(R.string.sun)
    )

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val availableHeightPerRow = maxHeight / weeks.size
        val circlePlusSpacerHeight = ui.dimensions.generalTouchTarget + ui.dimensions.smallSpacer
        val spaceForDots = availableHeightPerRow - circlePlusSpacerHeight
        val maxDotRows = when {
            spaceForDots >= ui.dimensions.doubleDotArea -> 2
            spaceForDots >= ui.dimensions.singleDotArea -> 1
            else -> 0
        }

        Column(modifier = Modifier.fillMaxWidth()) {

            Row(modifier = Modifier.fillMaxWidth()) {
                weekdays.forEach { day ->
                    Box(
                        modifier = Modifier
                            .weight(ui.dimensions.generalWeight)
                            .padding(vertical = ui.dimensions.verticalWeekdayPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day,
                            fontSize = ui.fonts.generalTextSize,
                            style = ui.typographies.generalTextStyle,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            weeks.forEach { week ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(ui.dimensions.generalWeight)
                ) {
                    week.forEach { date ->
                        Box(
                            modifier = Modifier
                                .weight(ui.dimensions.generalWeight)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (date != null) {
                                val isSelected = date == selectedDate
                                val isToday = date == LocalDate.now()
                                val events = eventsByDate[date] ?: emptyList()

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(ui.dimensions.generalTouchTarget)
                                            .clip(CircleShape)
                                            .clickable { onDateSelected(date) }
                                            .background(
                                                when {
                                                    isSelected -> MaterialTheme.colorScheme.primary
                                                    isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                                    else -> Color.Transparent
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = date.dayOfMonth.toString(),
                                            fontSize = ui.fonts.generalTextSize,
                                            color = when {
                                                isSelected -> MaterialTheme.colorScheme.onPrimary
                                                else -> MaterialTheme.colorScheme.onBackground
                                            }
                                        )
                                    }

                                    val dotAreaHeight = when (maxDotRows) {
                                        2 -> ui.dimensions.smallSpacer + ui.dimensions.doubleDotArea
                                        1 -> ui.dimensions.smallSpacer + ui.dimensions.singleDotArea
                                        else -> 0.dp
                                    }

                                    if (maxDotRows > 0) {
                                        Box(
                                            modifier = Modifier
                                                .height(dotAreaHeight)
                                                .fillMaxWidth(),
                                            contentAlignment = Alignment.TopCenter
                                        ) {
                                            if (events.isNotEmpty()) {
                                                val dotRows = events
                                                    .take(maxDotRows * 4)
                                                    .chunked(4)
                                                Column(
                                                    modifier = Modifier.padding(top = ui.dimensions.smallSpacer),
                                                    verticalArrangement = Arrangement.spacedBy(ui.dimensions.spacedByExtraSmall),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    dotRows.forEach { rowEvents ->
                                                        Row(
                                                            horizontalArrangement = Arrangement.spacedBy(ui.dimensions.spacedByExtraSmall),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            rowEvents.forEach { event ->
                                                                Box(
                                                                    modifier = Modifier
                                                                        .size(ui.dimensions.eventDotSize)
                                                                        .background(
                                                                            color = event.color,
                                                                            shape = CircleShape
                                                                        )
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val ui = LocalUiScale.current

    LazyColumn(
        modifier = modifier.fillMaxSize().clipToBounds(),
        contentPadding = PaddingValues(ui.dimensions.mediumPaddingValue),
        verticalArrangement = Arrangement.spacedBy(ui.dimensions.spacedByMedium)
    ) {

        items(
            items = events,
            key = { it.eventId }
        ) { event ->

            val dismissState = rememberSwipeToDismissBoxState(
                confirmValueChange = { value ->
                    when (value) {
                        SwipeToDismissBoxValue.StartToEnd -> {
                            onUpdateEventClick(event.eventId)
                            false
                        }

                        SwipeToDismissBoxValue.EndToStart -> {
                            val eventId: Int = event.eventId
                            val reminderId: Int? = if (event.reminderId == 0) null else event.reminderId
                            onDeleteClick(reminderId, eventId, selectedDate)

                            if (reminderId != null) {
                                AlarmScheduler.cancelAlarm(context, reminderId)
                            }
                            false
                        }

                        SwipeToDismissBoxValue.Settled -> false
                    }
                }
            )

            SwipeToDismissBox(
                state = dismissState,
                enableDismissFromStartToEnd = true,
                enableDismissFromEndToStart = true,
                backgroundContent = {
                    val direction = dismissState.dismissDirection

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(MaterialTheme.shapes.small)
                            .background(
                                when (direction) {
                                    SwipeToDismissBoxValue.StartToEnd ->
                                        Color(0xFF1DE9B6)

                                    SwipeToDismissBoxValue.EndToStart ->
                                        Color(0xFFFF1744)

                                    SwipeToDismissBoxValue.Settled ->
                                        Color.Transparent
                                }
                            )
                            .padding(horizontal = ui.dimensions.mediumPaddingValue),

                        contentAlignment = when (direction) {
                            SwipeToDismissBoxValue.StartToEnd ->
                                Alignment.CenterStart

                            else ->
                                Alignment.CenterEnd
                        }
                    ) {

                        Icon(
                            imageVector = when (direction) {

                                SwipeToDismissBoxValue.StartToEnd ->
                                    Icons.Default.Edit

                                SwipeToDismissBoxValue.EndToStart ->
                                    Icons.Default.Delete

                                SwipeToDismissBoxValue.Settled ->
                                    Icons.Default.Edit
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(ui.components.cardIconSize)
                        )
                    }
                }
            ) {
                EventCard(
                    event = event
                )
            }
        }
    }
}

@Composable
fun EventCard(
    event: CalendarEventUI
) {
    val ui = LocalUiScale.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = MaterialTheme.shapes.small,
        elevation = CardDefaults.cardElevation(defaultElevation = ui.components.smallCardElevation)
    ) {
        Box(
            modifier = Modifier
            .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                Box(
                    modifier = Modifier
                        .width(ui.components.colorStripSize)
                        .fillMaxHeight()
                        .background(event.color)
                )

                AdjustableSpacer(ui.dimensions.mediumSpacer)

                Column(
                    modifier = Modifier
                        .weight(ui.dimensions.generalWeight)
                        .padding(ui.dimensions.smallPaddingValue)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = event.title,
                            style = ui.typographies.eventCardTitleStyle,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.weight(ui.dimensions.generalWeight)
                        )

                        AdjustableSpacer(ui.dimensions.mediumSpacer)

                        Text(
                            text = "${event.startTime} - ${event.endTime}",
                            style = ui.typographies.eventCardTimeStyle,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = event.description,
                            style = ui.typographies.eventCardDescriptionStyle,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.weight(ui.dimensions.generalWeight)
                        )

                        AdjustableSpacer(ui.dimensions.mediumSpacer)

                        Icon(
                            imageVector = event.icon,
                            contentDescription = null,
                            tint = event.color,
                            modifier = Modifier.size(ui.components.cardIconSize)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventTopBar(
    title: String,
    onDatePickerClick: () -> Unit,
    onCreateEventClick: () -> Unit,
) {
    val ui = LocalUiScale.current

    CenterAlignedTopAppBar(
        title = {
            Box(
                modifier = Modifier.fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    fontSize = ui.fonts.generalTopBarTitleSize,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center
                )
            }
        },
        navigationIcon = {
            IconButton(
                onClick = onDatePickerClick,
                modifier = Modifier.size(ui.dimensions.generalTouchTarget)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(ui.components.generalTopBarIconSize),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        actions = {
            IconButton(
                onClick = onCreateEventClick,
                modifier = Modifier.size(ui.dimensions.generalTouchTarget)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(ui.components.generalTopBarIconSize),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier.height(ui.components.generalTopBarHeight)
    )
}