package com.example.maiplan.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.maiplan.R
import com.example.maiplan.network.api.CategoryResponse
import com.example.maiplan.utils.common.IconData
import com.example.maiplan.utils.common.IconData.allIcons
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Reusable [Composable] that displays a heading style text.
 *
 * @param text The string to be displayed as the heading.
 */
@Composable
fun HeadingTextComponent(text: String) {
    Text(
        text = text,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

/**
 * Reusable [Composable] that displays clickable text without ripple effect.
 *
 * @param text The clickable string to be displayed.
 * @param onTextClicked Callback function that is invoked when the text is clicked.
 */
@Composable
fun ClickableTextComponent(text: String, onTextClicked: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Text(
        text = text,
        modifier = Modifier.clickable(
            interactionSource = interactionSource,
            indication = null
        ) { onTextClicked() },
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Medium
    )
}

/**
 * Reusable [Composable] that displays an email input field.
 *
 * Only non whitespace characters are allowed and the email string is limited to 64 characters.
 *
 * @param email The current text value of the email input field.
 * @param onEmailChange Callback invoked when the email input changes.
 *
 * @see OutlinedTextField
 */
@Composable
fun EmailTextComponent(email: String, onEmailChange: (String) -> Unit) {
    OutlinedTextField(
        value = email,
        onValueChange = { newEmail ->
            if (newEmail.length <= 64) {
                onEmailChange(newEmail.filter { !it.isWhitespace() })
            }
        },
        label = { Text(stringResource(R.string.email)) },
        leadingIcon = { Icon(Icons.Filled.Email, contentDescription = stringResource(R.string.email_icon)) },
        modifier = Modifier.fillMaxWidth(),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.onPrimary,
            unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary,
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            cursorColor = MaterialTheme.colorScheme.primary,
        ),
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Email,
            autoCorrectEnabled = false
        )
    )
}

/**
 * Reusable [Composable] that displays username input field.
 *
 * Only non whitespace characters are allowed and the username input string is limited to 32 characters
 *
 * @param username The current text value of the username input field.
 * @param onUsernameChange Callback invoked when the username input changes.
 *
 * @see OutlinedTextField
 */
@Composable
fun UsernameTextComponent(username: String, onUsernameChange: (String) -> Unit) {
    OutlinedTextField(
        value = username,
        onValueChange = { newUsername ->
            if (newUsername.length <= 32) {
                onUsernameChange(newUsername.filter { !it.isWhitespace() })
            }
        },
        label = { Text(stringResource(R.string.username)) },
        leadingIcon = { Icon(Icons.Filled.AccountCircle, contentDescription = stringResource(R.string.username_icon)) },
        modifier = Modifier.fillMaxWidth(),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.onPrimary,
            unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary,
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            cursorColor = MaterialTheme.colorScheme.primary
        ),
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Text,
            autoCorrectEnabled = false
        )
    )
}

/**
 * Reusable [Composable] that displays password input field with optional visibility toggle and strength indicator.
 *
 * Only non whitespace characters are allowed and the password input string is limited to 64 characters.
 *
 * @param password The current text value of the password input field.
 * @param label Localized label for the password input field.
 * @param onPasswordChange Callback invoked when the password input changes.
 * @param passwordVisible Whether the password should be shown in plain text.
 * @param onTogglePasswordVisibility Called when the visibility icon is clicked.
 * @param shouldIndicatorBeVisible Controls if the strength indicator should be shown.
 *
 * @see OutlinedTextField
 */
@Composable
fun PasswordTextComponent(
    password: CharArray,
    label: String,
    onPasswordChange: (CharArray) -> Unit,
    passwordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    shouldIndicatorBeVisible: Boolean = false
) {
    // CharArray converted to String to display in TextField
    val passwordString = remember(password) { String(password) }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Column {
        OutlinedTextField(
            value = passwordString,
            onValueChange = { newPasswordStr ->
                if (newPasswordStr.length <= 64) {
                    password.fill('\u0000') // clear old password array from memory
                    onPasswordChange(newPasswordStr.filter { !it.isWhitespace() }.toCharArray())
                }
            },
            label = { Text(label) },
            leadingIcon = {
                Icon(
                    Icons.Filled.Lock,
                    contentDescription = stringResource(R.string.password_icon)
                )
            },
            trailingIcon = {
                IconButton(onClick = onTogglePasswordVisibility) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = stringResource(R.string.toggle_password_visibility)
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            interactionSource = interactionSource,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.onPrimary,
                unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Password,
                autoCorrectEnabled = false
            )
        )

        if (shouldIndicatorBeVisible) {
            PasswordStrengthBar(passwordString, isFocused)
        } else {
            PasswordStrengthBar(passwordString, false)
        }
    }
}

/**
 * Reusable [Composable] that displays a visual password strength indicator based on password rules.
 *
 * Strength bar has 5 levels (localized labels):
 * - `Very Weak`
 * - `Weak`
 * - `Medium`
 * - `Strong`
 * - `Very Strong`
 *
 * Strength is evaluated by:
 * - Minimum `5` characters
 * - Contains `lowercase`, `uppercase`, `digits`, and `special characters (! _ @ # $ ?)`
 *
 * The bar only appears when the password field is focused.
 */
@Composable
fun PasswordStrengthBar(password: String, isFocused: Boolean) {
    if (isFocused) {
        Spacer(modifier = Modifier.height(8.dp))

        val hasMinLength = password.length >= 8
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecialChar = password.any { it in "!_@#$?" }

        val rawScore = listOf(
            hasMinLength,
            hasLowerCase,
            hasUpperCase,
            hasDigit,
            hasSpecialChar
        ).count { it }

        val score = (rawScore - 1).coerceAtLeast(0)


        val strengthColors = listOf(
            Color(0xFFD32F2F),
            Color(0xFFF57C00),
            Color(0xFFFBC02D),
            Color(0xFF388E3C),
            Color(0xFF1976D2)
        )

        val strengthLabels = listOf(
            stringResource(R.string.very_weak),
            stringResource(R.string.weak),
            stringResource(R.string.medium),
            stringResource(R.string.strong),
            stringResource(R.string.very_strong)
        )

        Column(modifier = Modifier.fillMaxWidth()) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
            ) {
                val progress = (size.width * (score / 4f)).coerceIn(0f, size.width)
                drawRect(
                    color = Color(0xFFB0BEC5),
                    size = size
                )
                drawRect(
                    color = strengthColors[score.coerceIn(0, 4)],
                    size = androidx.compose.ui.geometry.Size(progress, size.height)
                )
            }
            Text(
                text = strengthLabels[score.coerceIn(0, 4)],
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = strengthColors[score.coerceIn(0, 4)],
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

/**
 * Reusable [Composable] that displays a flexible text field with a customizable character limit.
 *
 * @param value The current text value of the input field.
 * @param label Localized label for the input field.
 * @param icon The [ImageVector] used as the trailing icon.
 * @param length The maximum number of characters allowed.
 * @param onValueChange Callback invoked when the input value changes.
 *
 * @see OutlinedTextField
 */
@Composable
fun AdjustableTextFieldLengthComponent(value: String, label: String, icon: ImageVector, length: Int, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            if (newValue.length <= length) {
                onValueChange(newValue)
            }
        },
        label = { Text(label) },
        trailingIcon = {
            Box(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(36.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        },
        modifier = Modifier.fillMaxWidth(),
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
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Text,
            autoCorrectEnabled = false
        )
    )
}

/**
 * Reusable [Composable] that displays a simple submit button for forms or triggering actions.
 *
 * @param value Localized text displayed inside the button.
 * @param onButtonClicked Callback invoked when the button is clicked.
 */
@Composable
fun SubmitButtonComponent(value: String, onButtonClicked: () -> Unit) {
    Button(
        onClick = onButtonClicked,
        modifier = Modifier
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.extraSmall,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Text(value, fontSize = 16.sp)
    }
}

/**
 * Reusable [Composable] that displays error messages
 *
 * @param value Localized error message text to display.
 */
@Composable
fun ErrorMessageComponent(value: String) {
    Text(
        text = value,
        color = MaterialTheme.colorScheme.onError,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    )
}

/**
 * Reusable [Composable] that displays Material3 [DatePicker] dialog that allows users to select a date.
 *
 * @param onDateSelected Callback invoked when the user selects a date and confirms.
 * @param onDismiss Callback invoked when the dialog is dismissed without selection.
 *
 * @see DatePicker
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialogComponent(
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
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
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary)
            ) {
                Text(stringResource(R.string.cancel))
            }
        },
        colors = DatePickerDefaults.colors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        DatePicker(
            state = datePickerState,
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
    }
}

/**
 * Reusable [Composable] that displays a time picker dialog using Material3's [TimePicker].
 *
 * The user can select an hour and minute, and confirm or cancel the selection.
 *
 * @param onTimeSelected Callback invoked with the selected [LocalTime] when the user confirms.
 * @param onDismiss Callback invoked when the dialog is dismissed or canceled.
 *
 * @see TimePicker
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialogComponent(
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
) {
    val timePickerState = rememberTimePickerState()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = MaterialTheme.colorScheme.primary,
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

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    TextButton(onClick = {
                        val hour = timePickerState.hour
                        val minute = timePickerState.minute
                        onTimeSelected(LocalTime.of(hour, minute))
                    }) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

/**
 * Reusable [Composable] that displays a search field with character limit and leading search icon.
 *
 * @param searchQuery The current query text input field.
 * @param length Maximum allowed character length.
 * @param onValueChange Callback invoked when the input value changes.
 *
 * @see OutlinedTextField
 */
@Composable
fun SearchFieldComponent(searchQuery: String, length: Int, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = { newValue ->
            if (newValue.length <= length) {
                onValueChange(newValue)
            }
        },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(stringResource(R.string.category_search)) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
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
}

/**
 * Reusable [Composable] that displays a row that shows the selected color and opens a color picker dialog when clicked.
 *
 * @param selectedColor The current selected color.
 * @param onColorSelected Callback invoked when a new color is selected from the dialog
 */
@Composable
fun ColorPickerRow(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    var showColorPicker by remember { mutableStateOf(false) }

    val borderColor = MaterialTheme.colorScheme.onBackground
    val borderWidth = OutlinedTextFieldDefaults.UnfocusedBorderThickness
    val borderShape = OutlinedTextFieldDefaults.shape

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { showColorPicker = true }
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
                text = stringResource(R.string.category_color),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(selectedColor, CircleShape)
                    .border(1.dp, Color.Gray, CircleShape)
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

/**
 * Reusable [Composable] that displays a custom slider thumb and track background.
 *
 * @param value The current slider value.
 * @param onValueChange Callback invoked when the slider value changes.
 * @param valueRange The allowed range of values for the slider.
 * @param trackBrush A brush used to paint the track background.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    trackBrush: Brush
) {
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
            .height(64.dp),
        thumb = {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
        },
        track = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .background(trackBrush, MaterialTheme.shapes.extraSmall)
            )
        }
    )
}

/**
 * Reusable [Composable] that displays a color picker dialog,
 * allows the user to pick a color using `HSV` sliders for `Hue`, `Saturation`, and `Brightness`.
 *
 * @param initialColor The color to start with when the dialog opens.
 * @param onColorSelected Callback invoked when the `User` selects a color and confirms.
 * @param onDismiss Callback invoked when the dialog is dismissed without selecting a color.
 */
@Composable
fun ColorPickerDialog(
    initialColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
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
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(color, CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.onPrimary, CircleShape)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(stringResource(R.string.hue), color = MaterialTheme.colorScheme.onBackground)
                CustomSlider(
                    value = hue,
                    onValueChange = { hue = it },
                    valueRange = 0f..360f,
                    trackBrush = Brush.horizontalGradient(
                        colors = (0..360 step 30).map { Color.hsv(it.toFloat(), 1f, 1f) }
                    )
                )

                Text(stringResource(R.string.saturation), color = MaterialTheme.colorScheme.onBackground)
                CustomSlider(
                    value = saturation,
                    onValueChange = { saturation = it },
                    valueRange = 0f..1f,
                    trackBrush = Brush.horizontalGradient(
                        colors = listOf(Color.hsv(hue, 0f, 1f), Color.hsv(hue, 1f, 1f))
                    )
                )

                Text(stringResource(R.string.brightness), color = MaterialTheme.colorScheme.onBackground)
                CustomSlider(
                    value = value,
                    onValueChange = { value = it },
                    valueRange = 0f..1f,
                    trackBrush = Brush.horizontalGradient(
                        colors = listOf(Color.Black, Color.hsv(hue, saturation, 1f))
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.onBackground) }
                    TextButton(onClick = { onColorSelected(color); onDismiss() }) { Text(
                        stringResource(R.string.select), color = MaterialTheme.colorScheme.onBackground) }
                }
            }
        }
    }
}

/**
 * Reusable [Composable] that displays a row that shows the selected icon and open an icon picker dialog when clicked.
 *
 * @param selectedIcon The currently selected icon.
 * @param onIconSelected Callback invoked when a new icon is selected from the dialog.
 * @param onIconSelectedString Callback invoked with selected icon's name as a string for storing purpose.
 */
@Composable
fun IconPickerRow(
    selectedIcon: ImageVector?,
    onIconSelected: (ImageVector) -> Unit,
    onIconSelectedString: (String) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    var showIconPicker by remember { mutableStateOf(false) }

    val borderColor = MaterialTheme.colorScheme.onBackground
    val borderWidth = OutlinedTextFieldDefaults.UnfocusedBorderThickness
    val borderShape = OutlinedTextFieldDefaults.shape

    Box(
        modifier = Modifier
            .fillMaxWidth()
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
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                    .size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                selectedIcon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
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

/**
 * Reusable [Composable] that displays a dialog of scrollable grid of icons.
 *
 * @param onIconSelected Callback invoked when an icon is clicked.
 * @param onIconSelectedString Callback invoked with the icon's key (identifier) as a string.
 * @param onDismiss Callback invoked when the dialog is dismissed without selecting an icon.
 */
@Composable
fun IconPickerDialog(
    onIconSelected: (ImageVector) -> Unit,
    onIconSelectedString: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val scrollState = rememberLazyGridState()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.tertiaryContainer,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .height(300.dp)
                        .fillMaxWidth()
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        contentPadding = PaddingValues(8.dp),
                        state = scrollState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(allIcons.entries.toList()) { (key, icon) ->
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clickable { onIconSelected(icon); onIconSelectedString(key); onDismiss() }
                            )
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
                                .height(24.dp)
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
                    Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.onBackground)
                }
            }
        }
    }
}

/**
 * Reusable [Composable] that displays a simple top app bar with a centered title and a back arrow icon.
 *
 * @param text Localized title displayed in the center of the top app bar.
 * @param onBackClick Callback invoked when the back arrow is clicked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleTopBar(
    text: String,
    onBackClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    )
}

/**
 * Reusable [Composable] field that displays a selected [LocalDate] and opens a date picker dialog when tapped.
 *
 * @param label The label to display alongside the selected date.
 * @param selectedDate The current selected date, or null if none.
 * @param onDateSelected Callback triggered when a date is picked.
 */

@Composable
fun DateInputComponent(
    label: String,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    val displayDate = selectedDate?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) ?: ""

    Box(
        modifier = Modifier
            .fillMaxWidth()
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
                style = MaterialTheme.typography.bodyLarge
            )
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(36.dp)
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

/**
 * Reusable [Composable] field that displays a selected [LocalTime] and opens a picker picker dialog when tapped.
 *
 * @param label The label shown with the time.
 * @param selectedTime The currently selected time, or null.
 * @param onTimeSelected Callback when a time is selected.
 */
@Composable
fun TimeInputComponent(
    label: String,
    selectedTime: LocalTime?,
    onTimeSelected: (LocalTime) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    var showDialog by remember { mutableStateOf(false) }

    val displayTime = selectedTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: ""

    Box(
        modifier = Modifier
            .fillMaxWidth()
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
                style = MaterialTheme.typography.bodyLarge
            )
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(36.dp)
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

/**
 * Reusable [Composable] Dropdown menu for selecting priority (low, mid, high) using localized strings.
 *
 * @param selectedPriority The current priority level (1 to 3).
 * @param onPrioritySelected Callback triggered when a new priority is chosen.
 */
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

/**
 * Reusable [Composable] that displays a centered large section title.
 *
 * @param text The text content to display.
 */
@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )
}

/**
 * Reusable [Composable] field that allows selection of both date and time, returning a [LocalDateTime].
 *
 * @param label The label for the field.
 * @param dateTime Currently selected date-time or null.
 * @param onDateTimeSelected Callback with the full selected [LocalDateTime].
 */
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
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
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Icon(
                imageVector = Icons.Default.Event,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(36.dp)
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

/**
 * Reusable [Composable] dropdown menu for selecting a [CategoryResponse], showing its icon and name.
 *
 * @param categories List of available categories to choose from.
 * @param selectedCategory Currently selected category, or null.
 * @param onCategorySelected Callback when a category is selected.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdownComponent(
    categories: List<CategoryResponse>,
    selectedCategory: CategoryResponse?,
    onCategorySelected: (CategoryResponse) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedCategory?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Category") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = IconData.getIconByKey(category.icon),
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(text = category.name, color = MaterialTheme.colorScheme.onBackground)
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