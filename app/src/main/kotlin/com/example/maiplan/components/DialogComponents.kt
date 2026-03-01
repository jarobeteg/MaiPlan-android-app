package com.example.maiplan.components

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.maiplan.R
import com.example.maiplan.database.entities.CategoryEntity
import com.example.maiplan.utils.LocalUiScale
import com.example.maiplan.utils.common.IconData
import com.example.maiplan.utils.common.IconData.allIcons
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.forEach

@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialogComponent(
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    val configuration = LocalConfiguration.current
    val orientation = configuration.orientation

    val baseWidth = 360f
    val baseHeight = 550f

    val scaleFactor = remember(configuration) {
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            val targetHeight = configuration.screenHeightDp * 0.65f
            targetHeight / baseHeight
        } else {
            val targetWidth = configuration.screenWidthDp * 0.6f
            targetWidth / baseWidth
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .width(360.dp)
                    .scale(scaleFactor),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.primary,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(bottom = 8.dp),
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
                        Spacer(modifier = Modifier.width(8.dp))
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

@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialogComponent(
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
) {
    val timePickerState = rememberTimePickerState(is24Hour = true)

    val configuration = LocalConfiguration.current
    val orientation = configuration.orientation
    val isLandscape = orientation == Configuration.ORIENTATION_LANDSCAPE

    val baseWidth = 360f
    val baseHeight = 550f

    val scaleFactor = remember(configuration) {
        if (isLandscape) {
            val targetHeight = configuration.screenHeightDp * 0.65f
            targetHeight / baseHeight
        } else {
            val targetWidth = configuration.screenWidthDp * 0.6f
            targetWidth / baseWidth
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = if (isLandscape) {
                    Modifier
                        .fillMaxWidth(0.45f)
                        .scale(scaleFactor)
                } else {
                    Modifier
                        .width(baseWidth.dp)
                        .scale(scaleFactor)
                },
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.primary,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
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

                    Spacer(modifier = Modifier.height(12.dp))

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
                        Spacer(modifier = Modifier.width(8.dp))
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
    val ui = LocalUiScale.current
    val interactionSource = remember { MutableInteractionSource() }
    var showColorPicker by remember { mutableStateOf(false) }

    val borderColor = MaterialTheme.colorScheme.onBackground
    val borderWidth = OutlinedTextFieldDefaults.UnfocusedBorderThickness
    val borderShape = OutlinedTextFieldDefaults.shape

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(ui.components.generalFieldHeight)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { showColorPicker = true }
            )
            .border(borderWidth, borderColor, borderShape)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.category_color),
                fontSize = ui.fonts.generalTextSize,
                style = ui.typographies.generalTextStyle,
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                    .size(ui.components.generalIconSize)
                    .background(selectedColor, CircleShape)
                    .border(width = ui.dimensions.generalBorder, Color.Gray, CircleShape)
            )
        }
    }

    if (showColorPicker) {
        ColorPickerDialog(
            initialColor = selectedColor,
            onColorSelected = {
                onColorSelected(it)
                showColorPicker = false
            },
            onDismiss = { showColorPicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    trackBrush: Brush
) {
    val ui = LocalUiScale.current

    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        colors = SliderDefaults.colors(
            thumbColor = MaterialTheme.colorScheme.primary,
            activeTrackColor = Color.Transparent,
            inactiveTrackColor = Color.Transparent
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(ui.components.sliderHeight),
        thumb = {
            Box(
                modifier = Modifier
                    .size(ui.components.thumbSize)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
        },
        track = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ui.components.trackHeight)
                    .background(trackBrush, MaterialTheme.shapes.extraSmall)
            )
        }
    )
}

@Composable
fun ColorPickerDialog(
    initialColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    val ui = LocalUiScale.current

    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(initialColor.toArgb(), hsv)

    var hue by remember { mutableFloatStateOf(hsv[0]) }
    var saturation by remember { mutableFloatStateOf(hsv[1]) }
    var value by remember { mutableFloatStateOf(hsv[2]) }
    var color by remember { mutableStateOf(initialColor) }

    LaunchedEffect(hue, saturation, value) {
        color = Color.hsv(hue, saturation, value)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.tertiaryContainer,
            modifier = Modifier
                .padding(ui.dimensions.dialogPadding)
                .then(Modifier.width(ui.components.dialogWidth))
        ) {
            Column(
                modifier = Modifier.padding(ui.dimensions.dialogPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(ui.components.previewSize)
                        .background(color, CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.onPrimary, CircleShape)
                )

                AdjustableSpacer(ui.dimensions.generalSpacer)

                Text(stringResource(R.string.hue), fontSize = ui.fonts.generalTextSize, color = MaterialTheme.colorScheme.onBackground)
                CustomSlider(
                    value = hue,
                    onValueChange = { hue = it },
                    valueRange = 0f..360f,
                    trackBrush = Brush.horizontalGradient(
                        colors = (0..360 step 30).map { Color.hsv(it.toFloat(), 1f, 1f) }
                    )
                )

                Text(stringResource(R.string.saturation), fontSize = ui.fonts.generalTextSize, color = MaterialTheme.colorScheme.onBackground)
                CustomSlider(
                    value = saturation,
                    onValueChange = { saturation = it },
                    valueRange = 0f..1f,
                    trackBrush = Brush.horizontalGradient(
                        colors = listOf(Color.hsv(hue, 0f, 1f), Color.hsv(hue, 1f, 1f))
                    )
                )

                Text(stringResource(R.string.brightness), fontSize = ui.fonts.generalTextSize, color = MaterialTheme.colorScheme.onBackground)
                CustomSlider(
                    value = value,
                    onValueChange = { value = it },
                    valueRange = 0f..1f,
                    trackBrush = Brush.horizontalGradient(
                        colors = listOf(Color.Black, Color.hsv(hue, saturation, 1f))
                    )
                )

                AdjustableSpacer(ui.dimensions.generalSpacer)

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel), fontSize = ui.fonts.generalTextSize, color = MaterialTheme.colorScheme.onBackground) }
                    TextButton(onClick = { onColorSelected(color); onDismiss() }) { Text(
                        stringResource(R.string.select), fontSize = ui.fonts.generalTextSize, color = MaterialTheme.colorScheme.onBackground) }
                }
            }
        }
    }
}

@Composable
fun IconPickerRow(
    selectedIcon: ImageVector?,
    onIconSelected: (ImageVector) -> Unit,
    onIconSelectedString: (String) -> Unit
) {
    val ui = LocalUiScale.current
    val interactionSource = remember { MutableInteractionSource() }
    var showIconPicker by remember { mutableStateOf(false) }

    val borderColor = MaterialTheme.colorScheme.onBackground
    val borderWidth = OutlinedTextFieldDefaults.UnfocusedBorderThickness
    val borderShape = OutlinedTextFieldDefaults.shape

    Box(
        modifier = Modifier
            .fillMaxWidth().height(ui.components.generalFieldHeight)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { showIconPicker = true }
            )
            .border(borderWidth, borderColor, borderShape)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.category_icon),
                fontSize = ui.fonts.generalTextSize,
                style = ui.typographies.generalTextStyle,
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                    .size(ui.components.generalIconSize),
                contentAlignment = Alignment.Center
            ) {
                selectedIcon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }

    if (showIconPicker) {
        IconPickerDialog(
            onIconSelected = {
                onIconSelected(it)
                showIconPicker = false
            },
            onIconSelectedString = {
                onIconSelectedString(it)
                showIconPicker = false
            },
            onDismiss = { showIconPicker = false }
        )
    }
}

@Composable
fun IconPickerDialog(
    onIconSelected: (ImageVector) -> Unit,
    onIconSelectedString: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val ui = LocalUiScale.current
    val scrollState = rememberLazyGridState()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.tertiaryContainer,
            modifier = Modifier
                .padding(ui.dimensions.iconPickerDialogPadding)
                .then(Modifier.width(ui.components.dialogWidth))
        ) {
            Column(
                modifier = Modifier.padding(ui.dimensions.iconPickerDialogPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .height(ui.components.gridHeight)
                        .fillMaxWidth()
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = ui.components.iconPickerIconSize),
                        contentPadding = PaddingValues(12.dp),
                        state = scrollState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(allIcons.entries.toList()) { (key, icon) ->
                            Box(
                                modifier = Modifier
                                    .size(ui.components.iconPickerIconSize)
                                    .padding(ui.dimensions.gridPadding)
                                    .clickable {
                                        onIconSelected(icon)
                                        onIconSelectedString(key)
                                        onDismiss()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                        }
                    }

                    val showScrollIndicator by remember {
                        derivedStateOf { scrollState.canScrollForward }
                    }
                    if (showScrollIndicator) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .height(ui.components.generalIconSize)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                                        )
                                    )
                                )
                        )
                    }
                }

                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text(stringResource(R.string.cancel), fontSize = ui.fonts.generalTextSize, color = MaterialTheme.colorScheme.onBackground)
                }
            }
        }
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
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
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
    val ui = LocalUiScale.current
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.height(ui.components.generalFieldHeight)
    ) {
        OutlinedTextField(
            value = selectedCategory?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(
                text = "Category",
                fontSize = ui.fonts.generalTextSize,
                style = ui.typographies.generalTextStyle
            ) },
            textStyle = TextStyle(fontSize = ui.fonts.generalTextSize),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded, modifier = Modifier.size(ui.components.generalIconSize)) },
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
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
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
                            modifier = Modifier.height(ui.components.generalFieldHeight)
                        ) {
                            Icon(
                                imageVector = IconData.getIconByKey(category.icon),
                                contentDescription = null,
                                modifier = Modifier.size(ui.components.generalIconSize).padding(end = 8.dp)
                            )
                            Text(
                                text = category.name,
                                fontSize = ui.fonts.generalTextSize,
                                style = ui.typographies.generalTextStyle,
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