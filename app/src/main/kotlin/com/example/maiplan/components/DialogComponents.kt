package com.example.maiplan.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.maiplan.R
import com.example.maiplan.database.entities.CategoryEntity
import com.example.maiplan.utils.LocalAppDesign
import com.example.maiplan.utils.common.IconData
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlin.collections.forEach

private val DialogIconSize = 24.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialogComponent(
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val ui = LocalAppDesign.current
    val datePickerState = rememberDatePickerState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(ui.dimensions.dialogPadding),
            contentAlignment = Alignment.Center,
        ) {

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        onDismiss()
                    }
            )

            Surface(
                    modifier = Modifier
                        .widthIn(max = 360.dp)
                        .fillMaxWidth()
                        .heightIn(max = 600.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.primary,
                    tonalElevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        DatePicker(
                            state = datePickerState,
                            showModeToggle = false,
                            colors = DatePickerDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                                headlineContentColor = MaterialTheme.colorScheme.onPrimary,
                                weekdayContentColor = MaterialTheme.colorScheme.secondaryContainer,
                                subheadContentColor = MaterialTheme.colorScheme.onPrimary,
                                yearContentColor = MaterialTheme.colorScheme.onPrimary,
                                selectedDayContentColor = MaterialTheme.colorScheme.primary,
                                selectedDayContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedYearContentColor = MaterialTheme.colorScheme.primary,
                                selectedYearContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                todayDateBorderColor = MaterialTheme.colorScheme.secondary,
                                todayContentColor = MaterialTheme.colorScheme.secondaryContainer,
                                currentYearContentColor = MaterialTheme.colorScheme.secondaryContainer,
                                dayContentColor = MaterialTheme.colorScheme.onPrimary,
                                dayInSelectionRangeContentColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 16.dp, bottom = 16.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = onDismiss,
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary)
                            ) {
                                Text("Cancel")
                            }
                            AdjustableSpacer(ui.dimensions.mediumSpacer)
                            TextButton(
                                onClick = {
                                    val selectedDateMillis = datePickerState.selectedDateMillis
                                    if (selectedDateMillis != null) {
                                        val selectedDate = Instant.ofEpochMilli(selectedDateMillis)
                                            .atZone(ZoneId.systemDefault())
                                            .toLocalDate()
                                        onDateSelected(selectedDate)
                                    }
                                    onDismiss()
                                },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary)
                            ) {
                                Text("OK")
                            }
                        }
                    }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialogComponent(
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
) {
    val ui = LocalAppDesign.current
    val timePickerState = rememberTimePickerState(is24Hour = true)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(ui.dimensions.dialogPadding),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .widthIn(max = 360.dp)
                    .fillMaxWidth()
                    .heightIn(max = 600.dp),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.primary,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(ui.dimensions.dialogPadding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TimePicker(
                        state = timePickerState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor = MaterialTheme.colorScheme.secondary,
                            clockDialSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
                            clockDialUnselectedContentColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectorColor = MaterialTheme.colorScheme.secondaryContainer,
                            containerColor = MaterialTheme.colorScheme.primary,
                            periodSelectorBorderColor = MaterialTheme.colorScheme.secondary,
                            periodSelectorSelectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            periodSelectorUnselectedContainerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                            periodSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
                            periodSelectorUnselectedContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                            timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                            timeSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
                            timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                        )
                    )

                    AdjustableSpacer(ui.dimensions.largeSpacer)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary)
                        ) {
                            Text("Cancel", fontSize = 16.sp)
                        }
                        AdjustableSpacer(ui.dimensions.mediumSpacer)
                        TextButton(onClick = {
                            onTimeSelected(LocalTime.of(timePickerState.hour, timePickerState.minute))
                            onDismiss()
                        }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary)
                        ) {
                            Text("OK", fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ColorPickerRow(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    val ui = LocalAppDesign.current
    var showColorPicker by remember { mutableStateOf(false) }
    val closeColorPicker = { showColorPicker = false }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                showColorPicker = true
            }
    ) {
        OutlinedTextField(
            value = "",
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = {
                Text(
                    text = stringResource(R.string.category_color),
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            trailingIcon = {
                Box(
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(DialogIconSize)
                        .background(selectedColor, CircleShape)
                        .border(ui.dimensions.generalBorder, Color.Gray, CircleShape)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
            textStyle = MaterialTheme.typography.bodyLarge,
            colors = TextFieldDefaults.colors(
                disabledContainerColor = MaterialTheme.colorScheme.background,
                disabledIndicatorColor = MaterialTheme.colorScheme.onBackground,
                disabledTextColor = MaterialTheme.colorScheme.onBackground,
                disabledLabelColor = MaterialTheme.colorScheme.onBackground
            )
        )
    }

    if (showColorPicker) {
        ColorPickerDialog(
            initialColor = selectedColor,
            onColorSelected = {
                onColorSelected(it)
                closeColorPicker()
            },
            onDismiss = {
                closeColorPicker()
            }
        )
    }
}

@Composable
fun IconPickerRow(
    selectedIcon: ImageVector?,
    onIconSelected: (ImageVector) -> Unit,
    onIconSelectedString: (String) -> Unit
) {
    val ui = LocalAppDesign.current
    var showIconPicker by remember { mutableStateOf(false) }
    val closeIconPicker = { showIconPicker = false }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                showIconPicker = true
            }
    ) {
        OutlinedTextField(
            value = "",
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = {
                Text(
                    text = stringResource(R.string.category_icon),
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            trailingIcon = {
                selectedIcon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(DialogIconSize),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
            textStyle = MaterialTheme.typography.bodyLarge,
            colors = TextFieldDefaults.colors(
                disabledContainerColor = MaterialTheme.colorScheme.background,
                disabledIndicatorColor = MaterialTheme.colorScheme.onBackground,
                disabledTextColor = MaterialTheme.colorScheme.onBackground,
                disabledLabelColor = MaterialTheme.colorScheme.onBackground
            )
        )
    }

    if (showIconPicker) {
        IconPickerDialog(
            initialIcon = selectedIcon,
            onIconSelected = {
                onIconSelected(it)
                closeIconPicker()
            },
            onIconSelectedString = {
                onIconSelectedString(it)
                closeIconPicker()
            },
            onDismiss = {
                closeIconPicker()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriorityDropdown(
    selectedPriority: Int,
    onPrioritySelected: (Int) -> Unit
) {
    val priorityMap = mapOf(
        1 to stringResource(R.string.low),
        2 to stringResource(R.string.mid),
        3 to stringResource(R.string.high)
    )

    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = priorityMap[selectedPriority] ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Priority") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.background,
                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.onBackground,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                focusedLabelColor = MaterialTheme.colorScheme.onBackground,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary)
        ) {
            priorityMap.forEach { (value, label) ->
                DropdownMenuItem(
                    text = {
                        Text(text = label, color = MaterialTheme.colorScheme.onBackground)
                    },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary)
                        .fillMaxWidth(),
                    onClick = {
                        onPrioritySelected(value)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdownComponent(
    categories: List<CategoryEntity>,
    selectedCategory: CategoryEntity?,
    onCategorySelected: (CategoryEntity) -> Unit
) {
    val ui = LocalAppDesign.current
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.heightIn(min = 56.dp)
    ) {
        OutlinedTextField(
            value = selectedCategory?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(
                text = "Category",
                style = MaterialTheme.typography.bodyLarge,
            ) },
            textStyle = MaterialTheme.typography.bodyLarge,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded, modifier = Modifier.size(DialogIconSize)) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.background,
                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.onBackground,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                focusedLabelColor = MaterialTheme.colorScheme.onBackground,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary)
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.heightIn(min = 56.dp)
                        ) {
                            Icon(
                                imageVector = IconData.getIconByKey(category.icon),
                                contentDescription = null,
                                modifier = Modifier.size(DialogIconSize).padding(end = 8.dp)
                            )
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground)
                        }
                    },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary)
                        .fillMaxWidth(),
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}
