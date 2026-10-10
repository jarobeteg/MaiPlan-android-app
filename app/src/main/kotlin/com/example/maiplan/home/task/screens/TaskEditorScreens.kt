package com.example.maiplan.home.task.screens

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.maiplan.R
import com.example.maiplan.category.CategoryActivity
import com.example.maiplan.utils.LocalAdaptiveLayout
import com.example.maiplan.viewmodel.task.*
import com.example.maiplan.repository.task.*
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditorScreen(editing: Boolean, viewModel: TaskEditorViewModel, collectionViewModel: TaskViewModel,
    onBack: () -> Unit, onSaved: (Long) -> Unit) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val collection by collectionViewModel.metadata.collectAsStateWithLifecycle()
    var discard by rememberSaveable { mutableStateOf(false) }
    var reload by rememberSaveable { mutableStateOf(false) }
    var datePicker by rememberSaveable { mutableStateOf(false) }
    var categoryMenu by remember { mutableStateOf(false) }
    var editingStep by rememberSaveable { mutableStateOf<String?>(null) }
    var removingStep by rememberSaveable { mutableStateOf<String?>(null) }
    val draft = state.draft
    val back = { if (!state.saving) { if (state.dirty) discard = true else onBack() } }
    BackHandler(onBack = back)
    LaunchedEffect(state.savedId) {
        state.savedId?.let { id ->
            state.warning?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
            onSaved(id)
        }
    }
    Scaffold(topBar = { TaskTopBar(stringResource(if (editing) R.string.task_edit else R.string.task_new), back) },
        bottomBar = {
            if (!state.loading && !state.unavailable && !state.loadFailed) Box(Modifier.fillMaxWidth().navigationBarsPadding().imePadding()
                .padding(horizontal = 16.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
                Button(onClick = { viewModel.save() }, enabled = !state.saving && state.savedId == null && !collection.loading && collection.error == null,
                    modifier = Modifier.widthIn(max = LocalAdaptiveLayout.current.formMaxWidth).fillMaxWidth()) {
                    Text(stringResource(if (state.saving) R.string.task_saving else if (state.stale) R.string.task_review_merge else R.string.task_save))
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
                    state.error?.let { TaskMessage(it) }
                    collection.error?.let { TaskMessage(it); TextButton(onClick = collectionViewModel::retry) { Text(stringResource(R.string.task_retry)) } }
                    if (state.stale) {
                        TaskMessage(stringResource(R.string.task_stale))
                        TextButton(enabled = !state.saving, onClick = { if (state.dirty) reload = true else viewModel.loadLatest() }) {
                            Text(stringResource(R.string.task_reload))
                        }
                    }
                    OutlinedTextField(value = draft.title, onValueChange = { viewModel.edit(draft.copy(title = it)) },
                        label = { Text(stringResource(R.string.task_field_title)) }, enabled = !state.saving,
                        modifier = Modifier.fillMaxWidth(), minLines = 1, maxLines = 4)
                    OutlinedTextField(value = draft.description, onValueChange = { viewModel.edit(draft.copy(description = it)) },
                        label = { Text(stringResource(R.string.task_description)) }, enabled = !state.saving,
                        modifier = Modifier.fillMaxWidth(), minLines = 3, maxLines = 10)
                    Text(stringResource(R.string.task_category_field), style = MaterialTheme.typography.titleMedium)
                    if (draft.categoryId != null && !collection.loading && collection.error == null &&
                        collection.categories.none { it.categoryLocalId == draft.categoryId }) {
                        TaskMessage(stringResource(R.string.task_category_missing))
                    }
                    Box {
                        OutlinedButton(enabled = !state.saving && !collection.loading && collection.error == null, onClick = { categoryMenu = true }) {
                            Text(collection.categories.find { it.categoryLocalId == draft.categoryId }?.name ?: stringResource(R.string.uncategorized))
                        }
                        DropdownMenu(expanded = categoryMenu, onDismissRequest = { categoryMenu = false }) {
                            DropdownMenuItem(text = { Text(stringResource(R.string.uncategorized)) }, onClick = {
                                categoryMenu = false; viewModel.edit(draft.copy(categoryId = null)) })
                            collection.categories.forEach { category -> DropdownMenuItem(text = { Text(category.name) }, onClick = {
                                categoryMenu = false; viewModel.edit(draft.copy(categoryId = category.categoryLocalId)) }) }
                        }
                    }
                    if (draft.categoryId != null) TextButton(enabled = !state.saving, onClick = { viewModel.edit(draft.copy(categoryId = null)) }) {
                        Text(stringResource(R.string.task_clear_category))
                    }
                    TextButton(enabled = !state.saving, onClick = { context.startActivity(Intent(context, CategoryActivity::class.java)) }) {
                        Text(stringResource(R.string.task_categories))
                    }
                    OutlinedTextField(value = draft.date, onValueChange = { viewModel.edit(draft.copy(date = it)) },
                        label = { Text(stringResource(if ((!editing && state.repeat.unit != 0) || state.futureScope) R.string.task_repeat_anchor else R.string.task_planned_date)) }, supportingText = { Text(stringResource(R.string.task_date_hint)) },
                        enabled = !state.saving, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(enabled = !state.saving, onClick = { datePicker = true }) { Text(stringResource(R.string.task_choose_date)) }
                        if (draft.date.isNotEmpty() && state.repeat.unit == 0 && !state.isSeries) TextButton(enabled = !state.saving, onClick = { viewModel.edit(draft.copy(date = "")) }) { Text(stringResource(R.string.task_clear_date)) }
                    }
                    Text(stringResource(R.string.task_estimate), style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(value = draft.minutes, onValueChange = { viewModel.edit(draft.copy(minutes = it)) },
                            label = { Text(stringResource(R.string.task_minutes)) }, enabled = !state.saving, singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f))
                        OutlinedTextField(value = draft.seconds, onValueChange = { viewModel.edit(draft.copy(seconds = it)) },
                            label = { Text(stringResource(R.string.task_seconds)) }, enabled = !state.saving, singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f))
                    }
                    Text(stringResource(R.string.task_estimate_help), style = MaterialTheme.typography.bodySmall)
                    if (draft.minutes.isNotEmpty() || draft.seconds.isNotEmpty()) TextButton(enabled = !state.saving,
                        onClick = { viewModel.edit(draft.copy(minutes = "", seconds = "")) }) { Text(stringResource(R.string.task_clear_estimate)) }
                    runCatching { effectiveTaskEstimate(draft.copy(title = "Estimate").content().estimatedMilliseconds,
                        state.steps.map { it.input().estimatedMilliseconds }) }.getOrNull()?.let { TaskEstimateText(it) }
                    HorizontalDivider()
                    if (state.isSeries) {
                        Text(stringResource(R.string.task_edit_scope), style = MaterialTheme.typography.titleMedium)
                        FilterChip(selected = !state.futureScope, enabled = !state.saving,
                            onClick = { viewModel.chooseFutureScope(false) }, label = { Text(stringResource(R.string.task_edit_occurrence)) })
                        FilterChip(selected = state.futureScope, enabled = !state.saving,
                            onClick = { viewModel.chooseFutureScope(true) }, label = { Text(stringResource(R.string.task_edit_future)) })
                        if (state.futureScope) OutlinedTextField(state.cutoff, viewModel::editCutoff, enabled = !state.saving,
                            label = { Text(stringResource(R.string.task_edit_cutoff)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    }
                    TaskSeriesFields(state.repeat, !state.saving, !editing || state.futureScope,
                        !editing || state.isSeries, allowOneOff = !editing, onChange = viewModel::editRepeat)
                    if (!state.repeat.relative && !(state.futureScope || !editing && state.repeat.unit != 0))
                        TaskReminderFields(state.reminder, !state.saving, viewModel::editReminder)
                    HorizontalDivider()
                    Text(stringResource(R.string.task_steps), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.task_step_inherit), style = MaterialTheme.typography.bodySmall)
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
                            Text(taskStatusLabel(TaskStatus.fromCode(step.status)), style = MaterialTheme.typography.labelMedium)
                            TaskEstimateText(TaskEstimate(step.input().estimatedMilliseconds, false, false))
                        }, extraActions = { id ->
                            IconButton(enabled = state.checklistEditable && !state.saving, onClick = { editingStep = id }) {
                                Icon(Icons.Rounded.Edit, stringResource(R.string.task_step_edit, stepsByKey.getValue(id).title))
                            }
                            IconButton(enabled = state.checklistEditable && !state.saving, onClick = { removingStep = id }) {
                                Icon(Icons.Rounded.DeleteOutline, stringResource(R.string.task_step_remove, stepsByKey.getValue(id).title))
                            }
                        })
                    OutlinedButton(enabled = state.checklistEditable && !state.saving, onClick = { editingStep = UUID.randomUUID().toString() }) {
                        Text(stringResource(R.string.task_step_add))
                    }
                }
            }
        }
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
    if (discard || reload) AlertDialog(onDismissRequest = { discard = false; reload = false },
        title = { Text(stringResource(R.string.task_discard_title)) }, text = { Text(stringResource(R.string.task_discard_body)) },
        confirmButton = { TextButton(onClick = {
            if (reload) viewModel.loadLatest() else onBack()
            discard = false; reload = false
        }) { Text(stringResource(if (reload) R.string.task_reload else R.string.task_discard)) } },
        dismissButton = { TextButton(onClick = { discard = false; reload = false }) { Text(stringResource(R.string.task_keep_editing)) } })
    state.merge?.let { merge -> AlertDialog(onDismissRequest = viewModel::cancelMerge,
        title = { Text(stringResource(R.string.task_merge_title)) },
        text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.task_merge_body, merge.changedFields.joinToString().ifEmpty { stringResource(R.string.task_none) }))
            if (merge.collisions.isNotEmpty()) Text(stringResource(R.string.task_merge_collisions, merge.collisions.joinToString()))
        } }, confirmButton = { TextButton(onClick = { viewModel.save(merge) }) { Text(stringResource(R.string.task_merge_save)) } },
        dismissButton = { TextButton(onClick = viewModel::cancelMerge) { Text(stringResource(R.string.task_keep_editing)) } }) }
    if (datePicker) {
        val initial = runCatching { LocalDate.parse(draft.date) }.getOrNull()?.takeIf { it.year in 1..9999 } ?: LocalDate.now()
        val picker = rememberDatePickerState(initialSelectedDateMillis = initial.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(), yearRange = 1..9999)
        DatePickerDialog(onDismissRequest = { datePicker = false },
            confirmButton = { TextButton(enabled = picker.selectedDateMillis != null, onClick = {
                picker.selectedDateMillis?.let { viewModel.edit(draft.copy(date = java.time.Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toString())) }
                datePicker = false
            }) { Text(stringResource(R.string.task_confirm)) } },
            dismissButton = { TextButton(onClick = { datePicker = false }) { Text(stringResource(R.string.task_cancel)) } }) { DatePicker(picker) }
    }
}
