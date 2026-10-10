package com.example.maiplan.home.task.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.maiplan.R
import com.example.maiplan.viewmodel.task.TaskRepeatEditorDraft
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun TaskSeriesFields(draft: TaskRepeatEditorDraft, enabled: Boolean, calendarEditable: Boolean,
    showCalendar: Boolean, allowOneOff: Boolean = true, onChange: (TaskRepeatEditorDraft) -> Unit) {
    if (showCalendar) {
        Text(stringResource(R.string.task_repeat), style = MaterialTheme.typography.titleMedium)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(0 to R.string.task_repeat_none, 1 to R.string.task_repeat_daily,
                2 to R.string.task_repeat_weekly, 3 to R.string.task_repeat_monthly).filter { allowOneOff || it.first != 0 }.forEach { (unit, label) ->
                FilterChip(selected = draft.unit == unit, enabled = enabled && calendarEditable,
                    onClick = { onChange(draft.copy(unit = unit)) }, label = { Text(stringResource(label)) })
            }
        }
        if (draft.unit != 0) {
            OutlinedTextField(draft.interval, { onChange(draft.copy(interval = it)) }, enabled = enabled && calendarEditable,
                singleLine = true, label = { Text(stringResource(R.string.task_repeat_interval)) }, modifier = Modifier.fillMaxWidth())
            if (draft.unit == 2) Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                DayOfWeek.entries.forEach { day ->
                    val bit = 1 shl (day.value - 1)
                    FilterChip(selected = draft.weekdays and bit != 0, enabled = enabled && calendarEditable,
                        onClick = { onChange(draft.copy(weekdays = draft.weekdays xor bit)) },
                        label = { Text(day.getDisplayName(TextStyle.SHORT, Locale.getDefault())) })
                }
            }
            OutlinedTextField(draft.endDate, { onChange(draft.copy(endDate = it)) }, enabled = enabled && calendarEditable,
                singleLine = true, label = { Text(stringResource(R.string.task_repeat_end)) }, modifier = Modifier.fillMaxWidth())
            Text(stringResource(R.string.task_repeat_independent), style = MaterialTheme.typography.bodySmall)
        }
    }
    if (draft.unit != 0) {
        Row { Switch(draft.relative, { onChange(draft.copy(relative = it)) }, enabled = enabled, modifier = Modifier.testTag("task-relative-reminder-enabled"))
            Text(stringResource(R.string.task_repeat_reminder), modifier = Modifier.padding(12.dp)) }
        if (draft.relative) {
            OutlinedTextField(draft.leadDays, { onChange(draft.copy(leadDays = it)) }, enabled = enabled,
                label = { Text(stringResource(R.string.task_repeat_lead)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(draft.time, { onChange(draft.copy(time = it)) }, enabled = enabled,
                label = { Text(stringResource(R.string.task_repeat_clock)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(draft.zone, { onChange(draft.copy(zone = it)) }, enabled = enabled,
                label = { Text(stringResource(R.string.task_reminder_zone)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(draft.message, { onChange(draft.copy(message = it)) }, enabled = enabled,
                label = { Text(stringResource(R.string.task_reminder_message)) }, modifier = Modifier.fillMaxWidth())
            TaskReminderAccessWarnings()
        }
    }
}

@Composable
fun TaskSeriesDeleteDialog(enabled: Boolean, onDismiss: () -> Unit, onOccurrence: () -> Unit, onAll: () -> Unit) {
    AlertDialog(onDismissRequest = { if (enabled) onDismiss() }, title = { Text(stringResource(R.string.task_delete)) },
        text = { Text(stringResource(R.string.task_delete_series_help)) },
        confirmButton = { Column {
            TextButton(enabled = enabled, onClick = onOccurrence) { Text(stringResource(R.string.task_delete_occurrence)) }
            TextButton(enabled = enabled, onClick = onAll) { Text(stringResource(R.string.task_delete_all_occurrences)) }
        } }, dismissButton = { TextButton(enabled = enabled, onClick = onDismiss) { Text(stringResource(R.string.task_keep_editing)) } })
}
