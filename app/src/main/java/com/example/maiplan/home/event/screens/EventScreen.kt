package com.example.maiplan.home.event.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.getString
import androidx.navigation.compose.rememberNavController
import com.example.maiplan.R
import com.example.maiplan.components.DatePickerDialogComponent
import com.example.maiplan.home.BottomNavigationBar
import com.example.maiplan.home.event.EventNavHost
import com.example.maiplan.home.event.EventRoutes
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale

@Composable
fun EventScreenWithNav(context: Context) {
    val navController = rememberNavController()
    var selectedView by rememberSaveable { mutableIntStateOf(0) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
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
                onViewSelected = { index ->
                    selectedView = index
                    val route = when (index) {
                        0 -> EventRoutes.Monthly.route
                        1 -> EventRoutes.Weekly.route
                        2 -> EventRoutes.Daily.route
                        else -> EventRoutes.Monthly.route
                    }
                    navController.navigate(route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onDatePickerClick = { showDatePicker = true }) },
        bottomBar = { BottomNavigationBar(context) }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            EventNavHost(navController, selectedDate, context)
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
    onDatePickerClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var buttonWidth by remember { mutableIntStateOf(0) }

    TopAppBar(
        title = {
            Box(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.primary)
                    .border(2.dp, MaterialTheme.colorScheme.secondary, MaterialTheme.shapes.medium)
            ) {
                OutlinedButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier
                        .height(40.dp)
                        .onGloballyPositioned { coordinates ->
                            buttonWidth = coordinates.size.width
                        },
                    border = null,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary)
                ) {
                    Text(
                        text = formattedTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown Arrow",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.rotate(if (expanded) 90f else 0f)
                    )
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
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        actions = {
            IconButton(onClick = onDatePickerClick) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
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
                text = { Text(view, color = MaterialTheme.colorScheme.onPrimary) },
                onClick = {
                    onItemSelected(index)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}