package com.example.maiplan.home.task.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.maiplan.R
import com.example.maiplan.home.event.screens.EventOptionRow
import com.example.maiplan.home.event.screens.EventSelectionField
import com.example.maiplan.viewmodel.task.TaskRepeatEditorDraft
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun TaskSeriesFields(draft: TaskRepeatEditorDraft, enabled: Boolean, calendarEditable: Boolean,
    showCalendar: Boolean, allowOneOff: Boolean = true, onChooseEndDate: () -> Unit,
    onChange: (TaskRepeatEditorDraft) -> Unit) {
    if (showCalendar) {
        val options = listOf(0 to stringResource(R.string.task_repeat_none), 1 to stringResource(R.string.task_repeat_daily),
            2 to stringResource(R.string.task_repeat_weekly), 3 to stringResource(R.string.task_repeat_monthly))
            .filter { allowOneOff || it.first != 0 }
        EventOptionRow(options, null, { it.second }, isSelected = { it.first == draft.unit },
            isEnabled = { enabled && calendarEditable }) { (unit, _) ->
            onChange(draft.copy(unit = unit, relative = unit != 0 && draft.relative))
        }
        if (draft.unit != 0) {
            val intervalLabel = stringResource(when (draft.unit) {
                1 -> R.string.task_repeat_every_days
                2 -> R.string.task_repeat_every_weeks
                else -> R.string.task_repeat_every_months
            })
            TaskTextField(draft.interval, { onChange(draft.copy(interval = it.filter(Char::isDigit))) }, enabled = enabled && calendarEditable,
                singleLine = true, label = { Text(intervalLabel) }, leadingIcon = { Icon(Icons.Rounded.Repeat, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            if (draft.unit == 2) {
                Text(stringResource(R.string.task_repeat_weekdays), style = MaterialTheme.typography.labelLarge)
                EventOptionRow(DayOfWeek.entries, null, { it.getDisplayName(TextStyle.SHORT, Locale.getDefault()) },
                    isSelected = { draft.weekdays and (1 shl (it.value - 1)) != 0 },
                    isEnabled = { enabled && calendarEditable }) { day ->
                    onChange(draft.copy(weekdays = draft.weekdays xor (1 shl (day.value - 1))))
                }
            }
            EventSelectionField(stringResource(R.string.task_repeat_end),
                taskEditorDateText(draft.endDate, stringResource(R.string.task_repeat_no_end)), Icons.Rounded.CalendarMonth,
                enabled = enabled && calendarEditable, onClick = onChooseEndDate)
            if (draft.endDate.isNotBlank()) TextButton(enabled = enabled && calendarEditable,
                onClick = { onChange(draft.copy(endDate = "")) }) {
                Text(stringResource(R.string.task_repeat_clear_end))
            }
            Text(stringResource(R.string.task_repeat_independent), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
