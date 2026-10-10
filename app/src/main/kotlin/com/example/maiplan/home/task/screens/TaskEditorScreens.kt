package com.example.maiplan.home.task.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.maiplan.R
import com.example.maiplan.components.EditorDiscardDialog
import com.example.maiplan.home.event.screens.CategoryPickerOverlay
import com.example.maiplan.home.event.screens.EventCategoryField
import com.example.maiplan.home.event.screens.EventDatePickerOverlay
import com.example.maiplan.home.event.screens.EventOptionRow
import com.example.maiplan.home.event.screens.EventSelectionField
import com.example.maiplan.home.event.screens.EventTimePickerOverlay
import com.example.maiplan.home.event.screens.ZonePickerOverlay
import com.example.maiplan.utils.LocalAdaptiveLayout
import com.example.maiplan.viewmodel.task.*
import com.example.maiplan.repository.task.*
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskEditorScreen(editing: Boolean, viewModel: TaskEditorViewModel, collectionViewModel: TaskViewModel,
    onBack: () -> Unit, onSaved: (Long) -> Unit) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val collection by collectionViewModel.metadata.collectAsStateWithLifecycle()
    var discard by rememberSaveable { mutableStateOf(false) }
    var reload by rememberSaveable { mutableStateOf(false) }
    var pickingDate by rememberSaveable { mutableStateOf<String?>(null) }
    var pickingTime by rememberSaveable { mutableStateOf<String?>(null) }
    var pickingZone by rememberSaveable { mutableStateOf<String?>(null) }
    var pickingCategory by rememberSaveable { mutableStateOf(false) }
    var editingStep by rememberSaveable { mutableStateOf<String?>(null) }
    var removingStep by rememberSaveable { mutableStateOf<String?>(null) }
    val draft = state.draft
    val selectedCategory = collection.categories.firstOrNull { it.categoryLocalId == draft.categoryId }
    val templateEditing = state.futureScope || !editing && state.repeat.unit != 0
    val reminderMode = when {
        state.repeat.unit != 0 && state.repeat.relative -> "relative"
        !templateEditing && state.reminder.enabled -> "absolute"
        else -> "none"
    }
    val back = {
        when {
            pickingDate != null -> pickingDate = null
            pickingTime != null -> pickingTime = null
            pickingZone != null -> pickingZone = null
            pickingCategory -> pickingCategory = false
            !state.saving -> if (state.dirty) discard = true else onBack()
        }
    }
    BackHandler(onBack = back)
    LaunchedEffect(state.savedId) {
        state.savedId?.let { id ->
            state.warning?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
            onSaved(id)
        }
    }
    Box(Modifier.fillMaxSize()) {
        Scaffold(containerColor = Color.Transparent, topBar = { TaskTopBar(stringResource(if (editing) R.string.task_edit else R.string.task_new), back) },
            bottomBar = {
                if (!state.loading && !state.unavailable && !state.loadFailed) Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
                    Box(Modifier.fillMaxWidth().navigationBarsPadding().imePadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
                        Button(onClick = { viewModel.save() }, enabled = !state.saving && state.savedId == null && !collection.loading && collection.error == null,
                            shape = RoundedCornerShape(15.dp),
                            modifier = Modifier.widthIn(max = LocalAdaptiveLayout.current.formMaxWidth).fillMaxWidth().heightIn(min = 56.dp)) {
                            if (state.saving) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary)
                            else Icon(Icons.Rounded.Check, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(9.dp))
                            Text(stringResource(if (state.saving) R.string.task_saving else if (state.stale) R.string.task_review_merge else R.string.task_save),
                                fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }) { padding ->
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
                when {
                    state.loading -> TaskLoadState(true, "")
                    state.loadFailed -> TaskLoadState(false, state.error.orEmpty(), viewModel::retry)
                    state.unavailable -> TaskLoadState(false, stringResource(R.string.task_unavailable) + "\n" + stringResource(R.string.task_unavailable_help))
                    else -> Column(Modifier.widthIn(max = LocalAdaptiveLayout.current.formMaxWidth).fillMaxWidth()
                        .verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        TaskHeading(stringResource(if (editing) R.string.task_edit_heading else R.string.task_create_heading),
                            stringResource(if (editing) R.string.task_edit_subtitle else R.string.task_create_subtitle))
                        TaskEditorPreview(state, selectedCategory, templateEditing)
                        state.error?.let { TaskMessage(it) }
                        collection.error?.let { TaskMessage(it); TextButton(onClick = collectionViewModel::retry) { Text(stringResource(R.string.task_retry)) } }
                        if (state.stale) {
                            TaskMessage(stringResource(R.string.task_stale))
                            TextButton(enabled = !state.saving, onClick = { if (state.dirty) reload = true else viewModel.loadLatest() }) {
                                Text(stringResource(R.string.task_reload))
                            }
                        }
                        TaskSection(stringResource(R.string.task_details_title), stringResource(R.string.task_details_subtitle), Icons.Rounded.TaskAlt) {
                            TaskTextField(value = draft.title, onValueChange = { viewModel.edit(draft.copy(title = it)) },
                                label = { Text(stringResource(R.string.task_field_title)) }, enabled = !state.saving,
                                leadingIcon = { Icon(Icons.Rounded.Title, null) }, modifier = Modifier.fillMaxWidth(), minLines = 1, maxLines = 4)
                            TaskTextField(value = draft.description, onValueChange = { viewModel.edit(draft.copy(description = it)) },
                                label = { Text(stringResource(R.string.task_description)) }, enabled = !state.saving,
                                leadingIcon = { Icon(Icons.Rounded.Description, null) }, modifier = Modifier.fillMaxWidth(), minLines = 3, maxLines = 10)
                        }
                        TaskSection(stringResource(R.string.task_planning_title), stringResource(R.string.task_planning_subtitle), Icons.Rounded.Event) {
                            if (draft.categoryId != null && !collection.loading && collection.error == null &&
                                collection.categories.none { it.categoryLocalId == draft.categoryId }) {
                                TaskMessage(stringResource(R.string.task_category_missing))
                            }
                            EventCategoryField(selectedCategory, enabled = !state.saving && !collection.loading && collection.error == null) {
                                pickingCategory = true
                            }
                            if (draft.categoryId != null) TextButton(enabled = !state.saving, onClick = { viewModel.edit(draft.copy(categoryId = null)) }) {
                                Text(stringResource(R.string.task_clear_category))
                            }
                            EventSelectionField(stringResource(if (templateEditing) R.string.task_repeat_anchor else R.string.task_planned_date),
                                taskEditorDateText(draft.date, stringResource(R.string.task_unscheduled)), Icons.Rounded.CalendarMonth,
                                enabled = !state.saving, onClick = { pickingDate = "planned" })
                            Text(stringResource(R.string.task_date_hint), style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (draft.date.isNotEmpty() && state.repeat.unit == 0 && !state.isSeries) TextButton(enabled = !state.saving,
                                onClick = { viewModel.edit(draft.copy(date = "")) }) { Text(stringResource(R.string.task_clear_date)) }
                        }
                        TaskSection(stringResource(R.string.task_estimate), stringResource(R.string.task_estimate_help), Icons.Rounded.Schedule) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                TaskTextField(value = draft.minutes, onValueChange = { viewModel.edit(draft.copy(minutes = it)) },
                                    label = { Text(stringResource(R.string.task_minutes)) }, enabled = !state.saving, singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f))
                                TaskTextField(value = draft.seconds, onValueChange = { viewModel.edit(draft.copy(seconds = it)) },
                                    label = { Text(stringResource(R.string.task_seconds)) }, enabled = !state.saving, singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f))
                            }
                            if (draft.minutes.isNotEmpty() || draft.seconds.isNotEmpty()) TextButton(enabled = !state.saving,
                                onClick = { viewModel.edit(draft.copy(minutes = "", seconds = "")) }) { Text(stringResource(R.string.task_clear_estimate)) }
                            runCatching { effectiveTaskEstimate(draft.copy(title = "Estimate").content().estimatedMilliseconds,
                                state.steps.map { it.input().estimatedMilliseconds }) }.getOrNull()?.let { TaskEstimateText(it) }
                        }
                        if (state.isSeries) {
                            TaskSection(stringResource(R.string.task_edit_scope), icon = Icons.Rounded.Edit) {
                                TaskFilterChip(selected = !state.futureScope, enabled = !state.saving,
                                    onClick = { viewModel.chooseFutureScope(false) }, label = { Text(stringResource(R.string.task_edit_occurrence)) })
                                TaskFilterChip(selected = state.futureScope, enabled = !state.saving,
                                    onClick = { viewModel.chooseFutureScope(true) }, label = { Text(stringResource(R.string.task_edit_future)) })
                                if (state.futureScope) EventSelectionField(stringResource(R.string.task_edit_cutoff),
                                    taskEditorDateText(state.cutoff, stringResource(R.string.task_choose_date)), Icons.Rounded.CalendarMonth,
                                    enabled = !state.saving, onClick = { pickingDate = "cutoff" })
                            }
                        }
                        if (!editing || state.isSeries || state.repeat.unit != 0) TaskSection(stringResource(R.string.task_repeat), icon = Icons.Rounded.Repeat) {
                            TaskSeriesFields(state.repeat, !state.saving, !editing || state.futureScope,
                                !editing || state.isSeries, allowOneOff = !editing,
                                onChooseEndDate = { pickingDate = "repeat" }, onChange = viewModel::editRepeat)
                        }
                        TaskSection(
                            stringResource(R.string.task_reminder), icon = Icons.Rounded.NotificationsNone) {
                            val options = buildList {
                                add("none" to stringResource(R.string.task_none))
                                if (state.repeat.unit != 0) add("relative" to stringResource(R.string.task_reminder_before_task))
                                if (!templateEditing) add("absolute" to stringResource(R.string.task_reminder_at_date))
                            }
                            EventOptionRow(options, null, { it.second }, isSelected = { it.first == reminderMode },
                                isEnabled = { !state.saving }) { (mode, _) ->
                                viewModel.editRepeat(state.repeat.copy(relative = mode == "relative"))
                                viewModel.editReminder(if (mode == "absolute") state.reminder.enable() else state.reminder.copy(enabled = false))
                            }
                            if (reminderMode == "relative") TaskRelativeReminderFields(state.repeat, !state.saving,
                                onChooseTime = { pickingTime = "relative" }, onChooseZone = { pickingZone = "relative" },
                                onChange = viewModel::editRepeat)
                            else if (!templateEditing) TaskReminderFields(state.reminder, !state.saving,
                                onChooseDate = { pickingDate = "absolute" }, onChooseTime = { pickingTime = "absolute" },
                                onChooseZone = { pickingZone = "absolute" }, onChange = viewModel::editReminder)
                        }
                        TaskSection(stringResource(R.string.task_steps), stringResource(R.string.task_step_inherit), Icons.Rounded.Checklist) {
                            if (!state.checklistEditable) TaskMessage(stringResource(R.string.task_step_locked))
                            if (state.steps.isEmpty()) Text(stringResource(R.string.task_step_empty), style = MaterialTheme.typography.bodySmall)
                            else Text(pluralStringResource(R.plurals.task_progress_count, state.steps.size,
                                state.steps.count { TaskStatus.fromCode(it.status).isTerminal }, state.steps.size))
                            val stepsByKey = state.steps.associateBy { it.key }
                            TaskOrderedSteps(state.steps.map { it.key }, state.checklistEditable && !state.saving,
                                title = { stepsByKey.getValue(it).title },
                                onReorder = { _, order -> viewModel.editSteps(order.map { stepsByKey.getValue(it) }) },
                                content = { id ->
                                    val step = stepsByKey.getValue(id)
                                    Text(step.title, style = MaterialTheme.typography.titleSmall)
                                    TaskStatusBadge(TaskStatus.fromCode(step.status))
                                    TaskEstimateText(TaskEstimate(step.input().estimatedMilliseconds, false, false))
                                }, extraActions = { id ->
                                    IconButton(enabled = state.checklistEditable && !state.saving, onClick = { editingStep = id }) {
                                        Icon(Icons.Rounded.Edit, stringResource(R.string.task_step_edit, stepsByKey.getValue(id).title))
                                    }
                                    IconButton(enabled = state.checklistEditable && !state.saving, onClick = { removingStep = id }) {
                                        Icon(Icons.Rounded.DeleteOutline, stringResource(R.string.task_step_remove, stepsByKey.getValue(id).title))
                                    }
                                })
                            OutlinedButton(enabled = state.checklistEditable && !state.saving, onClick = { editingStep = UUID.randomUUID().toString() },
                                shape = RoundedCornerShape(15.dp), modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                                Icon(Icons.Rounded.Add, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.task_step_add))
                            }
                        }
                    }
                }
            }
        }
        pickingDate?.let { field ->
            val value = when (field) {
                "planned" -> draft.date
                "repeat" -> state.repeat.endDate.ifBlank { draft.date }
                "cutoff" -> state.cutoff
                else -> state.reminder.date
            }
            EventDatePickerOverlay(initialDate = runCatching { LocalDate.parse(value) }.getOrNull()
                ?.takeIf { it.year in 1..9999 } ?: LocalDate.now(),
                yearRange = 1..9999,
                onDateSelected = { selected ->
                    if (!state.saving) when (field) {
                        "planned" -> viewModel.edit(draft.copy(date = selected.toString()))
                        "repeat" -> viewModel.editRepeat(state.repeat.copy(endDate = selected.toString()))
                        "cutoff" -> viewModel.editCutoff(selected.toString())
                        else -> viewModel.editReminder(state.reminder.copy(date = selected.toString()))
                    }
                }, onDismiss = { pickingDate = null })
        }
        pickingTime?.let { field ->
            EventTimePickerOverlay(initialTime = runCatching { LocalTime.parse(if (field == "relative") state.repeat.time else state.reminder.time) }
                .getOrDefault(LocalTime.of(9, 0)),
                onTimeSelected = { selected ->
                    if (field == "relative") viewModel.editRepeat(state.repeat.copy(time = selected.toString()))
                    else viewModel.editReminder(state.reminder.copy(time = selected.toString()))
                }, onDismiss = { pickingTime = null })
        }
        pickingZone?.let { field ->
            ZonePickerOverlay(selectedZone = if (field == "relative") state.repeat.zone else state.reminder.zoneId,
                onZoneSelected = {
                    if (field == "relative") viewModel.editRepeat(state.repeat.copy(zone = it))
                    else viewModel.editReminder(state.reminder.copy(zoneId = it))
                    pickingZone = null
                }, onDismiss = { pickingZone = null })
        }
        if (pickingCategory) CategoryPickerOverlay(collection.categories, selectedCategory,
            onCategorySelected = { viewModel.edit(draft.copy(categoryId = it?.categoryLocalId)); pickingCategory = false },
            onDismiss = { pickingCategory = false })
    }
    editingStep?.let { id -> TaskStepEditorDialog(state.steps.find { it.key == id } ?: TaskStepDraft(key = id),
        onDismiss = { editingStep = null }, onSave = { step ->
            val rows = if (state.steps.any { it.key == id }) state.steps.map { if (it.key == id) step else it } else state.steps + step
            viewModel.editSteps(rows); editingStep = null
        }, enabled = state.checklistEditable && !state.saving) }
    removingStep?.let { id -> AlertDialog(onDismissRequest = { removingStep = null },
        title = { Text(stringResource(R.string.task_step_remove_title)) },
        text = { Text(stringResource(R.string.task_step_remove_body, state.steps.find { it.key == id }?.title.orEmpty())) },
        confirmButton = { TextButton(enabled = !state.saving && state.checklistEditable, onClick = {
            viewModel.editSteps(state.steps.filter { it.key != id }); removingStep = null
        }) { Text(stringResource(R.string.task_step_remove_confirm)) } },
        dismissButton = { TextButton(onClick = { removingStep = null }) { Text(stringResource(R.string.task_cancel)) } }) }
    if (discard || reload) EditorDiscardDialog(onDismiss = { discard = false; reload = false },
        onDiscard = {
            if (reload) viewModel.loadLatest() else onBack()
            discard = false; reload = false
        }, confirmLabel = if (reload) R.string.task_reload else R.string.task_discard,
        enabled = !state.saving)
    state.merge?.let { merge -> AlertDialog(onDismissRequest = viewModel::cancelMerge,
        title = { Text(stringResource(R.string.task_merge_title)) },
        text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.task_merge_body, merge.changedFields.joinToString().ifEmpty { stringResource(R.string.task_none) }))
            if (merge.collisions.isNotEmpty()) Text(stringResource(R.string.task_merge_collisions, merge.collisions.joinToString()))
        } }, confirmButton = { TextButton(onClick = { viewModel.save(merge) }) { Text(stringResource(R.string.task_merge_save)) } },
        dismissButton = { TextButton(onClick = viewModel::cancelMerge) { Text(stringResource(R.string.task_keep_editing)) } }) }
}
