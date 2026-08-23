package com.example.maiplan.home.note.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Title
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.maiplan.R
import com.example.maiplan.database.entities.CategoryEntity
import com.example.maiplan.home.event.screens.EventDatePickerDialog
import com.example.maiplan.home.event.screens.EventTimePickerDialog
import com.example.maiplan.utils.LocalAdaptiveLayout
import com.example.maiplan.utils.common.IconData
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

internal val NotePrimary = Color(0xFF4A6583)
internal val NotePrimaryLight = Color(0xFF7089A5)
internal val NoteTeal = Color(0xFF14B8A6)
internal val NoteInk = Color(0xFF172033)
internal val NoteMuted = Color(0xFF667085)
internal val NoteBorder = Color(0xFFDDE3EC)
internal val NoteDanger = Color(0xFFD92D3A)

internal data class NoteEditorState(
    val title: String,
    val content: String,
    val selectedCategory: CategoryEntity?,
    val categories: List<CategoryEntity>,
    val reminderDateTime: LocalDateTime?,
    val reminderMessage: String,
    val errorMessage: String?,
    val isLoading: Boolean,
)

@Composable
internal fun NoteScreenBackground(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (dark) Color(0xFF101321) else Color(0xFFF4F6FC)),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(
                        NotePrimary.copy(alpha = if (dark) 0.18f else 0.09f),
                        Color.Transparent,
                    ),
                ),
                center = Offset(size.width * 0.05f, size.height * 0.03f),
                radius = size.minDimension * 0.68f,
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(
                        NoteTeal.copy(alpha = if (dark) 0.10f else 0.06f),
                        Color.Transparent,
                    ),
                ),
                center = Offset(size.width, size.height),
                radius = size.minDimension * 0.52f,
            )
        }
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NoteEditorTopBar(title: String, onBackClick: () -> Unit) {
    val dark = isSystemInDarkTheme()
    val foreground = if (dark) Color(0xFFF5F7FB) else NoteInk
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
                    Icons.AutoMirrored.Rounded.ArrowBack,
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
internal fun NoteEditorLayout(
    topBarTitle: String,
    heading: String,
    subtitle: String,
    submitLabel: String,
    state: NoteEditorState,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onCategoryChange: (CategoryEntity?) -> Unit,
    onReminderDateTimeChange: (LocalDateTime) -> Unit,
    onReminderMessageChange: (String) -> Unit,
    onReminderClear: () -> Unit,
    onSubmit: () -> Unit,
    onBackClick: () -> Unit,
) {
    val compactLandscape = LocalAdaptiveLayout.current.let { it.isLandscape && it.isShort }
    NoteScreenBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { NoteEditorTopBar(topBarTitle, onBackClick) },
        ) { innerPadding ->
            if (compactLandscape) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .weight(0.82f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        NoteEditorHeading(heading, subtitle)
                        NotePreview(state, compact = true)
                        Spacer(Modifier.height(4.dp))
                    }
                    Column(
                        modifier = Modifier
                            .weight(1.18f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                    ) {
                        NoteEditorForm(
                            state = state,
                            submitLabel = submitLabel,
                            compact = true,
                            onTitleChange = onTitleChange,
                            onContentChange = onContentChange,
                            onCategoryChange = onCategoryChange,
                            onReminderDateTimeChange = onReminderDateTimeChange,
                            onReminderMessageChange = onReminderMessageChange,
                            onReminderClear = onReminderClear,
                            onSubmit = onSubmit,
                        )
                    }
                }
            } else {
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
                        NoteEditorHeading(heading, subtitle)
                        NotePreview(state)
                        NoteEditorForm(
                            state = state,
                            submitLabel = submitLabel,
                            compact = false,
                            onTitleChange = onTitleChange,
                            onContentChange = onContentChange,
                            onCategoryChange = onCategoryChange,
                            onReminderDateTimeChange = onReminderDateTimeChange,
                            onReminderMessageChange = onReminderMessageChange,
                            onReminderClear = onReminderClear,
                            onSubmit = onSubmit,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteEditorForm(
    state: NoteEditorState,
    submitLabel: String,
    compact: Boolean,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onCategoryChange: (CategoryEntity?) -> Unit,
    onReminderDateTimeChange: (LocalDateTime) -> Unit,
    onReminderMessageChange: (String) -> Unit,
    onReminderClear: () -> Unit,
    onSubmit: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(if (compact) 12.dp else 18.dp),
    ) {
        NoteEditorSection(
            title = stringResource(R.string.note_details_title),
            subtitle = stringResource(R.string.note_details_subtitle),
            compact = compact,
        ) {
            NoteEditorTextField(
                value = state.title,
                onValueChange = { if (it.length <= 255) onTitleChange(it) },
                label = stringResource(R.string.title),
                placeholder = stringResource(R.string.note_title_placeholder),
                icon = Icons.Rounded.Title,
                singleLine = true,
                minLines = 1,
                maxLines = 1,
                imeAction = ImeAction.Next,
            )
            Spacer(Modifier.height(if (compact) 10.dp else 14.dp))
            NoteEditorTextField(
                value = state.content,
                onValueChange = onContentChange,
                label = stringResource(R.string.note_content),
                placeholder = stringResource(R.string.note_body_placeholder),
                icon = Icons.Rounded.Description,
                singleLine = false,
                minLines = if (compact) 3 else 5,
                maxLines = if (compact) 7 else 12,
                imeAction = ImeAction.Default,
            )
        }
        NoteEditorSection(
            title = stringResource(R.string.note_organization_title),
            subtitle = stringResource(R.string.note_organization_subtitle),
            compact = compact,
        ) {
            NoteCategoryDropdown(
                categories = state.categories,
                selectedCategory = state.selectedCategory,
                onCategorySelected = onCategoryChange,
            )
        }
        NoteEditorSection(
            title = stringResource(R.string.note_reminder_title),
            subtitle = stringResource(R.string.note_reminder_subtitle),
            compact = compact,
        ) {
            NoteReminderDateTimeSelector(
                value = state.reminderDateTime,
                onValueChange = onReminderDateTimeChange,
                onClear = onReminderClear,
            )
            if (state.reminderDateTime != null) {
                Spacer(Modifier.height(if (compact) 10.dp else 14.dp))
                NoteEditorTextField(
                    value = state.reminderMessage,
                    onValueChange = {
                        if (it.length <= 512) onReminderMessageChange(it)
                    },
                    label = stringResource(R.string.note_reminder_message),
                    placeholder = stringResource(R.string.note_reminder_message),
                    icon = Icons.AutoMirrored.Rounded.Message,
                    singleLine = false,
                    minLines = if (compact) 1 else 2,
                    maxLines = 4,
                    imeAction = ImeAction.Done,
                )
            }
        }
        state.errorMessage?.let { NoteEditorError(it) }
        NoteEditorButton(
            text = submitLabel,
            isLoading = state.isLoading,
            onClick = onSubmit,
        )
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun NoteEditorHeading(heading: String, subtitle: String) {
    val dark = isSystemInDarkTheme()
    Column {
        Text(
            text = heading,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = if (dark) Color(0xFFF5F7FB) else NoteInk,
            letterSpacing = (-0.5).sp,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = if (dark) Color(0xFFAEB7C9) else NoteMuted,
            lineHeight = 21.sp,
        )
    }
}

@Composable
private fun NotePreview(state: NoteEditorState, compact: Boolean = false) {
    val dark = isSystemInDarkTheme()
    val surface = if (dark) Color(0xFF191D2E) else Color.White
    val foreground = if (dark) Color(0xFFF5F7FB) else NoteInk
    val muted = if (dark) Color(0xFFAEB7C9) else NoteMuted
    val categoryColor = state.selectedCategory?.color?.toULongOrNull()?.let(::Color) ?: NotePrimary
    val categoryIcon = state.selectedCategory?.let { IconData.getIconByKey(it.icon) }
        ?: Icons.AutoMirrored.Rounded.Notes

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = surface,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, if (dark) Color.White.copy(alpha = 0.08f) else NoteBorder),
        shadowElevation = if (dark) 0.dp else 2.dp,
    ) {
        Column(Modifier.padding(if (compact) 14.dp else 18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    color = categoryColor.copy(alpha = if (dark) 0.27f else 0.13f),
                    shape = RoundedCornerShape(13.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            categoryIcon,
                            contentDescription = null,
                            tint = if (categoryColor.luminance() > 0.82f) NotePrimary else categoryColor,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = state.title.ifBlank { stringResource(R.string.note_preview_title) },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = foreground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = state.selectedCategory?.name ?: stringResource(R.string.note_no_category),
                        style = MaterialTheme.typography.labelMedium,
                        color = categoryColor,
                    )
                }
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = NoteTeal.copy(alpha = if (dark) 0.18f else 0.10f),
                ) {
                    Text(
                        text = stringResource(R.string.note_preview_label),
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = NoteTeal,
                    )
                }
                if (state.reminderDateTime != null) {
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        Icons.Rounded.Notifications,
                        contentDescription = stringResource(R.string.note_reminder_set),
                        tint = NoteTeal,
                        modifier = Modifier.size(19.dp),
                    )
                }
            }
            Spacer(Modifier.height(if (compact) 10.dp else 14.dp))
            Text(
                text = state.content.ifBlank { stringResource(R.string.note_preview_body) },
                style = MaterialTheme.typography.bodyMedium,
                color = if (state.content.isBlank()) muted else foreground.copy(alpha = 0.82f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 21.sp,
            )
        }
    }
}

@Composable
private fun NoteEditorSection(
    title: String,
    subtitle: String,
    compact: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val dark = isSystemInDarkTheme()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (dark) Color(0xFF191D2E) else Color.White,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, if (dark) Color.White.copy(alpha = 0.08f) else NoteBorder),
        shadowElevation = if (dark) 0.dp else 2.dp,
    ) {
        Column(Modifier.padding(if (compact) 14.dp else 18.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (dark) Color(0xFFF5F7FB) else NoteInk,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (dark) Color(0xFFAEB7C9) else NoteMuted,
            )
            Spacer(Modifier.height(if (compact) 12.dp else 16.dp))
            content()
        }
    }
}

@Composable
private fun NoteEditorTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector,
    singleLine: Boolean,
    minLines: Int,
    maxLines: Int,
    imeAction: ImeAction,
) {
    val dark = isSystemInDarkTheme()
    val field = if (dark) Color(0xFF20263A) else Color(0xFFF8FAFC)
    val foreground = if (dark) Color(0xFFF5F7FB) else NoteInk
    val muted = if (dark) Color(0xFFAEB7C9) else NoteMuted
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(icon, contentDescription = null, modifier = Modifier.size(21.dp))
        },
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        shape = RoundedCornerShape(15.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = imeAction,
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = field,
            unfocusedContainerColor = field,
            focusedBorderColor = NotePrimary,
            unfocusedBorderColor = if (dark) Color(0xFF3A435C) else NoteBorder,
            focusedLabelColor = NotePrimary,
            unfocusedLabelColor = muted,
            focusedLeadingIconColor = NotePrimary,
            unfocusedLeadingIconColor = muted,
            focusedTextColor = foreground,
            unfocusedTextColor = foreground,
            cursorColor = NotePrimary,
            focusedPlaceholderColor = muted,
            unfocusedPlaceholderColor = muted,
        ),
    )
}

@Composable
private fun NoteReminderDateTimeSelector(
    value: LocalDateTime?,
    onValueChange: (LocalDateTime) -> Unit,
    onClear: () -> Unit,
) {
    val dark = isSystemInDarkTheme()
    val field = if (dark) Color(0xFF20263A) else Color(0xFFF8FAFC)
    val foreground = if (dark) Color(0xFFF5F7FB) else NoteInk
    val muted = if (dark) Color(0xFFAEB7C9) else NoteMuted
    var showDateDialog by remember { mutableStateOf(false) }
    var showTimeDialog by remember { mutableStateOf(false) }
    var pendingDate by remember(value) {
        mutableStateOf(value?.toLocalDate() ?: LocalDate.now())
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = { showDateDialog = true },
        shape = RoundedCornerShape(17.dp),
        color = field,
        border = BorderStroke(1.dp, if (dark) Color(0xFF3A435C) else NoteBorder),
    ) {
        Row(
            modifier = Modifier.padding(11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(13.dp),
                color = NotePrimary.copy(alpha = if (dark) 0.24f else 0.10f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Rounded.Notifications,
                        contentDescription = null,
                        tint = NotePrimaryLight,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.date_time),
                    style = MaterialTheme.typography.labelSmall,
                    color = muted,
                )
                Text(
                    text = value?.format(DateTimeFormatter.ofPattern("EEE, MMM d · HH:mm"))
                        ?: stringResource(R.string.note_reminder_optional),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = foreground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (value != null) {
                IconButton(onClick = onClear, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.note_reminder_remove),
                        tint = NoteDanger,
                        modifier = Modifier.size(19.dp),
                    )
                }
            } else {
                Icon(
                    Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = NotePrimaryLight,
                    modifier = Modifier.size(21.dp),
                )
            }
        }
    }

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteCategoryDropdown(
    categories: List<CategoryEntity>,
    selectedCategory: CategoryEntity?,
    onCategorySelected: (CategoryEntity?) -> Unit,
) {
    val dark = isSystemInDarkTheme()
    val field = if (dark) Color(0xFF20263A) else Color(0xFFF8FAFC)
    val foreground = if (dark) Color(0xFFF5F7FB) else NoteInk
    val muted = if (dark) Color(0xFFAEB7C9) else NoteMuted
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            value = selectedCategory?.name ?: stringResource(R.string.note_no_category),
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
            readOnly = true,
            singleLine = true,
            label = { Text(stringResource(R.string.category)) },
            leadingIcon = {
                Icon(
                    selectedCategory?.let { IconData.getIconByKey(it.icon) } ?: Icons.Rounded.Category,
                    contentDescription = null,
                    modifier = Modifier.size(21.dp),
                )
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            shape = RoundedCornerShape(15.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = field,
                unfocusedContainerColor = field,
                focusedBorderColor = NotePrimary,
                unfocusedBorderColor = if (dark) Color(0xFF3A435C) else NoteBorder,
                focusedTextColor = foreground,
                unfocusedTextColor = foreground,
                focusedLabelColor = NotePrimary,
                unfocusedLabelColor = muted,
                focusedLeadingIconColor = NotePrimary,
                unfocusedLeadingIconColor = NotePrimaryLight,
                focusedTrailingIconColor = muted,
                unfocusedTrailingIconColor = muted,
            ),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(if (dark) Color(0xFF20263A) else Color.White),
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.note_no_category), color = foreground) },
                leadingIcon = {
                    Icon(Icons.Rounded.Category, contentDescription = null, tint = NotePrimaryLight)
                },
                onClick = {
                    onCategorySelected(null)
                    expanded = false
                },
            )
            categories.forEach { category ->
                val color = category.color.toULongOrNull()?.let(::Color) ?: NotePrimary
                DropdownMenuItem(
                    text = { Text(category.name, color = foreground) },
                    leadingIcon = {
                        Surface(
                            modifier = Modifier.size(34.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = color.copy(alpha = if (dark) 0.24f else 0.14f),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    IconData.getIconByKey(category.icon),
                                    contentDescription = null,
                                    tint = if (color.luminance() > 0.82f) NotePrimary else color,
                                    modifier = Modifier.size(19.dp),
                                )
                            }
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

@Composable
private fun NoteEditorError(message: String) {
    val dark = isSystemInDarkTheme()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (dark) Color(0xFF3B2027) else Color(0xFFFFF1F2),
        shape = RoundedCornerShape(13.dp),
        border = BorderStroke(1.dp, NoteDanger.copy(alpha = 0.24f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Rounded.ErrorOutline,
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
private fun NoteEditorButton(
    text: String,
    isLoading: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(15.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = NotePrimary,
            contentColor = Color.White,
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(21.dp),
                color = Color.White,
                strokeWidth = 2.dp,
            )
        } else {
            Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(9.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
