package com.example.maiplan.home.screens.event

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.getString
import com.example.maiplan.R
import com.example.maiplan.components.DatePickerDialogComponent
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale

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

    val formattedTitle = getFormattedTitle(context, selectedView, selectedDate)
    val views = listOf(
        getString(context, R.string.monthly),
        getString(context, R.string.weekly),
        getString(context, R.string.daily)
    )

    Scaffold(
        topBar = { EventTopBar(formattedTitle, rotationAngle, expanded, { expanded = !expanded }, views, selectedView, { selectedView = it }, { showDatePicker = true }) }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            when (selectedView) {
                0 -> MonthlyView(selectedDate, context)
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
    rotationAngle: Float,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    views: List<String>,
    selectedView: Int,
    onViewSelected: (Int) -> Unit,
    onDatePickerClick: () -> Unit
) {
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
                    onClick = onToggleExpand,
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
                        modifier = Modifier.rotate(rotationAngle)
                    )
                }

                EventDropdownMenu(expanded, views, buttonWidth, selectedView) { index ->
                    onViewSelected(index)
                    onToggleExpand()
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        actions = {
            IconButton(onClick = onDatePickerClick) {
                Icon(Icons.Default.CalendarMonth, contentDescription = "Select Date", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    )
}

@Composable
fun EventDropdownMenu(
    expanded: Boolean,
    views: List<String>,
    buttonWidth: Int,
    selectedView: Int,
    onItemSelected: (Int) -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { onItemSelected(selectedView) },
        modifier = Modifier
            .width(with(LocalDensity.current) { buttonWidth.toDp() })
            .background(MaterialTheme.colorScheme.primary)
            .clip(MaterialTheme.shapes.medium)
    ) {
        views.forEachIndexed { index, view ->
            DropdownMenuItem(
                text = { Text(view, color = MaterialTheme.colorScheme.onPrimary) },
                onClick = { onItemSelected(index) },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewActivityScreen() {
    EventScreen()
}