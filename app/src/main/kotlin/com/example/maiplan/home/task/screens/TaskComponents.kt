package com.example.maiplan.home.task.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.example.maiplan.R
import com.example.maiplan.database.entities.CategoryEntity
import com.example.maiplan.home.event.screens.EventSelectionField
import com.example.maiplan.repository.task.*
import com.example.maiplan.theme.LocalAppDarkTheme
import com.example.maiplan.theme.AppThemeManager
import com.example.maiplan.utils.common.IconData
import com.example.maiplan.viewmodel.task.TaskView
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun TaskFeatureTheme(content: @Composable () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val dark = LocalAppDarkTheme.current
    val isTablet = LocalConfiguration.current.smallestScreenWidthDp >= 600
    val surface = if (dark) Color(0xFF191D2E) else Color.White
    val field = if (dark) Color(0xFF20263A) else Color(0xFFF8FAFC)
    val ink = if (dark) Color(0xFFF5F7FB) else Color(0xFF172033)
    val muted = if (dark) Color(0xFFAEB7C9) else Color(0xFF667085)
    val primary = if (dark) AppThemeManager.selectedTheme.primaryLight else colors.primary
    val tint = primary.copy(alpha = 0.12f).compositeOver(surface)
    MaterialTheme(
        colorScheme = colors.copy(
            primary = primary, onPrimary = Color.White,
            background = if (dark) Color(0xFF101321) else Color(0xFFF4F6FC), onBackground = ink,
            surface = surface, onSurface = ink, surfaceVariant = field, onSurfaceVariant = muted,
            surfaceContainerLowest = surface, surfaceContainerLow = surface,
            surfaceContainer = surface, surfaceContainerHigh = field, surfaceContainerHighest = field,
            secondaryContainer = tint, onSecondaryContainer = ink,
            primaryContainer = tint, onPrimaryContainer = ink,
            outline = if (dark) Color(0xFF3A435C) else Color(0xFFDDE3EC),
            outlineVariant = if (dark) Color.White.copy(alpha = 0.08f) else Color(0xFFDDE3EC),
            onError = Color.White,
        ),
        shapes = MaterialTheme.shapes.copy(medium = RoundedCornerShape(15.dp), large = RoundedCornerShape(22.dp)),
    ) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            if (!isTablet) {
                Canvas(Modifier.fillMaxSize()) {
                    drawCircle(
                        brush = Brush.radialGradient(listOf(AppThemeManager.selectedTheme.primary.copy(alpha = if (dark) 0.18f else 0.09f), Color.Transparent)),
                        center = Offset(size.width * 0.05f, size.height * 0.03f), radius = size.minDimension * 0.68f,
                    )
                    drawCircle(
                        brush = Brush.radialGradient(listOf(Color(0xFF14B8A6).copy(alpha = if (dark) 0.10f else 0.06f), Color.Transparent)),
                        center = Offset(size.width, size.height), radius = size.minDimension * 0.52f,
                    )
                }
            }
            CompositionLocalProvider(LocalContentColor provides ink) {
                content()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    subtitle: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    CenterAlignedTopAppBar(title = {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
                letterSpacing = (-0.3).sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            subtitle?.let {
                Text(it, style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }, navigationIcon = {
        if (onBack != null) IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.task_back))
        }
    }, actions = actions, colors = TopAppBarDefaults.topAppBarColors(
        containerColor = Color.Transparent,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
        actionIconContentColor = MaterialTheme.colorScheme.onSurface,
    ))
}

@Composable
internal fun TaskPanel(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = if (LocalAppDarkTheme.current) 0.dp else 2.dp, content = content)
}

@Composable
internal fun TaskSection(title: String, subtitle: String? = null, icon: ImageVector? = null,
    content: @Composable ColumnScope.() -> Unit) {
    TaskPanel {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (icon != null) TaskSectionIcon(icon)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    subtitle?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
            content()
        }
    }
}

@Composable
internal fun TaskSectionIcon(icon: ImageVector, color: Color = MaterialTheme.colorScheme.primary) {
    Surface(shape = RoundedCornerShape(12.dp), color = color.copy(alpha = 0.12f)) {
        Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
internal fun TaskHeading(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp)
    }
}

@Composable
internal fun TaskTextField(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier,
    enabled: Boolean = true, readOnly: Boolean = false, singleLine: Boolean = false,
    minLines: Int = 1, maxLines: Int = Int.MAX_VALUE, keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    label: @Composable (() -> Unit)? = null, supportingText: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null, trailingIcon: @Composable (() -> Unit)? = null) {
    val colors = MaterialTheme.colorScheme
    OutlinedTextField(value, onValueChange, modifier = modifier, enabled = enabled, readOnly = readOnly,
        singleLine = singleLine, minLines = minLines, maxLines = maxLines, keyboardOptions = keyboardOptions,
        label = label, supportingText = supportingText, leadingIcon = leadingIcon, trailingIcon = trailingIcon,
        shape = RoundedCornerShape(15.dp), colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = colors.surfaceVariant, unfocusedContainerColor = colors.surfaceVariant,
            disabledContainerColor = colors.surfaceVariant, focusedBorderColor = colors.primary,
            unfocusedBorderColor = colors.outline, focusedLabelColor = colors.primary,
            unfocusedLabelColor = colors.onSurfaceVariant, focusedTextColor = colors.onSurface,
            unfocusedTextColor = colors.onSurface, cursorColor = colors.primary,
            focusedLeadingIconColor = colors.primary, unfocusedLeadingIconColor = colors.onSurfaceVariant,
        ))
}

@Composable
internal fun TaskFilterChip(selected: Boolean, onClick: () -> Unit, enabled: Boolean = true,
    label: @Composable () -> Unit) {
    FilterChip(selected = selected, onClick = onClick, enabled = enabled, label = label,
        shape = RoundedCornerShape(12.dp), colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedLabelColor = MaterialTheme.colorScheme.primary,
        ))
}

@Composable
internal fun TaskCategoryPicker(categories: List<CategoryEntity>, selectedId: Long?, onClick: () -> Unit) {
    val selected = categories.find { it.categoryLocalId == selectedId }
    val label = when {
        selectedId == null -> stringResource(R.string.task_all_categories)
        selected != null -> selected.name
        else -> stringResource(R.string.uncategorized)
    }
    EventSelectionField(label = stringResource(R.string.category), value = label,
        icon = selected?.let { IconData.getIconByKey(it.icon) } ?: Icons.Rounded.Category,
        onClick = onClick)
}

@Composable
internal fun TaskStatusBadge(status: TaskStatus) {
    val color = taskStatusColor(status)
    Surface(shape = RoundedCornerShape(10.dp), color = color.copy(alpha = 0.12f)) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(taskStatusIcon(status), null, tint = color, modifier = Modifier.size(16.dp))
            Text(taskStatusLabel(status), style = MaterialTheme.typography.labelMedium, color = color)
        }
    }
}

@Composable
internal fun taskStatusColor(status: TaskStatus): Color = when (status) {
    TaskStatus.DONE -> if (LocalAppDarkTheme.current) Color(0xFF5ED8C6) else Color(0xFF087F72)
    TaskStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}

internal fun taskStatusIcon(status: TaskStatus): ImageVector = when (status) {
    TaskStatus.TODO -> Icons.Rounded.RadioButtonUnchecked
    TaskStatus.IN_PROGRESS -> Icons.Rounded.PendingActions
    TaskStatus.DONE -> Icons.Rounded.CheckCircle
    TaskStatus.SKIPPED -> Icons.Rounded.SkipNext
    TaskStatus.CANCELLED -> Icons.Rounded.Block
}

@Composable
fun taskStatusLabel(status: TaskStatus): String = stringResource(when (status) {
    TaskStatus.TODO -> R.string.task_todo
    TaskStatus.IN_PROGRESS -> R.string.task_in_progress
    TaskStatus.DONE -> R.string.task_done
    TaskStatus.SKIPPED -> R.string.task_skipped
    TaskStatus.CANCELLED -> R.string.task_cancelled
})
@Composable
fun taskViewLabel(view: TaskView): String = when (view) {
    TaskView.ACTIONABLE -> stringResource(R.string.task_actionable)
    TaskView.ALL -> stringResource(R.string.task_all)
    else -> taskStatusLabel(TaskStatus.valueOf(view.name))
}
fun taskDay(date: LocalDate): String = date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))

@Composable
fun TaskMessage(message: String, onDismiss: (() -> Unit)? = null) {
    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(13.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                Icon(Icons.Rounded.Info, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Text(message, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            }
            if (onDismiss != null) TextButton(onClick = onDismiss) { Text(stringResource(R.string.task_dismiss)) }
        }
    }
}

@Composable
fun TaskLoadState(loading: Boolean, message: String, onRetry: (() -> Unit)? = null) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        if (loading) CircularProgressIndicator() else Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)) {
            TaskSectionIcon(Icons.Rounded.TaskAlt)
            Text(message, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (onRetry != null) TextButton(onClick = onRetry) { Text(stringResource(R.string.task_retry)) }
        }
    }
}

@Composable
fun TaskCategory(category: CategoryEntity?) {
    val color = taskCategoryColor(category?.color, MaterialTheme.colorScheme.primary)
    val tint = if (category == null || color.luminance() > 0.82f ||
        LocalAppDarkTheme.current && color.luminance() < 0.15f) MaterialTheme.colorScheme.primary else color
    Surface(color = tint.copy(alpha = 0.10f), shape = RoundedCornerShape(10.dp)) {
        Row(Modifier.padding(horizontal = 9.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(category?.let { IconData.getIconByKey(it.icon) } ?: Icons.Rounded.Category, null,
                tint = tint, modifier = Modifier.size(16.dp))
            Text(category?.name ?: stringResource(R.string.uncategorized), style = MaterialTheme.typography.labelMedium,
                maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

fun taskCategoryColor(value: String?, fallback: Color): Color = runCatching {
    val packed = value?.toULongOrNull() ?: return fallback
    (if (packed <= UInt.MAX_VALUE.toULong()) Color(packed.toInt()) else Color(packed)).also { it.colorSpace }
}.getOrDefault(fallback)

@Composable
fun TaskMetadata(snapshot: TaskSnapshot, category: CategoryEntity?, sync: TaskSyncState? = null) {
    TaskMetadata(snapshot.summary(), category, sync)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskMetadata(snapshot: TaskSummary, category: CategoryEntity?, sync: TaskSyncState? = null) {
    val task = snapshot.task
    val status = TaskStatus.fromCode(task.status)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TaskCategory(category)
            Row(Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Icon(Icons.Rounded.Event, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                Text(task.scheduledDate?.let(::taskDay) ?: stringResource(R.string.task_unscheduled),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (snapshot.estimate.milliseconds != null || snapshot.stepCount > 0) Row(
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Icon(Icons.Rounded.Schedule, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                TaskEstimateText(snapshot.estimate)
            }
        }
        if (!status.isTerminal && task.scheduledDate?.isBefore(LocalDate.now()) == true) {
            Text(stringResource(R.string.task_past_date), style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary)
        }
        if (snapshot.stepCount > 0) {
            Text(pluralStringResource(R.plurals.task_progress_count, snapshot.stepCount,
                snapshot.finishedSteps, snapshot.stepCount), style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            LinearProgressIndicator(progress = { snapshot.finishedSteps.toFloat() / snapshot.stepCount },
                modifier = Modifier.fillMaxWidth().height(4.dp), trackColor = MaterialTheme.colorScheme.primaryContainer)
        }
        task.completedDate?.let { Text(stringResource(R.string.task_completed_on, taskDay(it)), style = MaterialTheme.typography.bodySmall) }
        if (snapshot.reminder != null) Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.NotificationsNone, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp)); Text(stringResource(R.string.task_reminder_set), style = MaterialTheme.typography.labelSmall)
        }
        Text(stringResource(when {
            sync?.needsAttention() == true -> R.string.task_sync_attention
            sync?.pending == true || task.serverVersion == null -> R.string.task_sync_pending
            else -> R.string.task_sync_ok
        }), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

fun TaskSyncState.needsAttention() = conflicted || rejected || (serverDeleted && pending)

@Composable
fun TaskStatusControl(snapshot: TaskSnapshot, enabled: Boolean, onChange: (TaskStatus) -> Unit) {
    TaskStatusControl(snapshot.task.title, TaskStatus.fromCode(snapshot.task.status), enabled, onChange)
}

@Composable
fun TaskStatusControl(snapshot: TaskSummary, enabled: Boolean, onChange: (TaskStatus) -> Unit) {
    TaskStatusControl(snapshot.task.title, TaskStatus.fromCode(snapshot.task.status), enabled, onChange)
}

@Composable
private fun TaskStatusControl(title: String, status: TaskStatus, enabled: Boolean, onChange: (TaskStatus) -> Unit) {
    var menu by remember { mutableStateOf(false) }
    var confirm by remember { mutableStateOf<TaskStatus?>(null) }
    val description = stringResource(R.string.task_status_description, title)
    val statusLabel = taskStatusLabel(status)
    val color = taskStatusColor(status)
    Box {
        OutlinedButton(onClick = { menu = true }, enabled = enabled,
            shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, color.copy(alpha = 0.28f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = color, containerColor = color.copy(alpha = 0.08f)),
            modifier = Modifier.semantics { contentDescription = description; stateDescription = statusLabel }) {
            Icon(taskStatusIcon(status), null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(7.dp))
            Text(statusLabel)
            Spacer(Modifier.width(7.dp))
            Icon(Icons.Rounded.ExpandMore, null, modifier = Modifier.size(18.dp))
        }
        DropdownMenu(expanded = menu && enabled, onDismissRequest = { menu = false }) {
            listOf(TaskStatus.TODO, TaskStatus.IN_PROGRESS, TaskStatus.DONE, TaskStatus.SKIPPED, TaskStatus.CANCELLED).forEach { target ->
                DropdownMenuItem(text = { Text(taskStatusLabel(target)) }, enabled = enabled && target != status &&
                    !(status.isTerminal && target == TaskStatus.IN_PROGRESS), onClick = {
                    menu = false
                    if (target == TaskStatus.TODO || target.isTerminal) confirm = target else onChange(target)
                })
            }
        }
    }
    confirm?.let { target -> AlertDialog(onDismissRequest = { confirm = null },
        title = { Text(stringResource(R.string.task_change_status, taskStatusLabel(target))) },
        text = { Text(if (target == TaskStatus.TODO) stringResource(R.string.task_reset_body)
            else stringResource(R.string.task_terminal_body, taskStatusLabel(target))) },
        confirmButton = { TextButton(enabled = enabled, onClick = { confirm = null; onChange(target) }) { Text(stringResource(R.string.task_confirm)) } },
        dismissButton = { TextButton(onClick = { confirm = null }) { Text(stringResource(R.string.task_cancel)) } }) }
}

@Composable
fun TaskDeleteDialog(onDismiss: () -> Unit, onDelete: () -> Unit, enabled: Boolean) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.task_delete_title)) },
        text = { Text(stringResource(R.string.task_delete_body)) },
        confirmButton = { TextButton(enabled = enabled, onClick = onDelete) { Text(stringResource(R.string.task_delete)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.task_cancel)) } })
}

@Composable
fun TaskSyncPanel(issue: TaskSyncIssue, enabled: Boolean, onResolve: (TaskConflictChoice) -> Unit) {
    var choice by remember { mutableStateOf<TaskConflictChoice?>(null) }
    TaskSection(stringResource(R.string.task_sync_attention), icon = Icons.Rounded.SyncProblem) {
        Text(issue.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(stringResource(if (issue.seriesIssue) R.string.task_series_sync_help else if (issue.state.serverDeleted) R.string.task_sync_deleted else R.string.task_sync_help),
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        TextButton(enabled = enabled, onClick = { choice = TaskConflictChoice.SERVER }) { Text(stringResource(R.string.task_sync_server)) }
        if (!issue.seriesIssue && !issue.state.serverDeleted) TextButton(enabled = enabled, onClick = { choice = TaskConflictChoice.LOCAL }) { Text(stringResource(R.string.task_sync_local)) }
        TextButton(enabled = enabled, onClick = { choice = TaskConflictChoice.COPY }) { Text(stringResource(R.string.task_sync_copy)) }
    }
    choice?.let { selected -> AlertDialog(onDismissRequest = { choice = null },
        title = { Text(stringResource(R.string.task_sync_attention)) },
        text = { Text(stringResource(if (issue.seriesIssue && selected == TaskConflictChoice.COPY) R.string.task_series_copy_confirm else when (selected) {
            TaskConflictChoice.SERVER -> R.string.task_sync_server_confirm
            TaskConflictChoice.LOCAL -> R.string.task_sync_local_confirm
            TaskConflictChoice.COPY -> R.string.task_sync_copy_confirm
        })) },
        confirmButton = { TextButton(enabled = enabled, onClick = { choice = null; onResolve(selected) }) { Text(stringResource(R.string.task_confirm)) } },
        dismissButton = { TextButton(onClick = { choice = null }) { Text(stringResource(R.string.task_cancel)) } }) }
}
