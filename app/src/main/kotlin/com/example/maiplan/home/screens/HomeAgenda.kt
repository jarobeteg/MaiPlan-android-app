package com.example.maiplan.home.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.maiplan.R
import com.example.maiplan.home.clock.HomeClockHourFormat
import com.example.maiplan.home.event.utils.CalendarEventUI
import com.example.maiplan.home.task.screens.TaskEstimateText
import com.example.maiplan.home.task.screens.taskStatusIcon
import com.example.maiplan.home.task.screens.taskStatusLabel
import com.example.maiplan.repository.task.TaskStatus
import com.example.maiplan.repository.task.TaskSummary
import com.example.maiplan.repository.note.NoteReminderItem
import com.example.maiplan.theme.AppThemeManager
import com.example.maiplan.theme.LocalAppDarkTheme
import com.example.maiplan.utils.common.IconData
import com.example.maiplan.viewmodel.home.HomeSection
import java.time.LocalDate
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

private val homeInk: Color
    @Composable get() = if (LocalAppDarkTheme.current) Color(0xFFF5F7FB) else Color(0xFF172033)
private val homeMuted: Color
    @Composable get() = if (LocalAppDarkTheme.current) Color(0xFFAEB7C9) else Color(0xFF667085)
private val homeAccent: Color
    @Composable get() = if (LocalAppDarkTheme.current) AppThemeManager.selectedTheme.primaryLight else AppThemeManager.selectedTheme.primary

@OptIn(ExperimentalLayoutApi::class)
internal fun LazyListScope.homeAgenda(
    today: LocalDate,
    events: HomeSection<CalendarEventUI>,
    tasks: HomeSection<TaskSummary>,
    notes: HomeSection<NoteReminderItem>,
    hourFormat: HomeClockHourFormat,
    onEvent: (CalendarEventUI) -> Unit,
    onTask: (Long) -> Unit,
    onNote: (Long) -> Unit,
    onAllEvents: () -> Unit,
    onAllTasks: () -> Unit,
    onAllNotes: () -> Unit,
    onRetry: () -> Unit,
) {
    item(key = "today") {
        Column(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(today.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)),
                style = MaterialTheme.typography.bodyMedium, color = homeMuted)
            Text(stringResource(R.string.home_today), style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold, color = homeInk, modifier = Modifier.semantics { heading() })
            if (!events.loading && !events.failed && !tasks.loading && !tasks.failed && !notes.loading && !notes.failed) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    HomeCount(Icons.Rounded.CalendarMonth, pluralStringResource(R.plurals.home_event_count,
                        events.items.size, events.items.size))
                    HomeCount(Icons.Rounded.CheckCircleOutline, pluralStringResource(R.plurals.home_task_count,
                        tasks.items.size, tasks.items.size))
                    HomeCount(Icons.AutoMirrored.Rounded.Notes, pluralStringResource(R.plurals.home_note_count,
                        notes.items.size, notes.items.size))
                }
            }
        }
    }
    if (!events.loading && !events.failed && events.items.isEmpty() &&
        !tasks.loading && !tasks.failed && tasks.items.isEmpty() &&
        !notes.loading && !notes.failed && notes.items.isEmpty()) {
        item(key = "clear-day") {
            HomePanel {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.WbSunny, null, tint = homeAccent, modifier = Modifier.size(30.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(stringResource(R.string.home_clear_day), color = homeInk,
                            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(stringResource(R.string.home_clear_day_body), color = homeMuted,
                            style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
    item(key = "events-heading") {
        HomeSectionHeading(stringResource(R.string.home_events), stringResource(R.string.home_view_events), onAllEvents)
    }
    when {
        events.loading || events.failed -> item(key = "events-state") { HomeLoadState(events.loading, onRetry) }
        events.items.isEmpty() -> item(key = "events-empty") { HomeEmpty(stringResource(R.string.home_no_events)) }
        else -> items(events.items.sortedWith(compareBy<CalendarEventUI> { it.isTimed }
            .thenBy { it.day.occurrence.start?.toInstant() }.thenBy { it.title.lowercase(Locale.ROOT) }
            .thenBy { it.listKey }), key = { "event:${it.listKey}" }) { event ->
            HomeEventCard(event, hourFormat) { onEvent(event) }
        }
    }
    item(key = "tasks-heading") {
        HomeSectionHeading(stringResource(R.string.home_tasks), stringResource(R.string.home_view_tasks), onAllTasks)
    }
    when {
        tasks.loading || tasks.failed -> item(key = "tasks-state") { HomeLoadState(tasks.loading, onRetry) }
        tasks.items.isEmpty() -> item(key = "tasks-empty") { HomeEmpty(stringResource(R.string.home_no_tasks)) }
        else -> items(tasks.items.sortedWith(compareBy<TaskSummary> { TaskStatus.fromCode(it.task.status).isTerminal }
            .thenBy { it.task.title.lowercase(Locale.ROOT) }.thenBy { it.task.syncId }),
            key = { "task:${it.task.syncId}" }) { task -> HomeTaskCard(task) { onTask(task.task.taskLocalId) } }
    }
    item(key = "notes-heading") {
        HomeSectionHeading(stringResource(R.string.home_notes), stringResource(R.string.home_view_notes), onAllNotes)
    }
    when {
        notes.loading || notes.failed -> item(key = "notes-state") { HomeLoadState(notes.loading, onRetry) }
        notes.items.isEmpty() -> item(key = "notes-empty") { HomeEmpty(stringResource(R.string.home_no_notes)) }
        else -> items(notes.items, key = { "note:${it.note.syncId}" }) { note ->
            HomeNoteCard(note, hourFormat) { onNote(note.note.noteLocalId) }
        }
    }
}

@Composable
private fun HomeCount(icon: ImageVector, label: String) {
    Surface(color = homeAccent.copy(alpha = 0.10f), shape = RoundedCornerShape(12.dp)) {
        Row(Modifier.padding(horizontal = 11.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, null, tint = homeAccent, modifier = Modifier.size(16.dp))
            Text(label, color = homeInk, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun HomeSectionHeading(title: String, action: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, color = homeInk, style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f).semantics { heading() })
        TextButton(onClick = onClick, colors = ButtonDefaults.textButtonColors(contentColor = homeAccent)) {
            Text(action)
        }
    }
}

@Composable
private fun HomePanel(onClick: (() -> Unit)? = null, content: @Composable () -> Unit) {
    val dark = LocalAppDarkTheme.current
    val color = if (dark) Color(0xFF191D2E) else Color.White
    val border = BorderStroke(1.dp, if (dark) Color(0xFF30374D) else Color(0xFFE1E6EF))
    val modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
    val body: @Composable () -> Unit = { Box(Modifier.padding(16.dp)) { content() } }
    if (onClick != null) Surface(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(20.dp),
        color = color, border = border, content = body)
    else Surface(modifier = modifier, shape = RoundedCornerShape(20.dp), color = color, border = border, content = body)
}

@Composable
private fun HomeEventCard(event: CalendarEventUI, hourFormat: HomeClockHourFormat, onClick: () -> Unit) {
    HomeItemCard(event.title, event.icon, event.color, onClick) {
        Text(homeEventTime(event, hourFormat), color = homeAccent,
            style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        if (event.description.isNotBlank()) Text(event.description, color = homeMuted,
            style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
        if (event.isRecurring) Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(Icons.Rounded.Repeat, null, tint = homeMuted, modifier = Modifier.size(14.dp))
            Text(stringResource(R.string.home_repeating), color = homeMuted, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun homeEventTime(event: CalendarEventUI, hourFormat: HomeClockHourFormat): String {
    if (!event.isTimed) return stringResource(R.string.home_all_day)
    val formatter = DateTimeFormatter.ofPattern(if (hourFormat == HomeClockHourFormat.TWELVE_HOUR) "h:mm a" else "HH:mm")
    val start = event.day.occurrence.start!!.format(formatter)
    val end = event.day.occurrence.end!!.format(formatter)
    val time = when {
        event.day.isFirstDay && event.day.isLastDay -> stringResource(R.string.home_time_range, start, end)
        event.day.isFirstDay -> stringResource(R.string.home_from, start)
        event.day.isLastDay -> stringResource(R.string.home_until, end)
        else -> stringResource(R.string.home_continues)
    }
    return if (event.zoneId != ZoneId.systemDefault().id) stringResource(R.string.home_event_zone, time, event.zoneId) else time
}

@Composable
private fun HomeTaskCard(summary: TaskSummary, onClick: () -> Unit) {
    val status = TaskStatus.fromCode(summary.task.status)
    val category = summary.category
    val color = category?.color?.toULongOrNull()?.let { Color(it) } ?: homeAccent
    HomeItemCard(summary.task.title, category?.let { IconData.getIconByKey(it.icon) }
        ?: taskStatusIcon(status), color, onClick) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Icon(taskStatusIcon(status), null, tint = homeAccent, modifier = Modifier.size(15.dp))
            Text(taskStatusLabel(status), color = homeAccent, style = MaterialTheme.typography.labelMedium)
        }
        category?.let { Text(it.name, color = homeMuted, style = MaterialTheme.typography.bodySmall,
            maxLines = 1, overflow = TextOverflow.Ellipsis) }
        if (summary.estimate.milliseconds != null) {
            MaterialTheme(colorScheme = MaterialTheme.colorScheme.copy(onSurfaceVariant = homeMuted)) {
                TaskEstimateText(summary.estimate)
            }
        }
        if (summary.stepCount > 0) {
            Text(pluralStringResource(R.plurals.task_progress_count, summary.stepCount, summary.finishedSteps, summary.stepCount),
                color = homeMuted, style = MaterialTheme.typography.bodySmall)
            LinearProgressIndicator(progress = { summary.finishedSteps.toFloat() / summary.stepCount },
                modifier = Modifier.fillMaxWidth().height(4.dp), color = homeAccent,
                trackColor = homeAccent.copy(alpha = 0.12f), drawStopIndicator = {})
        }
    }
}

@Composable
private fun HomeNoteCard(item: NoteReminderItem, hourFormat: HomeClockHourFormat, onClick: () -> Unit) {
    val category = item.category
    val color = category?.color?.toULongOrNull()?.let { Color(it) } ?: homeAccent
    val formatter = DateTimeFormatter.ofPattern(if (hourFormat == HomeClockHourFormat.TWELVE_HOUR) "h:mm a" else "HH:mm")
    val time = Instant.ofEpochMilli(item.reminderTime).atZone(ZoneId.systemDefault()).format(formatter)
    HomeItemCard(item.note.title, category?.let { IconData.getIconByKey(it.icon) }
        ?: Icons.AutoMirrored.Rounded.Notes, color, onClick) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Icon(Icons.Rounded.NotificationsNone, null, tint = homeAccent, modifier = Modifier.size(15.dp))
            Text(stringResource(R.string.home_note_reminder_at, time), color = homeAccent,
                style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
        item.note.content?.takeIf { it.isNotBlank() }?.let {
            Text(it, color = homeMuted, style = MaterialTheme.typography.bodySmall,
                maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        category?.let { Text(it.name, color = homeMuted, style = MaterialTheme.typography.labelSmall,
            maxLines = 1, overflow = TextOverflow.Ellipsis) }
    }
}

@Composable
private fun HomeItemCard(title: String, icon: ImageVector, accent: Color, onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit) {
    HomePanel(onClick) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(color = accent.copy(alpha = 0.13f), shape = RoundedCornerShape(14.dp)) {
                Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = accent, modifier = Modifier.size(23.dp))
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(title, color = homeInk, style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                content()
            }
            Icon(Icons.Rounded.ChevronRight, null, tint = homeMuted, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun HomeEmpty(message: String) {
    Text(message, color = homeMuted, style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.fillMaxWidth().padding(start = 2.dp, top = 4.dp, bottom = 12.dp))
}

@Composable
private fun HomeLoadState(loading: Boolean, onRetry: () -> Unit) {
    HomePanel {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (loading) CircularProgressIndicator(Modifier.size(20.dp), color = homeAccent, strokeWidth = 2.dp)
            Text(stringResource(if (loading) R.string.home_loading else R.string.home_load_error),
                color = homeMuted, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            if (!loading) TextButton(onClick = onRetry) { Text(stringResource(R.string.home_retry), color = homeAccent) }
        }
    }
}
