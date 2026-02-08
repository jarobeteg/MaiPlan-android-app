package com.example.maiplan.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun DateInputComponent(
    label: String,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    val displayDate = selectedDate?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) ?: ""

    val isTablet = isTablet()

    val fontSize = if (isTablet) 24.sp else 18.sp
    val style = if (isTablet) MaterialTheme.typography.labelLarge else MaterialTheme.typography.labelSmall
    val iconSize = if (isTablet) 36.dp else 24.dp
    val fieldHeight = if (isTablet) 72.dp else 64.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(fieldHeight)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { showDatePicker = true }
            .border(
                OutlinedTextFieldDefaults.UnfocusedBorderThickness,
                MaterialTheme.colorScheme.onBackground,
                OutlinedTextFieldDefaults.shape
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$label: $displayDate",
                fontSize = fontSize,
                style = style
            )
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(iconSize)
            )
        }
    }

    if (showDatePicker) {
        DatePickerDialogComponent(
            onDateSelected = {
                onDateSelected(it)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@Composable
fun TimeInputComponent(
    label: String,
    selectedTime: LocalTime?,
    onTimeSelected: (LocalTime) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    var showDialog by remember { mutableStateOf(false) }

    val displayTime = selectedTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: ""

    val isTablet = isTablet()

    val fontSize = if (isTablet) 24.sp else 18.sp
    val style = if (isTablet) MaterialTheme.typography.labelLarge else MaterialTheme.typography.labelSmall
    val iconSize = if (isTablet) 36.dp else 24.dp
    val fieldHeight = if (isTablet) 72.dp else 64.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(fieldHeight)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { showDialog = true }
            .border(
                OutlinedTextFieldDefaults.UnfocusedBorderThickness,
                MaterialTheme.colorScheme.onBackground,
                OutlinedTextFieldDefaults.shape
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$label: $displayTime",
                fontSize = fontSize,
                style = style
            )
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(iconSize)
            )
        }
    }

    if (showDialog) {
        TimePickerDialogComponent(
            onTimeSelected = {
                onTimeSelected(it)
                showDialog = false
            },
            onDismiss = {
                showDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalDateTimeInputField(
    label: String,
    dateTime: LocalDateTime?,
    onDateTimeSelected: (LocalDateTime) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var tempDate by remember { mutableStateOf(dateTime?.toLocalDate() ?: LocalDate.now()) }
    var tempTime by remember { mutableStateOf(dateTime?.toLocalTime() ?: LocalTime.now()) }

    val displayText = dateTime?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) ?: ""

    val isTablet = isTablet()

    val fontSize = if (isTablet) 24.sp else 18.sp
    val style = if (isTablet) MaterialTheme.typography.labelLarge else MaterialTheme.typography.labelSmall
    val iconSize = if (isTablet) 36.dp else 24.dp
    val fieldHeight = if (isTablet) 72.dp else 64.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(fieldHeight)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                showDatePicker = true
            }
            .border(
                OutlinedTextFieldDefaults.UnfocusedBorderThickness,
                MaterialTheme.colorScheme.onBackground,
                OutlinedTextFieldDefaults.shape
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$label: $displayText",
                fontSize = fontSize,
                style = style,
                color = MaterialTheme.colorScheme.onBackground
            )
            Icon(
                imageVector = Icons.Default.Event,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(iconSize)
            )
        }
    }
    if (showDatePicker) {
        DatePickerDialogComponent(
            onDateSelected = {
                tempDate = it
                showDatePicker = false
                showTimePicker = true
            },
            onDismiss = { showDatePicker = false }
        )
    }

    if (showTimePicker) {
        TimePickerDialogComponent(
            onTimeSelected = {
                tempTime = it
                showTimePicker = false
                onDateTimeSelected(LocalDateTime.of(tempDate, tempTime))
            },
            onDismiss = {
                showTimePicker = false
            }
        )
    }
}