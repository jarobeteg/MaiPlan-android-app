package com.example.maiplan.home.task.screens

import androidx.compose.foundation.layout.*
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.maiplan.R
import com.example.maiplan.repository.task.TaskStatus
import com.example.maiplan.repository.task.TaskEstimate
import com.example.maiplan.utils.LocalAdaptiveLayout
import com.example.maiplan.viewmodel.task.TaskViewModel

@Composable
fun TaskDetailScreen(id: Long, viewModel: TaskViewModel, onBack: () -> Unit, onEdit: () -> Unit, onOpen: (Long) -> Unit) {
    val detail by remember(id, viewModel) { viewModel.detail(id) }.collectAsStateWithLifecycle()
    val collection by viewModel.metadata.collectAsStateWithLifecycle()
    val actions by viewModel.actions.collectAsStateWithLifecycle()
    var deleting by rememberSaveable { mutableStateOf(false) }
    val busy = id in actions.busyIds
    BackHandler(enabled = busy) { }
    Scaffold(topBar = { TaskTopBar(stringResource(R.string.task_detail), { if (!busy) onBack() }) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            val snapshot = detail.task
            when {
                detail.loading -> TaskLoadState(true, "")
                detail.error != null -> TaskLoadState(false, detail.error!!, viewModel::retry)
                snapshot == null -> TaskLoadState(false, stringResource(R.string.task_unavailable) + "\n" + stringResource(R.string.task_unavailable_help))
                else -> Column(Modifier.widthIn(max = LocalAdaptiveLayout.current.formMaxWidth).fillMaxWidth()
                    .verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    Text(snapshot.task.title, style = MaterialTheme.typography.headlineSmall)
                    Text(taskStatusLabel(TaskStatus.fromCode(snapshot.task.status)), style = MaterialTheme.typography.titleMedium)
                    TaskMetadata(snapshot, collection.categories.find { it.categoryLocalId == snapshot.effectiveCategoryLocalId },
                        collection.issues.find { it.syncId == snapshot.task.syncId }?.state)
                    snapshot.task.description?.let { Text(it) }
                    TaskReminderSummary(snapshot)
                    actions.message?.let { TaskMessage(it, viewModel::dismissMessage) }
                    collection.error?.let { TaskMessage(it); TextButton(onClick = viewModel::retry) { Text(stringResource(R.string.task_retry)) } }
                    collection.issues.find { it.syncId == snapshot.task.syncId && it.state.needsAttention() }?.let { issue ->
                        TaskSyncPanel(issue, !busy) { choice -> viewModel.resolve(issue, choice) { resolved -> if (resolved != id) onOpen(resolved) } }
                    }
                    TaskStatusControl(snapshot, !busy) { viewModel.setStatus(id, it) }
                    Button(enabled = !busy, onClick = onEdit) { Text(stringResource(R.string.task_edit)) }
                    OutlinedButton(enabled = !busy, onClick = { deleting = true }) { Text(stringResource(R.string.task_delete)) }
                    if (snapshot.subtasks.isNotEmpty()) {
                        HorizontalDivider()
                        Text(stringResource(R.string.task_steps), style = MaterialTheme.typography.titleMedium)
                        val editable = !busy && !TaskStatus.fromCode(snapshot.task.status).isTerminal
                        if (TaskStatus.fromCode(snapshot.task.status).isTerminal) TaskMessage(stringResource(R.string.task_step_locked))
                        val byKey = snapshot.subtasks.associateBy { it.syncId.toString() }
                        TaskOrderedSteps(snapshot.subtasks.map { it.syncId.toString() }, editable, title = { byKey.getValue(it).title },
                            onReorder = { original, order -> viewModel.reorderSteps(id,
                                original.map { byKey.getValue(it).subtaskLocalId }, order.map { byKey.getValue(it).subtaskLocalId }) },
                            content = { key ->
                                val step = byKey.getValue(key)
                                val status = TaskStatus.fromCode(step.status)
                                val checkboxLabel = stringResource(R.string.task_step_checkbox, step.title)
                                val statusLabel = taskStatusLabel(status)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = status == TaskStatus.DONE, enabled = editable,
                                        onCheckedChange = { viewModel.setStepStatus(id, step.subtaskLocalId, if (it) TaskStatus.DONE else TaskStatus.TODO) },
                                        modifier = Modifier.testTag("task-step-check-$key").semantics { contentDescription = checkboxLabel; stateDescription = statusLabel })
                                    Text(step.title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                                }
                                TaskEstimateText(TaskEstimate(step.estimatedMilliseconds, false, false))
                                var menu by remember { mutableStateOf(false) }
                                val statusDescription = stringResource(R.string.task_step_status_description, step.title)
                                Box {
                                    TextButton(enabled = editable, onClick = { menu = true },
                                        modifier = Modifier.testTag("task-step-status-$key").semantics { contentDescription = statusDescription; stateDescription = statusLabel }) {
                                        Text(stringResource(R.string.task_step_status, taskStatusLabel(status)), color = MaterialTheme.colorScheme.onSurface)
                                    }
                                    DropdownMenu(expanded = menu && editable, onDismissRequest = { menu = false }) {
                                        listOf(TaskStatus.TODO, TaskStatus.IN_PROGRESS, TaskStatus.DONE, TaskStatus.SKIPPED, TaskStatus.CANCELLED).forEach { target ->
                                            DropdownMenuItem(text = { Text(taskStatusLabel(target)) }, enabled = target != status,
                                                onClick = { menu = false; viewModel.setStepStatus(id, step.subtaskLocalId, target) })
                                        }
                                    }
                                }
                            })
                    }
                }
            }
        }
    }
    val seriesId = detail.task?.task?.seriesId
    if (deleting && seriesId != null) TaskSeriesDeleteDialog(!busy, { deleting = false }, {
        deleting = false; viewModel.deleteOccurrence(id) { next -> if (next == null) onBack() else { onBack(); onOpen(next) } }
    }, { deleting = false; viewModel.deleteAll(id, seriesId, onBack) })
    else if (deleting) TaskDeleteDialog(onDismiss = { deleting = false }, enabled = !busy, onDelete = {
        deleting = false; viewModel.delete(id, onBack)
    })
}
