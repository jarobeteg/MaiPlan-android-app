package com.example.maiplan.home.task.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.example.maiplan.R
import com.example.maiplan.database.entities.CategoryEntity
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
    MaterialTheme(colorScheme = colors.copy(onSurface = colors.onBackground,
        primary = if (dark) AppThemeManager.selectedTheme.primaryLight else colors.primary,
        onPrimary = if (dark) Color(0xFF101314) else colors.onPrimary,
        onSecondaryContainer = if (dark) Color(0xFF101314) else colors.onSecondaryContainer,
        onSurfaceVariant = if (dark) Color(0xFFBBC2CE) else Color(0xFF505763),
        onError = Color.White), content = content)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskTopBar(title: String, onBack: (() -> Unit)? = null, actions: @Composable RowScope.() -> Unit = {}) {
    TopAppBar(title = { Text(title) }, navigationIcon = {
        if (onBack != null) IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.task_back))
        }
    }, actions = actions)
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
    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(message, style = MaterialTheme.typography.bodyMedium)
            if (onDismiss != null) TextButton(onClick = onDismiss) { Text(stringResource(R.string.task_dismiss)) }
        }
    }
}

@Composable
fun TaskLoadState(loading: Boolean, message: String, onRetry: (() -> Unit)? = null) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        if (loading) CircularProgressIndicator() else Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message)
            if (onRetry != null) TextButton(onClick = onRetry) { Text(stringResource(R.string.task_retry)) }
        }
    }
}

@Composable
fun TaskCategory(category: CategoryEntity?) {
    val color = taskCategoryColor(category?.color, MaterialTheme.colorScheme.primary)
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (category != null) Surface(color = color, shape = MaterialTheme.shapes.small) {
            Icon(IconData.getIconByKey(category.icon), null,
                tint = if (color.luminance() > 0.5f) Color.Black else Color.White,
                modifier = Modifier.padding(4.dp).size(18.dp))
        }
        Text(category?.name ?: stringResource(R.string.uncategorized), style = MaterialTheme.typography.labelMedium)
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

@Composable
fun TaskMetadata(snapshot: TaskSummary, category: CategoryEntity?, sync: TaskSyncState? = null) {
    val task = snapshot.task
    val status = TaskStatus.fromCode(task.status)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        TaskCategory(category)
        Text(task.scheduledDate?.let(::taskDay) ?: stringResource(R.string.task_unscheduled),
            style = MaterialTheme.typography.bodySmall)
        if (!status.isTerminal && task.scheduledDate?.isBefore(LocalDate.now()) == true) {
            Text(stringResource(R.string.task_past_date), style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary)
        }
        if (snapshot.estimate.milliseconds != null || snapshot.stepCount > 0) TaskEstimateText(snapshot.estimate)
        if (snapshot.stepCount > 0) Text(pluralStringResource(R.plurals.task_progress_count, snapshot.stepCount,
            snapshot.finishedSteps, snapshot.stepCount), style = MaterialTheme.typography.bodySmall)
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
    Box {
        OutlinedButton(onClick = { menu = true }, enabled = enabled,
            modifier = Modifier.semantics { contentDescription = description; stateDescription = statusLabel }) {
            Text(stringResource(R.string.task_status_action, taskStatusLabel(status)))
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
    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(issue.title, style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.task_sync_attention), style = MaterialTheme.typography.labelLarge)
            Text(stringResource(if (issue.seriesIssue) R.string.task_series_sync_help else if (issue.state.serverDeleted) R.string.task_sync_deleted else R.string.task_sync_help))
            TextButton(enabled = enabled, onClick = { choice = TaskConflictChoice.SERVER }) { Text(stringResource(R.string.task_sync_server)) }
            if (!issue.seriesIssue && !issue.state.serverDeleted) TextButton(enabled = enabled, onClick = { choice = TaskConflictChoice.LOCAL }) { Text(stringResource(R.string.task_sync_local)) }
            TextButton(enabled = enabled, onClick = { choice = TaskConflictChoice.COPY }) { Text(stringResource(R.string.task_sync_copy)) }
        }
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
