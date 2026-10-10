package com.example.maiplan.home.task.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.maiplan.R
import com.example.maiplan.database.entities.CategoryEntity
import com.example.maiplan.repository.task.TaskStatus
import com.example.maiplan.repository.task.effectiveTaskEstimate
import com.example.maiplan.utils.common.IconData
import com.example.maiplan.viewmodel.task.TaskEditorState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

internal fun taskEditorDateText(value: String, empty: String): String =
    runCatching { LocalDate.parse(value).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)) }
        .getOrElse { value.ifBlank { empty } }

@Composable
internal fun TaskEditorPreview(state: TaskEditorState, category: CategoryEntity?, templateEditing: Boolean) {
    val draft = state.draft
    val accent = category?.let { taskCategoryColor(it.color, MaterialTheme.colorScheme.primary) }
        ?: MaterialTheme.colorScheme.primary
    TaskPanel {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(58.dp).background(
                    Brush.linearGradient(listOf(accent, accent.copy(alpha = 0.72f))), RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center) {
                    Icon(category?.let { IconData.getIconByKey(it.icon) } ?: Icons.Rounded.TaskAlt, null,
                        tint = if (accent.luminance() > 0.68f) Color(0xFF172033) else Color.White,
                        modifier = Modifier.size(30.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(draft.title.ifBlank { stringResource(R.string.task_preview_title) },
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                        maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(taskEditorDateText(draft.date, stringResource(R.string.task_unscheduled)),
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(category?.name ?: stringResource(R.string.uncategorized),
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(Modifier.width(8.dp))
                Surface(shape = RoundedCornerShape(999.dp), color = Color(0xFF00BFA6).copy(alpha = 0.12f)) {
                    Text(stringResource(R.string.category_live_preview), modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF00BFA6))
                }
            }
            if (draft.description.isNotBlank()) Text(draft.description, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            runCatching { effectiveTaskEstimate(draft.copy(title = "Preview").content().estimatedMilliseconds,
                state.steps.map { it.input().estimatedMilliseconds }) }.getOrNull()?.let { TaskEstimateText(it) }
            if (state.steps.isNotEmpty()) Text(pluralStringResource(R.plurals.task_progress_count, state.steps.size,
                state.steps.count { TaskStatus.fromCode(it.status).isTerminal }, state.steps.size),
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (state.repeat.unit != 0) {
                Text(stringResource(when (state.repeat.unit) {
                    1 -> R.string.task_repeat_daily
                    2 -> R.string.task_repeat_weekly
                    else -> R.string.task_repeat_monthly
                }), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (state.repeat.unit != 0 && state.repeat.relative) {
                val lead = state.repeat.leadDays.toIntOrNull()
                if (lead != null) Text(pluralStringResource(R.plurals.task_relative_summary, lead, lead,
                    state.repeat.time, state.repeat.zone), style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else if (!templateEditing && state.reminder.enabled) {
                Text(stringResource(R.string.task_preview_reminder,
                    taskEditorDateText(state.reminder.date, ""), state.reminder.time, state.reminder.zoneId),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
