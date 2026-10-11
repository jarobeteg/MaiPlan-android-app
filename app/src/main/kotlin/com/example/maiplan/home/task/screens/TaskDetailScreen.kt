package com.example.maiplan.home.task.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.maiplan.R
import com.example.maiplan.viewmodel.task.TaskViewModel

@Composable
fun TaskDetailScreen(id: Long, viewModel: TaskViewModel, onBack: () -> Unit, onEdit: () -> Unit, onOpen: (Long) -> Unit) {
    val detail by remember(id, viewModel) { viewModel.detail(id) }.collectAsStateWithLifecycle()
    val collection by viewModel.metadata.collectAsStateWithLifecycle()
    val actions by viewModel.actions.collectAsStateWithLifecycle()
    var deleting by rememberSaveable(id) { mutableStateOf(false) }
    var moreMenu by remember(id) { mutableStateOf(false) }
    val busy = id in actions.busyIds
    BackHandler(enabled = busy) { }
    Scaffold(containerColor = Color.Transparent, topBar = {
        TaskTopBar(stringResource(R.string.task_detail), { if (!busy) onBack() }, actions = {
            if (detail.task != null && !detail.loading && detail.error == null) {
                IconButton(enabled = !busy, onClick = onEdit, modifier = Modifier.testTag("task-detail-edit")) {
                    Icon(Icons.Rounded.Edit, stringResource(R.string.task_edit))
                }
                Box {
                    IconButton(enabled = !busy, onClick = { moreMenu = true }) {
                        Icon(Icons.Rounded.MoreVert, stringResource(R.string.task_detail_more))
                    }
                    DropdownMenu(expanded = moreMenu && !busy, onDismissRequest = { moreMenu = false }) {
                        DropdownMenuItem(text = { Text(stringResource(R.string.task_delete), color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Rounded.DeleteOutline, null, tint = MaterialTheme.colorScheme.error) },
                            onClick = { moreMenu = false; deleting = true })
                    }
                }
            }
        })
    }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            val snapshot = detail.task
            when {
                detail.loading -> TaskLoadState(true, "")
                detail.error != null -> TaskLoadState(false, detail.error!!, viewModel::retry)
                snapshot == null -> TaskLoadState(false, stringResource(R.string.task_unavailable) + "\n" + stringResource(R.string.task_unavailable_help))
                else -> BoxWithConstraints(Modifier.widthIn(max = 1120.dp).fillMaxWidth()) {
                    val wide = maxWidth >= 840.dp
                    val scroll = rememberScrollState()
                    var viewport by remember { mutableStateOf<Rect?>(null) }
                    val category = collection.categories.find { it.categoryLocalId == snapshot.effectiveCategoryLocalId }
                    val issue = collection.issues.find { it.syncId == snapshot.task.syncId }
                    val planning: @Composable () -> Unit = {
                        TaskDetailOverview(snapshot, issue?.state)
                    }
                    val reminder: @Composable () -> Unit = {
                        if (snapshot.reminder != null || snapshot.task.relativeReminderJson != null) {
                            TaskDetailCard(stringResource(R.string.task_reminder), Icons.Rounded.NotificationsNone) {
                                TaskReminderSummary(snapshot)
                            }
                        }
                    }
                    val content: @Composable () -> Unit = {
                        snapshot.task.description?.takeIf { it.isNotBlank() }?.let { description ->
                            TaskDetailCard(stringResource(R.string.description), Icons.AutoMirrored.Rounded.Notes) {
                                Text(description, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                        TaskDetailChecklist(snapshot, busy, scroll, viewport,
                            onStepStatus = { step, status -> viewModel.setStepStatus(id, step, status) },
                            onReorder = { original, order -> viewModel.reorderSteps(id, original, order) }, onEdit = onEdit)
                    }
                    Column(Modifier.fillMaxWidth().testTag("task-detail")
                        .onGloballyPositioned { viewport = it.boundsInWindow() }
                        .verticalScroll(scroll).padding(horizontal = if (wide) 24.dp else 18.dp, vertical = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        TaskDetailHero(snapshot, category, busy) { viewModel.setStatus(id, it) }
                        actions.message?.let { TaskMessage(it, viewModel::dismissMessage) }
                        collection.error?.let {
                            TaskMessage(it)
                            TextButton(onClick = viewModel::retry) { Text(stringResource(R.string.task_retry)) }
                        }
                        issue?.takeIf { it.state.needsAttention() }?.let {
                            TaskSyncPanel(it, !busy) { choice ->
                                viewModel.resolve(it, choice) { resolved -> if (resolved != id) onOpen(resolved) }
                            }
                        }
                        if (wide) Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.Top) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) { content() }
                            Column(Modifier.width(360.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) { planning(); reminder() }
                        } else {
                            planning()
                            content()
                            reminder()
                        }
                        Spacer(Modifier.height(8.dp))
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
