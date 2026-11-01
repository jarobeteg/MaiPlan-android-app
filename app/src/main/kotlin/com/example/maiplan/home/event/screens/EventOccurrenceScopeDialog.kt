package com.example.maiplan.home.event.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.maiplan.R
import com.example.maiplan.theme.LocalAppDarkTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
internal fun EventOccurrenceScopeDialog(
    deleting: Boolean,
    enabled: Boolean,
    eventTitle: String,
    occurrenceDate: LocalDate,
    onDismiss: () -> Unit,
    onCurrent: () -> Unit,
    onAll: () -> Unit,
) {
    Dialog(
        onDismissRequest = { if (enabled) onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = enabled,
            dismissOnClickOutside = enabled,
        ),
    ) {
        Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
            EventOccurrenceScopeContent(deleting, enabled, eventTitle, occurrenceDate, onDismiss, onCurrent, onAll)
        }
    }
}

private data class EventScopeColors(
    val surface: Color,
    val field: Color,
    val foreground: Color,
    val muted: Color,
    val border: Color,
    val accent: Color,
    val danger: Color,
)

@Composable
private fun EventOccurrenceScopeContent(
    deleting: Boolean,
    enabled: Boolean,
    eventTitle: String,
    occurrenceDate: LocalDate,
    onDismiss: () -> Unit,
    onCurrent: () -> Unit,
    onAll: () -> Unit,
) {
    val dark = LocalAppDarkTheme.current
    val colors = EventScopeColors(
        surface = if (dark) Color(0xFF191D2E) else Color.White,
        field = if (dark) Color(0xFF20263A) else Color(0xFFF8FAFC),
        foreground = if (dark) Color(0xFFF5F7FB) else EventInk,
        muted = if (dark) Color(0xFFAEB7C9) else EventMuted,
        border = if (dark) Color(0xFF3A435C) else EventBorder,
        accent = if (dark) EventPrimaryLight else EventPrimary,
        danger = if (dark) Color(0xFFFDA4AF) else EventDanger,
    )
    val accent = if (deleting) colors.danger else colors.accent
    Surface(
        modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = colors.surface,
        contentColor = colors.foreground,
        border = BorderStroke(1.dp, colors.border),
        shadowElevation = 18.dp,
    ) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(15.dp),
                    color = accent.copy(alpha = 0.12f),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(if (deleting) Icons.Rounded.DeleteOutline else Icons.Rounded.Edit,
                            contentDescription = null, tint = accent, modifier = Modifier.size(25.dp))
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(if (deleting) R.string.event_delete_occurrences_title else R.string.event_edit_occurrences_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics { heading() },
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(if (deleting) R.string.event_delete_scope_help else R.string.event_edit_scope_help),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.muted,
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            Surface(color = colors.field, shape = RoundedCornerShape(16.dp)) {
                Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.CalendarMonth, contentDescription = null,
                        tint = colors.accent, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(eventTitle, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold,
                            color = colors.foreground, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(occurrenceDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)),
                            style = MaterialTheme.typography.bodySmall, color = colors.muted)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            EventScopeOption(
                title = stringResource(if (deleting) R.string.event_delete_current_occurrence else R.string.event_edit_current_occurrence),
                description = stringResource(if (deleting) R.string.event_delete_current_help else R.string.event_edit_current_help),
                icon = Icons.Rounded.Event,
                colors = colors,
                accent = accent,
                destructive = false,
                enabled = enabled,
                onClick = onCurrent,
            )
            Spacer(Modifier.height(10.dp))
            EventScopeOption(
                title = stringResource(if (deleting) R.string.event_delete_all_occurrences else R.string.event_edit_all_occurrences),
                description = stringResource(if (deleting) R.string.event_delete_all_help else R.string.event_edit_all_help),
                icon = Icons.Rounded.DateRange,
                colors = colors,
                accent = accent,
                destructive = deleting,
                enabled = enabled,
                onClick = onAll,
            )
            Spacer(Modifier.height(18.dp))
            TextButton(
                onClick = onDismiss,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.textButtonColors(containerColor = colors.field, contentColor = colors.foreground),
            ) {
                Text(stringResource(R.string.event_occurrence_cancel), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun EventScopeOption(
    title: String,
    description: String,
    icon: ImageVector,
    colors: EventScopeColors,
    accent: Color,
    destructive: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (destructive) accent.copy(alpha = 0.07f) else colors.surface,
        border = BorderStroke(1.dp, if (destructive) accent.copy(alpha = 0.3f) else colors.border),
    ) {
        Row(Modifier.heightIn(min = 88.dp).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(40.dp), shape = RoundedCornerShape(12.dp), color = accent.copy(alpha = 0.1f)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = accent.copy(alpha = if (enabled) 1f else 0.38f),
                        modifier = Modifier.size(21.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold,
                    color = (if (destructive) accent else colors.foreground).copy(alpha = if (enabled) 1f else 0.38f))
                Text(description, style = MaterialTheme.typography.bodySmall,
                    color = colors.muted.copy(alpha = if (enabled) 1f else 0.38f))
            }
        }
    }
}
