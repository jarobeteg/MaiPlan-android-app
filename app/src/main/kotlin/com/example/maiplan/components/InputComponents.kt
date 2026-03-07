package com.example.maiplan.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.example.maiplan.utils.LocalUiScale
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
    val ui = LocalUiScale.current
    var showDatePicker by remember { mutableStateOf(false) }
    val closeDatePicker = { showDatePicker = false }

    val displayDate = selectedDate?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) ?: ""

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                showDatePicker = true
            }
    ) {
        OutlinedTextField(
            value = displayDate,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = {
                Text(
                    label,
                    fontSize = ui.fonts.generalTextSize,
                    style = ui.typographies.generalTextStyle
                )
            },
            trailingIcon = {
                Box(
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(ui.components.generalIconSize),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(ui.components.generalFieldHeight),
            textStyle = TextStyle(fontSize = ui.fonts.generalTextSize),
            colors = TextFieldDefaults.colors(
                disabledContainerColor = MaterialTheme.colorScheme.background,
                disabledIndicatorColor = MaterialTheme.colorScheme.onBackground,
                disabledTextColor = MaterialTheme.colorScheme.onBackground,
                disabledLabelColor = MaterialTheme.colorScheme.onBackground
            )
        )
    }

    if (showDatePicker) {
        DatePickerDialogComponent(
            onDateSelected = {
                onDateSelected(it)
                closeDatePicker()
            },
            onDismiss = {
                closeDatePicker()
            }
        )
    }
}

@Composable
fun TimeInputComponent(
    label: String,
    selectedTime: LocalTime?,
    onTimeSelected: (LocalTime) -> Unit
) {
    val ui = LocalUiScale.current
    var showTimePicker by remember { mutableStateOf(false) }
    val closeTimePicker = { showTimePicker = false }

    val displayTime = selectedTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: ""

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                showTimePicker = true
            }
    ) {
        OutlinedTextField(
            value = displayTime,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = {
                Text(
                    label,
                    fontSize = ui.fonts.generalTextSize,
                    style = ui.typographies.generalTextStyle
                )
            },
            trailingIcon = {
                Box(
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(ui.components.generalIconSize),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(ui.components.generalFieldHeight),
            textStyle = TextStyle(fontSize = ui.fonts.generalTextSize),
            colors = TextFieldDefaults.colors(
                disabledContainerColor = MaterialTheme.colorScheme.background,
                disabledIndicatorColor = MaterialTheme.colorScheme.onBackground,
                disabledTextColor = MaterialTheme.colorScheme.onBackground,
                disabledLabelColor = MaterialTheme.colorScheme.onBackground
            )
        )
    }

    if (showTimePicker) {
        TimePickerDialogComponent(
            onTimeSelected = {
                onTimeSelected(it)
                closeTimePicker()
            },
            onDismiss = {
                closeTimePicker()
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
    val ui = LocalUiScale.current
    var showDatePicker by remember { mutableStateOf(false) }
    val closeDatePicker = { showDatePicker = false }
    var showTimePicker by remember { mutableStateOf(false) }
    val closeTimePicker = { showTimePicker = false }
    var tempDate by remember { mutableStateOf(dateTime?.toLocalDate() ?: LocalDate.now()) }
    var tempTime by remember { mutableStateOf(dateTime?.toLocalTime() ?: LocalTime.now()) }

    val displayText = dateTime?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) ?: ""

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                showDatePicker = true
            }
    ) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = {
                Text(
                    label,
                    fontSize = ui.fonts.generalTextSize,
                    style = ui.typographies.generalTextStyle
                )
            },
            trailingIcon = {
                Box(
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(ui.components.generalIconSize),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(ui.components.generalFieldHeight),
            textStyle = TextStyle(fontSize = ui.fonts.generalTextSize),
            colors = TextFieldDefaults.colors(
                disabledContainerColor = MaterialTheme.colorScheme.background,
                disabledIndicatorColor = MaterialTheme.colorScheme.onBackground,
                disabledTextColor = MaterialTheme.colorScheme.onBackground,
                disabledLabelColor = MaterialTheme.colorScheme.onBackground
            )
        )
    }

    if (showDatePicker) {
        DatePickerDialogComponent(
            onDateSelected = {
                tempDate = it
                closeDatePicker()
                showTimePicker = true
            },
            onDismiss = {
                closeDatePicker()
            }
        )
    }

    if (showTimePicker) {
        TimePickerDialogComponent(
            onTimeSelected = {
                tempTime = it
                closeTimePicker()
                onDateTimeSelected(LocalDateTime.of(tempDate, tempTime))
            },
            onDismiss = {
                closeTimePicker()
            }
        )
    }
}