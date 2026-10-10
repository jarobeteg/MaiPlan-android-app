package com.example.maiplan.home.task.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Message
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.maiplan.R
import com.example.maiplan.home.event.screens.EventSelectionField
import com.example.maiplan.repository.task.TaskSnapshot
import com.example.maiplan.repository.task.TaskStatus
import com.example.maiplan.utils.notifications.*
import com.example.maiplan.viewmodel.task.TaskReminderEditorDraft
import com.example.maiplan.viewmodel.task.TaskRepeatEditorDraft

@Composable
fun TaskReminderFields(value: TaskReminderEditorDraft, editable: Boolean, onChooseDate: () -> Unit,
    onChooseTime: () -> Unit, onChooseZone: () -> Unit, onChange: (TaskReminderEditorDraft) -> Unit) {
    Text(stringResource(R.string.task_reminder_help), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    if (!value.enabled && value.date.isNotBlank()) {
        Text("${taskEditorDateText(value.date, "")}  ${value.time} (${value.zoneId})", style = MaterialTheme.typography.bodySmall)
        TextButton(enabled = editable, onClick = { onChange(TaskReminderEditorDraft()) }) { Text(stringResource(R.string.task_reminder_remove)) }
    }
    if (value.enabled) {
        EventSelectionField(stringResource(R.string.task_reminder_date), taskEditorDateText(value.date,
            stringResource(R.string.task_reminder_choose_date)), Icons.Rounded.CalendarMonth, enabled = editable, onClick = onChooseDate)
        EventSelectionField(stringResource(R.string.task_reminder_time), value.time,
            Icons.Rounded.AccessTime, enabled = editable, onClick = onChooseTime)
        EventSelectionField(stringResource(R.string.task_reminder_zone), value.zoneId,
            Icons.Rounded.Public, enabled = editable, onClick = onChooseZone)
        TaskTextField(value.message, { onChange(value.copy(message = it.take(512))) }, enabled = editable,
            label = { Text(stringResource(R.string.task_reminder_message)) }, supportingText = { Text("${value.message.length}/512") },
            leadingIcon = { Icon(Icons.AutoMirrored.Rounded.Message, null) },
            minLines = 2, maxLines = 5, modifier = Modifier.fillMaxWidth())
        val oldTime = runCatching { value.reminder().triggerAtMillis <= System.currentTimeMillis() }.getOrDefault(false)
        if (oldTime) Text(stringResource(R.string.task_reminder_past), style = MaterialTheme.typography.bodySmall)
        TaskReminderAccessWarnings()
    }
}

@Composable
fun TaskRelativeReminderFields(value: TaskRepeatEditorDraft, editable: Boolean,
    onChooseTime: () -> Unit, onChooseZone: () -> Unit, onChange: (TaskRepeatEditorDraft) -> Unit) {
    Text(stringResource(R.string.task_repeat_reminder), style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant)
    TaskTextField(value.leadDays, { onChange(value.copy(leadDays = it.filter(Char::isDigit))) }, enabled = editable,
        label = { Text(stringResource(R.string.task_repeat_lead)) }, singleLine = true,
        leadingIcon = { Icon(Icons.Rounded.NotificationsNone, null) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
    EventSelectionField(stringResource(R.string.task_reminder_time), value.time,
        Icons.Rounded.AccessTime, enabled = editable, onClick = onChooseTime)
    EventSelectionField(stringResource(R.string.task_reminder_zone), value.zone,
        Icons.Rounded.Public, enabled = editable, onClick = onChooseZone)
    TaskTextField(value.message, { onChange(value.copy(message = it.take(512))) }, enabled = editable,
        label = { Text(stringResource(R.string.task_reminder_message)) }, supportingText = { Text("${value.message.length}/512") },
        leadingIcon = { Icon(Icons.AutoMirrored.Rounded.Message, null) }, minLines = 2, maxLines = 5,
        modifier = Modifier.fillMaxWidth())
    TaskReminderAccessWarnings()
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
        Text(pluralStringResource(R.plurals.task_relative_summary, rule.lead_days, rule.lead_days,
            "%02d:%02d".format(rule.minute_of_day / 60, rule.minute_of_day % 60), rule.zone_id))
        rule.message?.let { message -> Text(message) }
        if (TaskStatus.fromCode(snapshot.task.status).isTerminal) Text(stringResource(R.string.task_reminder_paused)) else TaskReminderAccessWarnings()
        return
    }
    val reminder = snapshot.reminder ?: return
    val value = TaskReminderEditorDraft.from(reminder)
    Text("${value.date}  ${value.time} (${value.zoneId})")
    reminder.message?.takeIf { it.isNotBlank() }?.let { Text(it) }
    if (!value.enabled) Text(stringResource(R.string.task_reminder_disabled))
    else if (TaskStatus.fromCode(snapshot.task.status).isTerminal) Text(stringResource(R.string.task_reminder_paused))
    else TaskReminderAccessWarnings()
}
