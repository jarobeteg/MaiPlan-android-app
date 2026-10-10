package com.example.maiplan.home.task.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.maiplan.R
import com.example.maiplan.repository.task.TaskSnapshot
import com.example.maiplan.repository.task.TaskStatus
import com.example.maiplan.utils.notifications.*
import com.example.maiplan.viewmodel.task.TaskReminderEditorDraft
import java.time.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskReminderFields(value: TaskReminderEditorDraft, editable: Boolean, onChange: (TaskReminderEditorDraft) -> Unit) {
    var datePicker by rememberSaveable { mutableStateOf(false) }
    var timePicker by rememberSaveable { mutableStateOf(false) }
    Text(stringResource(R.string.task_reminder), style = MaterialTheme.typography.titleMedium)
    val reminderLabel = stringResource(R.string.task_reminder_enabled)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(stringResource(R.string.task_reminder_enabled))
        Switch(checked = value.enabled, enabled = editable, modifier = Modifier.testTag("task-reminder-enabled").semantics { contentDescription = reminderLabel },
            onCheckedChange = { onChange(if (it) value.enable() else value.copy(enabled = false)) })
    }
    Text(stringResource(R.string.task_reminder_help), style = MaterialTheme.typography.bodySmall)
    if (!value.enabled && value.date.isNotBlank()) {
        Text("${value.date}  ${value.time} (${value.zoneId})", style = MaterialTheme.typography.bodySmall)
        TextButton(enabled = editable, onClick = { onChange(TaskReminderEditorDraft()) }) { Text(stringResource(R.string.task_reminder_remove)) }
    }
    if (value.enabled) {
        OutlinedTextField(value.date, { onChange(value.copy(date = it)) }, enabled = editable,
            label = { Text(stringResource(R.string.task_reminder_date)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        TextButton(enabled = editable, onClick = { datePicker = true }) { Text(stringResource(R.string.task_reminder_choose_date)) }
        OutlinedTextField(value.time, { onChange(value.copy(time = it)) }, enabled = editable,
            label = { Text(stringResource(R.string.task_reminder_time)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        TextButton(enabled = editable, onClick = { timePicker = true }) { Text(stringResource(R.string.task_reminder_choose_time)) }
        OutlinedTextField(value.zoneId, { onChange(value.copy(zoneId = it)) }, enabled = editable,
            label = { Text(stringResource(R.string.task_reminder_zone)) }, supportingText = { Text(stringResource(R.string.task_reminder_zone_hint)) },
            singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value.message, { onChange(value.copy(message = it.take(512))) }, enabled = editable,
            label = { Text(stringResource(R.string.task_reminder_message)) }, supportingText = { Text("${value.message.length}/512") },
            minLines = 2, maxLines = 5, modifier = Modifier.fillMaxWidth())
        val oldTime = runCatching { value.reminder().triggerAtMillis <= System.currentTimeMillis() }.getOrDefault(false)
        if (oldTime) Text(stringResource(R.string.task_reminder_past), style = MaterialTheme.typography.bodySmall)
        TaskReminderAccessWarnings()
    }
    if (datePicker) {
        val initial = runCatching { LocalDate.parse(value.date) }.getOrNull()?.takeIf { it.year in 1..9999 } ?: LocalDate.now()
        val picker = rememberDatePickerState(initialSelectedDateMillis = initial.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(), yearRange = 1..9999)
        DatePickerDialog(onDismissRequest = { datePicker = false }, confirmButton = {
            TextButton(enabled = picker.selectedDateMillis != null, onClick = {
                picker.selectedDateMillis?.let { onChange(value.copy(date = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toString())) }
                datePicker = false
            }) { Text(stringResource(R.string.task_confirm)) }
        }, dismissButton = { TextButton(onClick = { datePicker = false }) { Text(stringResource(R.string.task_cancel)) } }) { DatePicker(picker) }
    }
    if (timePicker) {
        val initial = runCatching { LocalTime.parse(value.time) }.getOrDefault(LocalTime.NOON)
        val picker = rememberTimePickerState(initial.hour, initial.minute, is24Hour = true)
        AlertDialog(onDismissRequest = { timePicker = false }, title = { Text(stringResource(R.string.task_reminder_time)) },
            text = { TimeInput(picker) }, confirmButton = { TextButton(onClick = {
                onChange(value.copy(time = LocalTime.of(picker.hour, picker.minute).toString())); timePicker = false
            }) { Text(stringResource(R.string.task_confirm)) } },
            dismissButton = { TextButton(onClick = { timePicker = false }) { Text(stringResource(R.string.task_cancel)) } })
    }
}

@Composable
fun TaskReminderAccessWarnings() {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    var notifications by remember { mutableStateOf(NotificationHelper.canDeliverReminders(context)) }
    var exact by remember { mutableStateOf(AlarmScheduler.canScheduleExactAlarms(context)) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        notifications = NotificationHelper.canDeliverReminders(context)
        if (notifications) enqueueEventAlarmRecovery(context)
    }
    DisposableEffect(owner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val newNotifications = NotificationHelper.canDeliverReminders(context)
                val newExact = AlarmScheduler.canScheduleExactAlarms(context)
                if (newNotifications && !notifications || newExact && !exact) enqueueEventAlarmRecovery(context)
                notifications = newNotifications; exact = newExact
            }
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    if (!notifications) {
        TaskMessage(stringResource(R.string.task_reminder_notifications_denied))
        TextButton(onClick = {
            if (Build.VERSION.SDK_INT >= 33 && !NotificationHelper.canPostNotifications(context)) launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            else NotificationHelper.openReminderNotificationSettings(context)
        }) { Text(stringResource(R.string.task_reminder_allow_notifications)) }
        TextButton(onClick = { NotificationHelper.openReminderNotificationSettings(context) }) {
            Text(stringResource(R.string.event_notifications_open_settings_inline))
        }
    }
    if (!exact) {
        TaskMessage(stringResource(R.string.task_reminder_exact_denied))
        TextButton(onClick = { AlarmScheduler.requestExactAlarmPermission(context) }) { Text(stringResource(R.string.event_exact_alarm_open_settings_inline)) }
    }
}

@Composable
fun TaskReminderSummary(snapshot: TaskSnapshot) {
    snapshot.task.relativeReminderJson?.let {
        val rule = com.example.maiplan.repository.task.TaskRelativeReminder.decode(it)
        Text(stringResource(R.string.task_reminder), style = MaterialTheme.typography.titleMedium)
        Text(pluralStringResource(R.plurals.task_relative_summary, rule.lead_days, rule.lead_days,
            "%02d:%02d".format(rule.minute_of_day / 60, rule.minute_of_day % 60), rule.zone_id))
        rule.message?.let { message -> Text(message) }
        if (TaskStatus.fromCode(snapshot.task.status).isTerminal) Text(stringResource(R.string.task_reminder_paused)) else TaskReminderAccessWarnings()
        return
    }
    val reminder = snapshot.reminder ?: return
    val value = TaskReminderEditorDraft.from(reminder)
    Text(stringResource(R.string.task_reminder), style = MaterialTheme.typography.titleMedium)
    Text("${value.date}  ${value.time} (${value.zoneId})")
    reminder.message?.takeIf { it.isNotBlank() }?.let { Text(it) }
    if (!value.enabled) Text(stringResource(R.string.task_reminder_disabled))
    else if (TaskStatus.fromCode(snapshot.task.status).isTerminal) Text(stringResource(R.string.task_reminder_paused))
    else TaskReminderAccessWarnings()
}
