package com.example.maiplan.components

import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.example.maiplan.R
import com.example.maiplan.theme.AppThemeManager
import com.example.maiplan.theme.LocalAppDarkTheme

@Composable
internal fun rememberEditorBackAction(
    hasChanges: Boolean,
    saving: Boolean = false,
    onBack: () -> Unit,
): () -> Unit {
    var showDiscard by rememberSaveable { mutableStateOf(false) }
    val requestBack: () -> Unit = {
        if (!saving) {
            if (hasChanges) showDiscard = true else onBack()
        }
    }
    BackHandler(onBack = requestBack)
    if (showDiscard) {
        EditorDiscardDialog(
            onDismiss = { showDiscard = false },
            onDiscard = {
                showDiscard = false
                onBack()
            },
            enabled = !saving,
        )
    }
    return requestBack
}

@Composable
internal fun EditorDiscardDialog(
    onDismiss: () -> Unit,
    onDiscard: () -> Unit,
    confirmLabel: Int = R.string.task_discard,
    enabled: Boolean = true,
) {
    val foreground = MaterialTheme.colorScheme.onBackground
    val actionColor = if (LocalAppDarkTheme.current) AppThemeManager.selectedTheme.primaryLight
        else MaterialTheme.colorScheme.primary
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.task_discard_title)) },
        text = { Text(stringResource(R.string.task_discard_body)) },
        titleContentColor = foreground,
        textContentColor = foreground.copy(alpha = 0.8f),
        confirmButton = {
            TextButton(enabled = enabled, onClick = onDiscard,
                colors = ButtonDefaults.textButtonColors(contentColor = actionColor)) {
                Text(stringResource(confirmLabel))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = actionColor)) {
                Text(stringResource(R.string.task_keep_editing))
            }
        },
    )
}
