package com.example.maiplan.home.task.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.maiplan.R
import com.example.maiplan.database.entities.CategoryEntity
import com.example.maiplan.database.entities.SubtaskEntity
import com.example.maiplan.repository.task.TaskEstimate
import com.example.maiplan.repository.task.TaskSnapshot
import com.example.maiplan.repository.task.TaskStatus
import com.example.maiplan.repository.task.TaskSyncState
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun TaskDetailHero(snapshot: TaskSnapshot, category: CategoryEntity?, busy: Boolean, onStatus: (TaskStatus) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val status = TaskStatus.fromCode(snapshot.task.status)
    val accent = if (status == TaskStatus.DONE) taskStatusColor(status) else colors.primary
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val wide = maxWidth >= 600.dp && LocalDensity.current.fontScale <= 1.3f
        val titleContent: @Composable () -> Unit = {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TaskCategory(category)
                if (snapshot.task.seriesId != null) Surface(shape = RoundedCornerShape(10.dp), color = colors.surfaceVariant) {
                    Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Rounded.Repeat, null, modifier = Modifier.size(16.dp), tint = colors.onSurfaceVariant)
                        Text(stringResource(R.string.task_detail_recurring), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
            Text(snapshot.task.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { heading() })
        }
        Surface(shape = RoundedCornerShape(26.dp), color = colors.surface,
            border = BorderStroke(1.dp, accent.copy(alpha = 0.18f))) {
            Column(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(
                accent.copy(alpha = 0.12f).compositeOver(colors.surface), colors.surface))).padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)) {
                if (wide) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(14.dp)) { titleContent() }
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(stringResource(R.string.task_detail_status), style = MaterialTheme.typography.labelLarge, color = colors.onSurfaceVariant)
                        TaskStatusControl(snapshot, !busy, onStatus)
                    }
                } else {
                    titleContent()
                    HorizontalDivider(color = accent.copy(alpha = 0.14f))
                    BoxWithConstraints(Modifier.fillMaxWidth()) {
                        if (maxWidth < 280.dp || LocalDensity.current.fontScale > 1.3f) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(stringResource(R.string.task_detail_status), style = MaterialTheme.typography.labelLarge, color = colors.onSurfaceVariant)
                                TaskStatusControl(snapshot, !busy, onStatus)
                            }
                        } else Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(stringResource(R.string.task_detail_status), style = MaterialTheme.typography.labelLarge, color = colors.onSurfaceVariant)
                            TaskStatusControl(snapshot, !busy, onStatus)
                        }
                    }
                }
                if (busy) LinearProgressIndicator(Modifier.fillMaxWidth().testTag("task-detail-saving"))
            }
        }
    }
}

@Composable
internal fun TaskDetailCard(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    TaskPanel {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(shape = RoundedCornerShape(11.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                    Icon(icon, null, modifier = Modifier.padding(9.dp).size(18.dp), tint = MaterialTheme.colorScheme.primary)
                }
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f).semantics { heading() })
            }
            content()
        }
    }
}

@Composable
internal fun TaskDetailOverview(snapshot: TaskSnapshot, sync: TaskSyncState?) {
    val task = snapshot.task
    val status = TaskStatus.fromCode(task.status)
    TaskDetailCard(stringResource(R.string.task_detail_overview), Icons.Rounded.Dashboard) {
        val planned: @Composable (Modifier) -> Unit = { modifier ->
            TaskDetailFact(modifier, Icons.Rounded.CalendarToday, stringResource(R.string.task_by_day),
                task.scheduledDate?.let(::taskDay) ?: stringResource(R.string.task_unscheduled),
                if (!status.isTerminal && task.scheduledDate?.isBefore(LocalDate.now()) == true) stringResource(R.string.task_past_date) else null)
        }
        val estimate: @Composable (Modifier) -> Unit = { modifier ->
            TaskDetailFact(modifier, Icons.Rounded.Schedule, stringResource(R.string.task_detail_estimate),
                if (snapshot.estimate.milliseconds == null) stringResource(R.string.task_detail_no_estimate) else taskEstimateDuration(snapshot.estimate.milliseconds),
                when {
                    snapshot.estimate.isManual -> stringResource(R.string.task_detail_manual_estimate)
                    snapshot.estimate.isPartial -> stringResource(R.string.task_detail_partial_estimate)
                    snapshot.subtasks.isNotEmpty() -> stringResource(R.string.task_detail_step_estimate)
                    else -> null
                })
        }
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            if (maxWidth >= 300.dp && LocalDensity.current.fontScale <= 1.3f) {
                Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    planned(Modifier.weight(1f).fillMaxHeight())
                    estimate(Modifier.weight(1f).fillMaxHeight())
                }
            } else Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                planned(Modifier.fillMaxWidth())
                estimate(Modifier.fillMaxWidth())
            }
        }
        task.completedDate?.let { date ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.CheckCircle, null, tint = taskStatusColor(TaskStatus.DONE), modifier = Modifier.size(18.dp))
                Text(stringResource(R.string.task_completed_on, taskDay(date)), style = MaterialTheme.typography.bodyMedium)
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        val needsAttention = sync?.needsAttention() == true
        val pending = sync?.pending == true || task.serverVersion == null
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val color = if (needsAttention) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            Icon(if (needsAttention) Icons.Rounded.SyncProblem else if (pending) Icons.Rounded.CloudQueue else Icons.Rounded.CloudDone,
                null, modifier = Modifier.size(16.dp), tint = color)
            Text(stringResource(if (needsAttention) R.string.task_sync_attention else if (pending) R.string.task_sync_pending else R.string.task_sync_ok),
                style = MaterialTheme.typography.labelMedium, color = color)
        }
    }
}

@Composable
private fun TaskDetailFact(modifier: Modifier, icon: ImageVector, label: String, value: String, note: String?) {
    Surface(modifier, color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(15.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f))
            }
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            note?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@Composable
internal fun TaskDetailChecklist(snapshot: TaskSnapshot, busy: Boolean, scrollState: ScrollState, viewport: Rect?,
    onStepStatus: (Long, TaskStatus) -> Unit, onReorder: (List<Long>, List<Long>) -> Unit, onEdit: () -> Unit) {
    val terminal = TaskStatus.fromCode(snapshot.task.status).isTerminal
    val editable = !busy && !terminal
    TaskDetailCard(stringResource(R.string.task_steps), Icons.Rounded.Checklist) {
        if (snapshot.subtasks.isEmpty()) {
            Text(stringResource(R.string.task_detail_empty_steps), style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (terminal) Text(stringResource(R.string.task_step_locked), style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            else OutlinedButton(enabled = !busy, onClick = onEdit, shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Rounded.Add, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.task_detail_add_steps))
            }
        } else {
            val progress by animateFloatAsState(snapshot.finishedSteps.toFloat() / snapshot.subtasks.size, label = "task-detail-progress")
            Text(pluralStringResource(R.plurals.task_progress_count, snapshot.subtasks.size, snapshot.finishedSteps, snapshot.subtasks.size),
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(6.dp),
                color = if (terminal) taskStatusColor(TaskStatus.fromCode(snapshot.task.status)) else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer, drawStopIndicator = {})
            if (terminal) Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.Lock, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(stringResource(R.string.task_step_locked), style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            val byKey = snapshot.subtasks.associateBy { it.syncId.toString() }
            TaskOrderedSteps(snapshot.subtasks.map { it.syncId.toString() }, editable, title = { byKey.getValue(it).title },
                scrollState = scrollState, viewport = viewport, showReorderControls = !terminal,
                onReorder = { original, order ->
                    onReorder(original.map { byKey.getValue(it).subtaskLocalId }, order.map { byKey.getValue(it).subtaskLocalId })
                }, content = { key ->
                    val step = byKey.getValue(key)
                    val status = TaskStatus.fromCode(step.status)
                    val checkboxLabel = stringResource(R.string.task_step_checkbox, step.title)
                    val statusLabel = taskStatusLabel(status)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = status == TaskStatus.DONE, enabled = editable,
                            onCheckedChange = { onStepStatus(step.subtaskLocalId, if (it) TaskStatus.DONE else TaskStatus.TODO) },
                            modifier = Modifier.testTag("task-step-check-$key").semantics { contentDescription = checkboxLabel; stateDescription = statusLabel })
                        Text(step.title, style = MaterialTheme.typography.titleSmall,
                            color = if (status.isTerminal) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                            textDecoration = if (status == TaskStatus.DONE) TextDecoration.LineThrough else null,
                            modifier = Modifier.weight(1f))
                    }
                    if (step.estimatedMilliseconds != null) Box(Modifier.padding(start = 48.dp)) {
                        TaskEstimateText(TaskEstimate(step.estimatedMilliseconds, false, false))
                    }
                }, extraActions = { key ->
                    val step = byKey.getValue(key)
                    if (terminal) TaskStatusBadge(TaskStatus.fromCode(step.status))
                    else TaskDetailStepStatus(step, editable) { onStepStatus(step.subtaskLocalId, it) }
                })
        }
    }
}

@Composable
private fun TaskDetailStepStatus(step: SubtaskEntity, enabled: Boolean, onChange: (TaskStatus) -> Unit) {
    var menu by remember { mutableStateOf(false) }
    val status = TaskStatus.fromCode(step.status)
    val description = stringResource(R.string.task_step_status_description, step.title)
    val statusLabel = taskStatusLabel(status)
    Box {
        TextButton(enabled = enabled, onClick = { menu = true }, contentPadding = PaddingValues(horizontal = 8.dp),
            modifier = Modifier.testTag("task-step-status-${step.syncId}").semantics { contentDescription = description; stateDescription = statusLabel }) {
            Icon(taskStatusIcon(status), null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(statusLabel)
            Spacer(Modifier.width(4.dp))
            Icon(Icons.Rounded.ExpandMore, null, modifier = Modifier.size(18.dp))
        }
        DropdownMenu(expanded = menu && enabled, onDismissRequest = { menu = false }) {
            taskStatusMenuOrder.forEach { target ->
                TaskStatusMenuItem(target, enabled = target != status,
                    onClick = { menu = false; onChange(target) })
            }
        }
    }
}
