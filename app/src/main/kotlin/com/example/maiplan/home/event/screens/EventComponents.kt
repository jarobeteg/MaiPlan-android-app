package com.example.maiplan.home.event.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Message
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Title
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.maiplan.R
import com.example.maiplan.database.entities.CategoryEntity
import com.example.maiplan.utils.common.IconData
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

internal val EventPrimary = Color(0xFF4A6583)
internal val EventPrimaryLight = Color(0xFF7089A5)
internal val EventTeal = Color(0xFF14B8A6)
internal val EventInk = Color(0xFF172033)
internal val EventMuted = Color(0xFF667085)
internal val EventBorder = Color(0xFFDDE3EC)
internal val EventDanger = Color(0xFFD92D3A)

internal data class EventEditorState(
    val title: String,
    val description: String,
    val date: LocalDate?,
    val startTime: LocalTime?,
    val endTime: LocalTime?,
    val selectedCategory: CategoryEntity?,
    val categories: List<CategoryEntity>,
    val reminderDateTime: LocalDateTime?,
    val reminderMessage: String,
    val errorMessage: String?,
)

@Composable
internal fun EventScreenBackground(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val background = if (dark) Color(0xFF101321) else Color(0xFFF4F6FC)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(
                        EventPrimary.copy(alpha = if (dark) 0.18f else 0.09f),
                        Color.Transparent,
                    ),
                ),
                center = Offset(size.width * 0.06f, size.height * 0.02f),
                radius = size.minDimension * 0.68f,
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(
                        EventTeal.copy(alpha = if (dark) 0.10f else 0.06f),
                        Color.Transparent,
                    ),
                ),
                center = Offset(size.width, size.height),
                radius = size.minDimension * 0.50f,
            )
        }
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EventEditorTopBar(title: String, onBackClick: () -> Unit) {
    val dark = isSystemInDarkTheme()
    val foreground = if (dark) Color(0xFFF5F7FB) else EventInk
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = foreground,
                letterSpacing = (-0.3).sp,
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = foreground,
                )
            }
        },
        actions = { Spacer(Modifier.size(48.dp)) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = (if (dark) Color(0xFF191D2E) else Color.White).copy(alpha = 0.94f),
        ),
    )
}

@Composable
internal fun EventEditorLayout(
    topBarTitle: String,
    heading: String,
    subtitle: String,
    state: EventEditorState,
    submitLabel: String,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onStartTimeChange: (LocalTime) -> Unit,
    onEndTimeChange: (LocalTime) -> Unit,
    onCategoryChange: (CategoryEntity) -> Unit,
    onReminderDateTimeChange: (LocalDateTime) -> Unit,
    onReminderMessageChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBackClick: () -> Unit,
) {
    EventScreenBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { EventEditorTopBar(topBarTitle, onBackClick) },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 680.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    EventEditorHeading(heading, subtitle)
                    EventPreview(state)

                    EventEditorSection(
                        title = stringResource(R.string.event_details_title),
                        subtitle = stringResource(R.string.event_details_subtitle),
                    ) {
                        EventEditorTextField(
                            value = state.title,
                            onValueChange = { if (it.length <= 255) onTitleChange(it) },
                            label = stringResource(R.string.title),
                            icon = Icons.Rounded.Title,
                            singleLine = true,
                            imeAction = ImeAction.Next,
                        )
                        Spacer(Modifier.height(14.dp))
                        EventEditorTextField(
                            value = state.description,
                            onValueChange = { if (it.length <= 512) onDescriptionChange(it) },
                            label = stringResource(R.string.description),
                            icon = Icons.Rounded.Description,
                            singleLine = false,
                            imeAction = ImeAction.Default,
                        )
                    }

                    EventEditorSection(
                        title = stringResource(R.string.event_schedule_title),
                        subtitle = stringResource(R.string.event_schedule_subtitle),
                    ) {
                        EventDateSelector(state.date, onDateChange)
                        Spacer(Modifier.height(12.dp))
                        BoxWithConstraints {
                            if (maxWidth >= 420.dp) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Box(Modifier.weight(1f)) {
                                        EventTimeSelector(
                                            label = stringResource(R.string.start_time),
                                            value = state.startTime,
                                            onValueChange = onStartTimeChange,
                                        )
                                    }
                                    Box(Modifier.weight(1f)) {
                                        EventTimeSelector(
                                            label = stringResource(R.string.end_time),
                                            value = state.endTime,
                                            onValueChange = onEndTimeChange,
                                        )
                                    }
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    EventTimeSelector(
                                        label = stringResource(R.string.start_time),
                                        value = state.startTime,
                                        onValueChange = onStartTimeChange,
                                    )
                                    EventTimeSelector(
                                        label = stringResource(R.string.end_time),
                                        value = state.endTime,
                                        onValueChange = onEndTimeChange,
                                    )
                                }
                            }
                        }
                    }

                    EventEditorSection(
                        title = stringResource(R.string.event_organization_title),
                        subtitle = stringResource(R.string.event_organization_subtitle),
                    ) {
                        EventCategoryDropdown(
                            categories = state.categories,
                            selectedCategory = state.selectedCategory,
                            onCategorySelected = onCategoryChange,
                        )
                    }

                    EventEditorSection(
                        title = stringResource(R.string.event_reminder_title),
                        subtitle = stringResource(R.string.event_reminder_subtitle),
                    ) {
                        EventReminderDateTimeSelector(
                            value = state.reminderDateTime,
                            onValueChange = onReminderDateTimeChange,
                        )
                        Spacer(Modifier.height(14.dp))
                        EventEditorTextField(
                            value = state.reminderMessage,
                            onValueChange = {
                                if (it.length <= 512) onReminderMessageChange(it)
                            },
                            label = stringResource(R.string.message),
                            icon = Icons.AutoMirrored.Rounded.Message,
                            singleLine = false,
                            imeAction = ImeAction.Done,
                        )
                    }

                    if (state.errorMessage != null) {
                        EventEditorError(state.errorMessage)
                    }
                    EventEditorButton(text = submitLabel, onClick = onSubmit)
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun EventEditorHeading(heading: String, subtitle: String) {
    val dark = isSystemInDarkTheme()
    Column {
        Text(
            text = heading,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = if (dark) Color(0xFFF5F7FB) else EventInk,
            letterSpacing = (-0.5).sp,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = if (dark) Color(0xFFAEB7C9) else EventMuted,
            lineHeight = 21.sp,
        )
    }
}

@Composable
private fun EventPreview(state: EventEditorState) {
    val dark = isSystemInDarkTheme()
    val surface = if (dark) Color(0xFF191D2E) else Color.White
    val foreground = if (dark) Color(0xFFF5F7FB) else EventInk
    val muted = if (dark) Color(0xFFAEB7C9) else EventMuted
    val categoryColor = state.selectedCategory?.let { Color(it.color.toULong()) } ?: EventPrimary
    val icon = state.selectedCategory?.let { IconData.getIconByKey(it.icon) } ?: Icons.Rounded.Event
    val iconTint = if (categoryColor.luminance() > 0.68f) EventInk else Color.White
    val dateText = state.date?.format(DateTimeFormatter.ofPattern("EEE, MMM d"))
        ?: stringResource(R.string.event_preview_date)
    val timeText = if (state.startTime != null && state.endTime != null) {
        "${state.startTime.format(DateTimeFormatter.ofPattern("HH:mm"))} – ${state.endTime.format(DateTimeFormatter.ofPattern("HH:mm"))}"
    } else {
        stringResource(R.string.event_preview_time)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = surface,
        shadowElevation = if (dark) 0.dp else 4.dp,
        border = BorderStroke(1.dp, if (dark) Color(0xFF30374D) else EventBorder),
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(categoryColor, categoryColor.copy(alpha = 0.72f)),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(30.dp),
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = state.title.ifBlank { stringResource(R.string.event_preview_title) },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = foreground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.bodySmall,
                    color = muted,
                )
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.bodySmall,
                    color = muted,
                )
            }
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = EventTeal.copy(alpha = if (dark) 0.18f else 0.10f),
            ) {
                Text(
                    text = stringResource(R.string.category_live_preview),
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = EventTeal,
                )
            }
        }
    }
}

@Composable
private fun EventEditorSection(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    val dark = isSystemInDarkTheme()
    val surface = if (dark) Color(0xFF191D2E) else Color.White
    val foreground = if (dark) Color(0xFFF5F7FB) else EventInk
    val muted = if (dark) Color(0xFFAEB7C9) else EventMuted
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = surface,
        border = BorderStroke(1.dp, if (dark) Color(0xFF30374D) else EventBorder),
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = foreground,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = muted,
            )
            Spacer(Modifier.height(18.dp))
            content()
        }
    }
}

@Composable
private fun EventEditorTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    singleLine: Boolean,
    imeAction: ImeAction,
) {
    val dark = isSystemInDarkTheme()
    val field = if (dark) Color(0xFF20263A) else Color(0xFFF8FAFC)
    val foreground = if (dark) Color(0xFFF5F7FB) else EventInk
    val muted = if (dark) Color(0xFFAEB7C9) else EventMuted
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = if (singleLine) 56.dp else 108.dp),
        label = { Text(label) },
        leadingIcon = {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(21.dp))
        },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        shape = RoundedCornerShape(15.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = imeAction,
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = field,
            unfocusedContainerColor = field,
            focusedBorderColor = EventPrimary,
            unfocusedBorderColor = if (dark) Color(0xFF3A435C) else EventBorder,
            focusedLabelColor = EventPrimary,
            unfocusedLabelColor = muted,
            focusedLeadingIconColor = EventPrimary,
            unfocusedLeadingIconColor = muted,
            focusedTextColor = foreground,
            unfocusedTextColor = foreground,
            cursorColor = EventPrimary,
        ),
    )
}

@Composable
private fun EventDateSelector(value: LocalDate?, onValueChange: (LocalDate) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    EventSelectionField(
        label = stringResource(R.string.date),
        value = value?.format(DateTimeFormatter.ofPattern("EEE, MMM d, yyyy"))
            ?: stringResource(R.string.event_select_date),
        icon = Icons.Rounded.CalendarMonth,
        onClick = { showDialog = true },
    )
    if (showDialog) {
        EventDatePickerDialog(
            onDateSelected = onValueChange,
            onDismiss = { showDialog = false },
        )
    }
}

@Composable
private fun EventTimeSelector(
    label: String,
    value: LocalTime?,
    onValueChange: (LocalTime) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }
    EventSelectionField(
        label = label,
        value = value?.format(DateTimeFormatter.ofPattern("HH:mm"))
            ?: stringResource(R.string.event_select_time),
        icon = Icons.Rounded.AccessTime,
        onClick = { showDialog = true },
    )
    if (showDialog) {
        EventTimePickerDialog(
            onTimeSelected = onValueChange,
            onDismiss = { showDialog = false },
        )
    }
}

@Composable
private fun EventReminderDateTimeSelector(
    value: LocalDateTime?,
    onValueChange: (LocalDateTime) -> Unit,
) {
    var showDateDialog by remember { mutableStateOf(false) }
    var showTimeDialog by remember { mutableStateOf(false) }
    var pendingDate by remember(value) { mutableStateOf(value?.toLocalDate() ?: LocalDate.now()) }
    EventSelectionField(
        label = stringResource(R.string.date_time),
        value = value?.format(DateTimeFormatter.ofPattern("EEE, MMM d · HH:mm"))
            ?: stringResource(R.string.event_reminder_optional),
        icon = Icons.Rounded.Notifications,
        onClick = { showDateDialog = true },
    )
    if (showDateDialog) {
        EventDatePickerDialog(
            onDateSelected = {
                pendingDate = it
                showDateDialog = false
                showTimeDialog = true
            },
            onDismiss = { showDateDialog = false },
        )
    }
    if (showTimeDialog) {
        EventTimePickerDialog(
            onTimeSelected = {
                onValueChange(LocalDateTime.of(pendingDate, it))
                showTimeDialog = false
            },
            onDismiss = { showTimeDialog = false },
        )
    }
}

@Composable
private fun EventSelectionField(
    label: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    val dark = isSystemInDarkTheme()
    val field = if (dark) Color(0xFF20263A) else Color(0xFFF8FAFC)
    val foreground = if (dark) Color(0xFFF5F7FB) else EventInk
    val muted = if (dark) Color(0xFFAEB7C9) else EventMuted
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(17.dp),
        color = field,
        border = BorderStroke(1.dp, if (dark) Color(0xFF3A435C) else EventBorder),
    ) {
        Row(
            modifier = Modifier.padding(11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(13.dp),
                color = EventPrimary.copy(alpha = if (dark) 0.24f else 0.10f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = EventPrimaryLight,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = muted,
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = foreground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(10.dp))
            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = if (dark) Color.White.copy(alpha = 0.06f) else Color.White,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = EventPrimaryLight,
                        modifier = Modifier.size(19.dp),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventCategoryDropdown(
    categories: List<CategoryEntity>,
    selectedCategory: CategoryEntity?,
    onCategorySelected: (CategoryEntity) -> Unit,
) {
    val dark = isSystemInDarkTheme()
    val field = if (dark) Color(0xFF20263A) else Color(0xFFF8FAFC)
    val foreground = if (dark) Color(0xFFF5F7FB) else EventInk
    val muted = if (dark) Color(0xFFAEB7C9) else EventMuted
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            value = selectedCategory?.name.orEmpty(),
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
            readOnly = true,
            singleLine = true,
            label = { Text(stringResource(R.string.category)) },
            leadingIcon = {
                Icon(
                    imageVector = selectedCategory?.let { IconData.getIconByKey(it.icon) }
                        ?: Icons.Rounded.Category,
                    contentDescription = null,
                    modifier = Modifier.size(21.dp),
                )
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RoundedCornerShape(15.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = field,
                unfocusedContainerColor = field,
                focusedBorderColor = EventPrimary,
                unfocusedBorderColor = if (dark) Color(0xFF3A435C) else EventBorder,
                focusedTextColor = foreground,
                unfocusedTextColor = foreground,
                focusedLabelColor = EventPrimary,
                unfocusedLabelColor = muted,
                focusedLeadingIconColor = EventPrimary,
                unfocusedLeadingIconColor = EventPrimary,
                focusedTrailingIconColor = muted,
                unfocusedTrailingIconColor = muted,
            ),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(if (dark) Color(0xFF20263A) else Color.White),
        ) {
            if (categories.isEmpty()) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.event_no_categories), color = muted) },
                    onClick = {},
                    enabled = false,
                )
            } else {
                categories.forEach { category ->
                    val categoryColor = Color(category.color.toULong())
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier.size(34.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    color = categoryColor.copy(alpha = if (dark) 0.24f else 0.14f),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = IconData.getIconByKey(category.icon),
                                            contentDescription = null,
                                            tint = if (categoryColor.luminance() > 0.82f) EventPrimary else categoryColor,
                                            modifier = Modifier.size(19.dp),
                                        )
                                    }
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = category.name,
                                    color = foreground,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        },
                        onClick = {
                            onCategorySelected(category)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun EventEditorError(message: String) {
    val dark = isSystemInDarkTheme()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(13.dp),
        color = if (dark) Color(0xFF3B2027) else Color(0xFFFFF1F2),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.ErrorOutline,
                contentDescription = null,
                tint = Color(0xFFE5484D),
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = if (dark) Color(0xFFFFB4B8) else Color(0xFFA61B29),
            )
        }
    }
}

@Composable
private fun EventEditorButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(15.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = EventPrimary,
            contentColor = Color.White,
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
    ) {
        Icon(
            imageVector = Icons.Rounded.Check,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(9.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EventDatePickerDialog(
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberDatePickerState()
    val dark = isSystemInDarkTheme()
    val surface = if (dark) Color(0xFF191D2E) else Color.White
    val foreground = if (dark) Color(0xFFF5F7FB) else EventInk
    val muted = if (dark) Color(0xFFAEB7C9) else EventMuted

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        EventPickerDialogSurface(onDismiss = onDismiss) {
            DatePicker(
                state = state,
                title = {
                    EventPickerHeader(
                        title = stringResource(R.string.picker_date_title),
                        subtitle = stringResource(R.string.picker_date_subtitle),
                        icon = Icons.Rounded.CalendarMonth,
                    )
                },
                showModeToggle = false,
                colors = DatePickerDefaults.colors(
                    containerColor = surface,
                    titleContentColor = foreground,
                    headlineContentColor = foreground,
                    weekdayContentColor = EventPrimaryLight,
                    subheadContentColor = foreground,
                    yearContentColor = foreground,
                    selectedDayContentColor = Color.White,
                    selectedDayContainerColor = EventPrimary,
                    selectedYearContentColor = Color.White,
                    selectedYearContainerColor = EventPrimary,
                    todayDateBorderColor = EventPrimary,
                    todayContentColor = EventPrimaryLight,
                    currentYearContentColor = EventPrimaryLight,
                    dayContentColor = foreground,
                    disabledDayContentColor = muted.copy(alpha = 0.45f),
                    dayInSelectionRangeContentColor = foreground,
                ),
            )
            EventPickerActions(
                onDismiss = onDismiss,
                onConfirm = {
                    state.selectedDateMillis?.let { millis ->
                        onDateSelected(
                            Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate(),
                        )
                    }
                    onDismiss()
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EventTimePickerDialog(
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberTimePickerState(is24Hour = true)
    val dark = isSystemInDarkTheme()
    val field = if (dark) Color(0xFF252B40) else Color(0xFFF2F5F9)
    val foreground = if (dark) Color(0xFFF5F7FB) else EventInk
    val muted = if (dark) Color(0xFFAEB7C9) else EventMuted

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        EventPickerDialogSurface(onDismiss = onDismiss) {
            EventPickerHeader(
                title = stringResource(R.string.picker_time_title),
                subtitle = stringResource(R.string.picker_time_subtitle),
                icon = Icons.Rounded.AccessTime,
            )
            TimePicker(
                state = state,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                colors = TimePickerDefaults.colors(
                    clockDialColor = field,
                    clockDialSelectedContentColor = Color.White,
                    clockDialUnselectedContentColor = foreground,
                    selectorColor = EventPrimary,
                    containerColor = Color.Transparent,
                    periodSelectorBorderColor = if (dark) Color(0xFF3A435C) else EventBorder,
                    periodSelectorSelectedContainerColor = EventPrimary,
                    periodSelectorUnselectedContainerColor = field,
                    periodSelectorSelectedContentColor = Color.White,
                    periodSelectorUnselectedContentColor = muted,
                    timeSelectorSelectedContainerColor = EventPrimary,
                    timeSelectorUnselectedContainerColor = field,
                    timeSelectorSelectedContentColor = Color.White,
                    timeSelectorUnselectedContentColor = foreground,
                ),
            )
            Spacer(Modifier.height(18.dp))
            EventPickerActions(
                onDismiss = onDismiss,
                onConfirm = {
                    onTimeSelected(LocalTime.of(state.hour, state.minute))
                    onDismiss()
                },
            )
        }
    }
}

@Composable
private fun EventPickerDialogSurface(
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val dark = isSystemInDarkTheme()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) { onDismiss() },
        )
        Surface(
            modifier = Modifier
                .widthIn(max = 380.dp)
                .fillMaxWidth()
                .heightIn(max = 620.dp),
            shape = RoundedCornerShape(26.dp),
            color = if (dark) Color(0xFF191D2E) else Color.White,
            border = BorderStroke(
                1.dp,
                if (dark) Color.White.copy(alpha = 0.09f) else EventBorder,
            ),
            shadowElevation = 18.dp,
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 10.dp),
                content = content,
            )
        }
    }
}

@Composable
private fun EventPickerHeader(
    title: String,
    subtitle: String,
    icon: ImageVector,
) {
    val dark = isSystemInDarkTheme()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 22.dp, end = 22.dp, top = 22.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(46.dp),
            shape = RoundedCornerShape(14.dp),
            color = EventPrimary.copy(alpha = if (dark) 0.25f else 0.10f),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = EventPrimaryLight,
                    modifier = Modifier.size(23.dp),
                )
            }
        }
        Spacer(Modifier.width(13.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (dark) Color(0xFFF5F7FB) else EventInk,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (dark) Color(0xFFAEB7C9) else EventMuted,
            )
        }
    }
}

@Composable
private fun EventPickerActions(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val dark = isSystemInDarkTheme()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
    ) {
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.height(44.dp),
            colors = ButtonDefaults.textButtonColors(
                containerColor = if (dark) Color(0xFF252B40) else Color(0xFFF2F5F9),
                contentColor = if (dark) Color(0xFFF5F7FB) else EventInk,
            ),
        ) {
            Text(stringResource(R.string.cancel), fontWeight = FontWeight.SemiBold)
        }
        TextButton(
            onClick = onConfirm,
            modifier = Modifier.height(44.dp),
            colors = ButtonDefaults.textButtonColors(
                containerColor = EventPrimary,
                contentColor = Color.White,
            ),
        ) {
            Text(stringResource(R.string.select), fontWeight = FontWeight.SemiBold)
        }
    }
}
