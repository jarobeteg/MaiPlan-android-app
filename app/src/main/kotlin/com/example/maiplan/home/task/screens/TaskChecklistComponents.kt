package com.example.maiplan.home.task.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.animateBounds
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.example.maiplan.R
import com.example.maiplan.repository.task.TaskEstimate
import com.example.maiplan.viewmodel.task.TaskStepDraft
import com.example.maiplan.viewmodel.task.taskEstimateMinutePart

@Composable
internal fun taskEstimateDuration(ms: Long?): String {
    if (ms == null) return stringResource(R.string.task_estimate_unknown)
    val hours = ms / 3_600_000
    val minutes = taskEstimateMinutePart(ms)
    return when {
        hours == 0L -> stringResource(R.string.task_estimate_minutes, minutes)
        minutes == "0" -> stringResource(R.string.task_estimate_hours, hours.toString())
        else -> stringResource(R.string.task_estimate_hours_minutes, hours.toString(), minutes)
    }
}

@Composable
fun TaskEstimateText(estimate: TaskEstimate) {
    val label = taskEstimateDuration(estimate.milliseconds)
    Text(when {
        estimate.isManual -> stringResource(R.string.task_manual_estimate, label)
        estimate.isPartial -> stringResource(R.string.task_partial_estimate, label)
        else -> label
    }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
fun TaskOrderedSteps(keys: List<String>, enabled: Boolean, title: (String) -> String,
    onReorder: (List<String>, List<String>) -> Unit, scrollState: ScrollState? = null,
    viewport: Rect? = null, showReorderControls: Boolean = true, content: @Composable (String) -> Unit,
    extraActions: @Composable RowScope.(String) -> Unit = {}) {
    var preview by remember { mutableStateOf<List<String>?>(null) }
    var dragged by remember { mutableStateOf<String?>(null) }
    var dragTop by remember { mutableFloatStateOf(0f) }
    var pointerY by remember { mutableFloatStateOf(0f) }
    val heights = remember { mutableStateMapOf<String, Int>() }
    val spacing = with(LocalDensity.current) { 8.dp.toPx() }
    val edge = with(LocalDensity.current) { 72.dp.toPx() }
    val maxScrollSpeed = with(LocalDensity.current) { 600.dp.toPx() }
    val currentEnabled by rememberUpdatedState(enabled)
    val currentKeys by rememberUpdatedState(keys)
    val commit by rememberUpdatedState(onReorder)
    val currentViewport by rememberUpdatedState(viewport)
    LaunchedEffect(keys, enabled) {
        preview = null; dragged = null
        heights.keys.retainAll(keys.toSet())
    }
    fun updateDrag(delta: Float) {
        val id = dragged ?: return
        val order = preview ?: return
        val maxTop = (order.sumOf { heights[it] ?: 0 } + spacing * (order.size - 1) - (heights[id] ?: 0)).coerceAtLeast(0f)
        dragTop = (dragTop + delta).coerceIn(0f, maxTop)
        preview = taskStepDragOrder(order, id, dragTop, heights, spacing)
    }
    LaunchedEffect(dragged, scrollState) {
        if (dragged == null || scrollState == null) return@LaunchedEffect
        var previousFrame = withFrameNanos { it }
        while (dragged != null) {
            val frame = withFrameNanos { it }
            val seconds = ((frame - previousFrame) / 1_000_000_000f).coerceAtMost(0.05f)
            previousFrame = frame
            val bounds = currentViewport ?: continue
            val speed = when {
                pointerY < bounds.top + edge -> -((bounds.top + edge - pointerY) / edge).coerceIn(0f, 1f)
                pointerY > bounds.bottom - edge -> ((pointerY - bounds.bottom + edge) / edge).coerceIn(0f, 1f)
                else -> 0f
            }
            if (speed != 0f) updateDrag(scrollState.scrollBy(speed * maxScrollSpeed * seconds))
        }
    }
    fun move(key: String, offset: Int) {
        val index = keys.indexOf(key)
        val target = index + offset
        if (enabled && index >= 0 && target in keys.indices) onReorder(keys, keys.toMutableList().apply { add(target, removeAt(index)) })
    }
    val displayedOrder = preview?.takeIf { it.size == keys.size && it.toSet() == keys.toSet() } ?: keys
    LookaheadScope {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            displayedOrder.forEachIndexed { index, id -> key(id) {
                val isDragging = dragged == id
                val translation by animateFloatAsState(
                    targetValue = if (isDragging) dragTop - taskStepTop(displayedOrder, id, heights, spacing) else 0f,
                    animationSpec = if (isDragging) snap() else spring(stiffness = Spring.StiffnessMediumLow),
                    label = "subtask-drop",
                )
                val position = stringResource(R.string.task_step_position, index + 1, keys.size)
                Surface(shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    color = if (isDragging) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    shadowElevation = if (isDragging) 8.dp else 0.dp,
                    modifier = Modifier.fillMaxWidth().zIndex(if (isDragging || translation != 0f) 1f else 0f)
                        .animateBounds(this@LookaheadScope, boundsTransform = { _, _ ->
                            if (dragged == id) snap() else spring(stiffness = Spring.StiffnessMediumLow)
                        })
                        .graphicsLayer { translationY = translation }
                        .onSizeChanged { heights[id] = it.height }.testTag("task-step-$id")
                        .semantics { stateDescription = position }) {
                    Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        content(id)
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            if (showReorderControls) {
                                val dragLabel = stringResource(R.string.task_step_drag, title(id))
                                var handleY by remember { mutableFloatStateOf(0f) }
                                Icon(Icons.Rounded.Menu, dragLabel, modifier = Modifier.size(48.dp)
                                    .testTag("task-step-drag-$id")
                                    .onGloballyPositioned { handleY = it.positionInWindow().y }
                                    .pointerInput(id, keys, enabled) {
                                        if (enabled) detectDragGestures(
                                            onDragStart = { position ->
                                                preview = keys; dragged = id
                                                dragTop = taskStepTop(keys, id, heights, spacing)
                                                pointerY = handleY + position.y
                                            },
                                            onDragCancel = { preview = null; dragged = null },
                                            onDragEnd = {
                                                val order = preview
                                                if (currentEnabled && currentKeys == keys && order != null && order != keys) commit(keys, order)
                                                preview = null; dragged = null
                                            }, onDrag = { change, amount ->
                                                change.consume()
                                                pointerY += amount.y
                                                updateDrag(amount.y)
                                            })
                                    }.padding(12.dp))
                                IconButton(enabled = enabled && dragged == null && index > 0, onClick = { move(id, -1) }) {
                                    Icon(Icons.Rounded.ArrowUpward, stringResource(R.string.task_step_up, title(id)))
                                }
                                IconButton(enabled = enabled && dragged == null && index < keys.lastIndex, onClick = { move(id, 1) }) {
                                    Icon(Icons.Rounded.ArrowDownward, stringResource(R.string.task_step_down, title(id)))
                                }
                            }
                            Spacer(Modifier.weight(1f))
                            extraActions(id)
                        }
                    }
                }
            } }
        }
    }
}

@Composable
fun TaskStepEditorDialog(step: TaskStepDraft, onDismiss: () -> Unit, onSave: (TaskStepDraft) -> Unit, enabled: Boolean = true) {
    var title by rememberSaveable(step.key) { mutableStateOf(step.title) }
    var hours by rememberSaveable(step.key) { mutableStateOf(step.hours) }
    var minutes by rememberSaveable(step.key) { mutableStateOf(step.minutes) }
    var error by rememberSaveable(step.key) { mutableStateOf<String?>(null) }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).imePadding().padding(horizontal = 24.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center) {
            Surface(modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth(), shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerHigh, contentColor = MaterialTheme.colorScheme.onSurface) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.task_step_editor), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        if (!enabled) Text(stringResource(R.string.task_step_locked))
                        TaskTextField(title, { title = it; error = null }, label = { Text(stringResource(R.string.task_step_title)) },
                            modifier = Modifier.fillMaxWidth(), maxLines = 4)
                        Text(stringResource(R.string.task_estimate))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TaskTextField(hours, { hours = it; error = null }, label = { Text(stringResource(R.string.task_hours)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.weight(1f))
                            TaskTextField(minutes, { minutes = it; error = null }, label = { Text(stringResource(R.string.task_minutes)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.weight(1f))
                        }
                        Text(stringResource(R.string.task_step_estimate_help), style = MaterialTheme.typography.bodySmall)
                    }
                    Row(Modifier.align(Alignment.End), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)) {
                            Text(stringResource(R.string.task_cancel))
                        }
                        Button(enabled = enabled, shape = RoundedCornerShape(15.dp), onClick = {
                            val draft = step.copy(title = title, hours = hours, minutes = minutes)
                            try { draft.input(); onSave(draft) } catch (e: IllegalArgumentException) { error = e.message }
                        }) { Text(stringResource(R.string.task_step_save)) }
                    }
                }
            }
        }
    }
}
