package com.example.maiplan.home.task.screens

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.maiplan.R
import com.example.maiplan.repository.task.TaskEstimate
import com.example.maiplan.viewmodel.task.TaskStepDraft
import java.math.BigDecimal

@Composable
fun TaskEstimateText(estimate: TaskEstimate) {
    val ms = estimate.milliseconds
    val label = if (ms == null) stringResource(R.string.task_estimate_unknown)
        else if (ms % 60_000 == 0L) stringResource(R.string.task_estimate_minutes, (ms / 60_000).toString())
        else stringResource(R.string.task_estimate_seconds, BigDecimal(ms).movePointLeft(3).stripTrailingZeros().toPlainString())
    Text(when {
        estimate.isManual -> stringResource(R.string.task_manual_estimate, label)
        estimate.isPartial -> stringResource(R.string.task_partial_estimate, label)
        else -> label
    }, style = MaterialTheme.typography.bodySmall)
}

@Composable
fun TaskOrderedSteps(keys: List<String>, enabled: Boolean, title: (String) -> String,
    onReorder: (List<String>, List<String>) -> Unit, content: @Composable (String) -> Unit,
    extraActions: @Composable RowScope.(String) -> Unit = {}) {
    var preview by remember { mutableStateOf<List<String>?>(null) }
    var dragged by remember { mutableStateOf<String?>(null) }
    var distance by remember { mutableFloatStateOf(0f) }
    val heights = remember { mutableMapOf<String, Int>() }
    val currentEnabled by rememberUpdatedState(enabled)
    val currentKeys by rememberUpdatedState(keys)
    val commit by rememberUpdatedState(onReorder)
    LaunchedEffect(keys, enabled) { preview = null; dragged = null; distance = 0f }
    fun move(key: String, offset: Int) {
        val index = keys.indexOf(key)
        val target = index + offset
        if (enabled && index >= 0 && target in keys.indices) onReorder(keys, keys.toMutableList().apply { add(target, removeAt(index)) })
    }
    val displayedOrder = preview?.takeIf { it.size == keys.size && it.toSet() == keys.toSet() } ?: keys
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        displayedOrder.forEachIndexed { index, id -> key(id) {
            val position = stringResource(R.string.task_step_position, index + 1, keys.size)
            Surface(shape = MaterialTheme.shapes.medium,
                color = if (dragged == id) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth().onSizeChanged { heights[id] = it.height }.testTag("task-step-$id")
                    .semantics { stateDescription = position }) {
                Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    content(id)
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        val dragLabel = stringResource(R.string.task_step_drag, title(id))
                        Icon(Icons.Rounded.DragHandle, dragLabel, modifier = Modifier.size(48.dp).padding(12.dp)
                            .pointerInput(id, keys, enabled) {
                                if (enabled) detectDragGesturesAfterLongPress(
                                    onDragStart = { preview = keys; dragged = id; distance = 0f },
                                    onDragCancel = { preview = null; dragged = null; distance = 0f },
                                    onDragEnd = {
                                        val order = preview
                                        if (currentEnabled && currentKeys == keys && order != null && order != keys) commit(keys, order)
                                        preview = null; dragged = null; distance = 0f
                                    }, onDrag = { change, amount ->
                                        change.consume()
                                        distance += amount.y
                                        val order = preview ?: return@detectDragGesturesAfterLongPress
                                        val at = order.indexOf(id)
                                        val next = at + if (distance > 0) 1 else -1
                                        if (at >= 0 && next in order.indices) {
                                            val threshold = ((heights[order[next]] ?: 100) + 8.dp.toPx()) / 2f
                                            if (kotlin.math.abs(distance) > threshold) {
                                                preview = order.toMutableList().apply { add(next, removeAt(at)) }
                                                distance -= if (distance > 0) threshold else -threshold
                                            }
                                        }
                                    })
                            })
                        IconButton(enabled = enabled && index > 0, onClick = { move(id, -1) }) {
                            Icon(Icons.Rounded.ArrowUpward, stringResource(R.string.task_step_up, title(id)))
                        }
                        IconButton(enabled = enabled && index < keys.lastIndex, onClick = { move(id, 1) }) {
                            Icon(Icons.Rounded.ArrowDownward, stringResource(R.string.task_step_down, title(id)))
                        }
                        Spacer(Modifier.weight(1f))
                        extraActions(id)
                    }
                }
            }
        } }
    }
}

@Composable
fun TaskStepEditorDialog(step: TaskStepDraft, onDismiss: () -> Unit, onSave: (TaskStepDraft) -> Unit, enabled: Boolean = true) {
    var title by rememberSaveable(step.key) { mutableStateOf(step.title) }
    var minutes by rememberSaveable(step.key) { mutableStateOf(step.minutes) }
    var seconds by rememberSaveable(step.key) { mutableStateOf(step.seconds) }
    var error by rememberSaveable(step.key) { mutableStateOf<String?>(null) }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).imePadding().padding(horizontal = 24.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center) {
            Surface(modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth(), shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.task_step_editor), style = MaterialTheme.typography.headlineSmall)
                    Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        if (!enabled) Text(stringResource(R.string.task_step_locked))
                        OutlinedTextField(title, { title = it; error = null }, label = { Text(stringResource(R.string.task_step_title)) },
                            modifier = Modifier.fillMaxWidth(), maxLines = 4)
                        Text(stringResource(R.string.task_estimate))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(minutes, { minutes = it; error = null }, label = { Text(stringResource(R.string.task_minutes)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.weight(1f))
                            OutlinedTextField(seconds, { seconds = it; error = null }, label = { Text(stringResource(R.string.task_seconds)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.weight(1f))
                        }
                        Text(stringResource(R.string.task_step_estimate_help), style = MaterialTheme.typography.bodySmall)
                    }
                    Row(Modifier.align(Alignment.End), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)) {
                            Text(stringResource(R.string.task_cancel))
                        }
                        TextButton(enabled = enabled, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface), onClick = {
                            val draft = step.copy(title = title, minutes = minutes, seconds = seconds)
                            try { draft.input(); onSave(draft) } catch (e: IllegalArgumentException) { error = e.message }
                        }) { Text(stringResource(R.string.task_step_save)) }
                    }
                }
            }
        }
    }
}
