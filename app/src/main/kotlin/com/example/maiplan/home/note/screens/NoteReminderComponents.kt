package com.example.maiplan.home.note.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Message
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.maiplan.R
import com.example.maiplan.home.event.screens.EventEditorError
import com.example.maiplan.home.event.screens.EventEditorTextField
import com.example.maiplan.home.event.screens.EventOptionRow
import com.example.maiplan.home.event.screens.EventSelectionField
import com.example.maiplan.theme.LocalAppDarkTheme
import com.example.maiplan.utils.notifications.AlarmScheduler
import com.example.maiplan.utils.notifications.NotificationHelper
import com.example.maiplan.utils.notifications.enqueueEventAlarmRecovery
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
internal fun NoteReminderFields(
    value: LocalDateTime?,
    message: String,
    onEnabledChange: (Boolean) -> Unit,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit,
    onMessageChange: (String) -> Unit,
) {
    val noneLabel = stringResource(R.string.note_reminder_none)
    val atDateLabel = stringResource(R.string.note_reminder_at_date)
    EventOptionRow(
        options = listOf(false, true),
        selected = value != null,
        label = { if (it) atDateLabel else noneLabel },
        onSelect = onEnabledChange,
    )
    if (value != null) {
        Spacer(Modifier.height(14.dp))
        EventSelectionField(
            label = stringResource(R.string.note_reminder_date),
            value = value.toLocalDate().format(DateTimeFormatter.ofPattern("EEE, MMM d, yyyy")),
            icon = Icons.Rounded.CalendarMonth,
            onClick = onDateClick,
        )
        Spacer(Modifier.height(12.dp))
        EventSelectionField(
            label = stringResource(R.string.note_reminder_time),
            value = value.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")),
            icon = Icons.Rounded.AccessTime,
            onClick = onTimeClick,
        )
        Spacer(Modifier.height(14.dp))
        EventEditorTextField(
            value = message,
            onValueChange = { if (it.length <= 512) onMessageChange(it) },
            label = stringResource(R.string.note_reminder_message),
            icon = Icons.AutoMirrored.Rounded.Message,
            singleLine = false,
            imeAction = ImeAction.Default,
        )
        NoteReminderAccessWarnings()
    }
}

@Composable
private fun NoteReminderAccessWarnings() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var notificationsEnabled by remember { mutableStateOf(NotificationHelper.canDeliverReminders(context)) }
    var exactAlarmsEnabled by remember { mutableStateOf(AlarmScheduler.canScheduleExactAlarms(context)) }
    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                notificationsEnabled = NotificationHelper.canDeliverReminders(context)
                val exactNow = AlarmScheduler.canScheduleExactAlarms(context)
                if (exactNow && !exactAlarmsEnabled) enqueueEventAlarmRecovery(context)
                exactAlarmsEnabled = exactNow
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val textButtonColors = ButtonDefaults.textButtonColors(
        contentColor = if (LocalAppDarkTheme.current) NotePrimaryLight else NotePrimary,
    )
    when {
        !notificationsEnabled -> {
            Spacer(Modifier.height(12.dp))
            EventEditorError(stringResource(R.string.event_notifications_disabled_inline))
            TextButton(
                onClick = { NotificationHelper.openReminderNotificationSettings(context) },
                colors = textButtonColors,
            ) {
                Text(stringResource(R.string.event_notifications_open_settings_inline))
            }
        }
        !exactAlarmsEnabled -> {
            Spacer(Modifier.height(12.dp))
            EventEditorError(stringResource(R.string.event_exact_alarm_disabled_inline))
            TextButton(
                onClick = { AlarmScheduler.requestExactAlarmPermission(context) },
                colors = textButtonColors,
            ) {
                Text(stringResource(R.string.event_exact_alarm_open_settings_inline))
            }
        }
    }
}
