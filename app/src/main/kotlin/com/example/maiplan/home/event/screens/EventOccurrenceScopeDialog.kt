package com.example.maiplan.home.event.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.maiplan.R

@Composable
internal fun EventOccurrenceScopeDialog(
    deleting: Boolean,
    enabled: Boolean,
    onDismiss: () -> Unit,
    onCurrent: () -> Unit,
    onAll: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (enabled) onDismiss() },
        title = { Text(stringResource(if (deleting) R.string.event_delete_occurrences_title else R.string.event_edit_occurrences_title)) },
        text = { Text(stringResource(R.string.event_occurrences_scope_help)) },
        confirmButton = {
            Column {
                TextButton(enabled = enabled, onClick = onCurrent) {
                    Text(stringResource(if (deleting) R.string.event_delete_current_occurrence else R.string.event_edit_current_occurrence))
                }
                TextButton(enabled = enabled, onClick = onAll) {
                    Text(stringResource(if (deleting) R.string.event_delete_all_occurrences else R.string.event_edit_all_occurrences))
                }
            }
        },
        dismissButton = { TextButton(enabled = enabled, onClick = onDismiss) { Text(stringResource(R.string.event_occurrence_cancel)) } },
    )
}
