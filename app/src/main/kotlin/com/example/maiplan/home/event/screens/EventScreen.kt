package com.example.maiplan.home.event.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavHostController
import com.example.maiplan.R
import com.example.maiplan.components.DatePickerDialogComponent
import com.example.maiplan.home.event.utils.LocalDateSaver
import com.example.maiplan.home.navigation.HomeNavigationBar
import com.example.maiplan.utils.LocalUiScale
import com.example.maiplan.viewmodel.event.EventViewModel
import java.time.LocalDate

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
    var showDatePicker by remember { mutableStateOf(false) }
    val closeDatePicker = { showDatePicker = false }

    Scaffold(
        topBar = {
            EventTopBar(
                title = stringResource(R.string.march),
                onDatePickerClick = { showDatePicker = true },
                onCreateEventClick = onCreateEventClick
            )},
        bottomBar = { HomeNavigationBar(rootNavController, context) }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {}
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