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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.maiplan.R
import com.example.maiplan.utils.IconData.allIcons
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun HeadingTextComponent(text: String) {
    Text(
        text = text,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

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

@Composable
fun PasswordTextComponent(
    password: String,
    label: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    shouldIndicatorBeVisible: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Column {
        OutlinedTextField(
            value = password,
            onValueChange = { newPassword ->
                if (newPassword.length <= 64) {
                    onPasswordChange(newPassword.filter { !it.isWhitespace() })
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
            PasswordStrengthBar(password, isFocused)
        } else {
            PasswordStrengthBar(password, false)
        }
    }
}

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

@Composable
fun AdjustableTextFieldLengthComponent(value: String, label: String, length: Int, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            if (newValue.length <= length) {
                onValueChange(newValue)
            }
        },
        label = { Text(label) },
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

@Composable
fun ErrorMessageComponent(value: String) {
    Text(
        text = value,
        color = MaterialTheme.colorScheme.onError,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    )
}

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
                    .size(42.dp)
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
                        modifier = Modifier.size(42.dp),
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