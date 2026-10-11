package com.example.maiplan.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import com.example.maiplan.R
import com.example.maiplan.theme.AppThemeManager
import com.example.maiplan.theme.LocalAppDarkTheme
import com.example.maiplan.utils.common.IconData
import java.util.Locale
import kotlin.math.roundToInt

private data class PickerColors(
    val surface: Color,
    val field: Color,
    val foreground: Color,
    val muted: Color,
    val border: Color,
    val accent: Color,
    val accentSurface: Color,
)

@Composable
private fun pickerColors(): PickerColors {
    val dark = LocalAppDarkTheme.current
    val primary = MaterialTheme.colorScheme.primary
    return PickerColors(
        surface = if (dark) Color(0xFF191D2E) else Color.White,
        field = if (dark) Color(0xFF20263A) else Color(0xFFF8FAFC),
        foreground = if (dark) Color(0xFFF5F7FB) else Color(0xFF172033),
        muted = if (dark) Color(0xFFAEB7C9) else Color(0xFF667085),
        border = if (dark) Color(0xFF3A435C) else Color(0xFFDDE3EC),
        accent = if (dark) AppThemeManager.selectedTheme.primaryLight else primary,
        accentSurface = primary.copy(alpha = if (dark) 0.25f else 0.10f),
    )
}

@Composable
private fun CategoryPickerSurface(
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = pickerColors()
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        Box(
            Modifier.fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier.fillMaxSize().clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
            )
            Surface(
                onClick = {},
                modifier = Modifier.widthIn(max = 520.dp).fillMaxWidth().heightIn(max = 680.dp),
                shape = RoundedCornerShape(26.dp),
                color = colors.surface,
                contentColor = colors.foreground,
                border = BorderStroke(1.dp, colors.border),
                shadowElevation = 18.dp,
            ) {
                Column(Modifier.padding(20.dp), content = content)
            }
        }
    }
}

@Composable
private fun CategoryPickerHeader(title: String, subtitle: String, icon: ImageVector) {
    val colors = pickerColors()
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.size(46.dp),
            shape = RoundedCornerShape(14.dp),
            color = colors.accentSurface,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = colors.accent, modifier = Modifier.size(23.dp))
            }
        }
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(3.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.muted)
        }
    }
    Spacer(Modifier.height(20.dp))
}

@Composable
private fun CategoryPickerActions(onDismiss: () -> Unit, onConfirm: (() -> Unit)? = null) {
    val colors = pickerColors()
    HorizontalDivider(color = colors.border.copy(alpha = 0.65f))
    Spacer(Modifier.height(12.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)) {
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.heightIn(min = 48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.textButtonColors(containerColor = colors.field, contentColor = colors.foreground),
        ) {
            Text(stringResource(R.string.cancel), fontWeight = FontWeight.SemiBold)
        }
        if (onConfirm != null) {
            Button(
                onClick = onConfirm,
                modifier = Modifier.heightIn(min = 48.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text(stringResource(R.string.select), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun ColorPickerDialog(initialColor: Color, onColorSelected: (Color) -> Unit, onDismiss: () -> Unit) {
    val colors = pickerColors()
    val initialHsv = remember(initialColor) {
        FloatArray(3).also { android.graphics.Color.colorToHSV(initialColor.toArgb(), it) }
    }
    var hue by rememberSaveable(initialColor) { mutableFloatStateOf(initialHsv[0]) }
    var saturation by rememberSaveable(initialColor) { mutableFloatStateOf(initialHsv[1]) }
    var brightness by rememberSaveable(initialColor) { mutableFloatStateOf(initialHsv[2]) }
    var adjusted by rememberSaveable(initialColor) { mutableStateOf(false) }
    val color = if (adjusted) Color.hsv(hue, saturation, brightness) else initialColor
    val presets = listOf(
        MaterialTheme.colorScheme.primary, Color(0xFF6366F1), Color(0xFF8B5CF6),
        Color(0xFFEC4899), Color(0xFFEF4444), Color(0xFFF97316),
        Color(0xFFEAB308), Color(0xFF84CC16), Color(0xFF22C55E),
        Color(0xFF14B8A6), Color(0xFF0EA5E9), Color(0xFF64748B),
    )

    CategoryPickerSurface(onDismiss) {
        CategoryPickerHeader(
            stringResource(R.string.category_choose_color_title),
            stringResource(R.string.category_choose_color_subtitle),
            Icons.Rounded.Palette,
        )
        Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = colors.field,
                border = BorderStroke(1.dp, colors.border.copy(alpha = 0.6f)),
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(48.dp).background(color, CircleShape).border(1.dp, colors.border, CircleShape),
                    )
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            stringResource(R.string.category_color_preview),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            color.toHexLabel(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Monospace,
                            color = colors.muted,
                        )
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
            Text(
                stringResource(R.string.category_color_presets),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(6.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                maxItemsInEachRow = 6,
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                presets.forEach { preset ->
                    val selected = color.toArgb() == preset.toArgb()
                    val description = stringResource(R.string.category_color_swatch, preset.toHexLabel())
                    Box(
                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp))
                            .selectable(selected, role = Role.RadioButton, onClick = {
                                val hsv = FloatArray(3)
                                android.graphics.Color.colorToHSV(preset.toArgb(), hsv)
                                hue = hsv[0]
                                saturation = hsv[1]
                                brightness = hsv[2]
                                adjusted = true
                            })
                            .semantics { contentDescription = description },
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            Modifier.size(38.dp).border(
                                if (selected) 2.dp else 1.dp,
                                if (selected) colors.accent else Color.Transparent,
                                CircleShape,
                            ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(Modifier.size(30.dp).background(preset, CircleShape), contentAlignment = Alignment.Center) {
                                if (selected) Icon(
                                    Icons.Rounded.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = if (preset.luminance() > 0.5f) Color(0xFF172033) else Color.White,
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(
                stringResource(R.string.category_color_custom),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(14.dp))
            ColorAdjustment(
                label = stringResource(R.string.hue),
                valueLabel = stringResource(R.string.category_color_degrees, hue.roundToInt()),
                value = hue,
                onValueChange = { hue = it; adjusted = true },
                range = 0f..360f,
                brush = Brush.horizontalGradient((0..360 step 30).map { Color.hsv(it.toFloat(), 1f, 1f) }),
            )
            ColorAdjustment(
                label = stringResource(R.string.saturation),
                valueLabel = stringResource(R.string.category_color_percent, (saturation * 100).roundToInt()),
                value = saturation,
                onValueChange = { saturation = it; adjusted = true },
                brush = Brush.horizontalGradient(listOf(Color.hsv(hue, 0f, brightness), Color.hsv(hue, 1f, brightness))),
            )
            ColorAdjustment(
                label = stringResource(R.string.brightness),
                valueLabel = stringResource(R.string.category_color_percent, (brightness * 100).roundToInt()),
                value = brightness,
                onValueChange = { brightness = it; adjusted = true },
                brush = Brush.horizontalGradient(listOf(Color.Black, Color.hsv(hue, saturation, 1f))),
            )
        }
        Spacer(Modifier.height(12.dp))
        CategoryPickerActions(onDismiss, onConfirm = { onColorSelected(color); onDismiss() })
    }
}

private fun Color.toHexLabel(): String = String.format(Locale.ROOT, "#%06X", toArgb() and 0xFFFFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColorAdjustment(
    label: String,
    valueLabel: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    brush: Brush,
    range: ClosedFloatingPointRange<Float> = 0f..1f,
) {
    val colors = pickerColors()
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = colors.muted)
        Text(valueLabel, style = MaterialTheme.typography.labelMedium, color = colors.foreground)
    }
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = range,
        modifier = Modifier.fillMaxWidth().height(48.dp).semantics { contentDescription = label },
        colors = SliderDefaults.colors(thumbColor = colors.accent),
        thumb = {
            Box(Modifier.size(24.dp).background(colors.accent, CircleShape).border(3.dp, colors.surface, CircleShape))
        },
        track = {
            Box(Modifier.fillMaxWidth().height(10.dp).background(brush, CircleShape))
        },
    )
}

@Composable
fun IconPickerDialog(
    onIconSelected: (ImageVector) -> Unit,
    onIconSelectedString: (String) -> Unit,
    onDismiss: () -> Unit,
    initialIcon: ImageVector? = null,
) {
    val colors = pickerColors()
    var query by rememberSaveable { mutableStateOf("") }
    var selectedGroup by rememberSaveable { mutableStateOf<Int?>(null) }
    val groups = IconData.groups.map { it to stringResource(it.titleRes) }
    val search = query.trim()
    val filteredIcons = groups
        .filter { (group, _) -> selectedGroup == null || group.titleRes == selectedGroup }
        .flatMap { (group, label) ->
            group.icons.entries.filter { it.key.contains(search, ignoreCase = true) || label.contains(search, ignoreCase = true) }
        }
    val scrollState = rememberLazyGridState()
    LaunchedEffect(query, selectedGroup) { scrollState.scrollToItem(0) }

    CategoryPickerSurface(onDismiss) {
        CategoryPickerHeader(
            stringResource(R.string.category_choose_icon_title),
            stringResource(R.string.category_choose_icon_subtitle),
            Icons.Rounded.GridView,
        )
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.category_icon_search)) },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, modifier = Modifier.size(21.dp)) },
            trailingIcon = if (query.isNotEmpty()) {
                { IconButton(onClick = { query = "" }) {
                    Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.category_icon_clear_search))
                } }
            } else null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = colors.field,
                unfocusedContainerColor = colors.field,
                focusedTextColor = colors.foreground,
                unfocusedTextColor = colors.foreground,
                focusedPlaceholderColor = colors.muted,
                unfocusedPlaceholderColor = colors.muted,
                focusedLeadingIconColor = colors.accent,
                unfocusedLeadingIconColor = colors.muted,
                focusedTrailingIconColor = colors.muted,
                unfocusedTrailingIconColor = colors.muted,
                focusedBorderColor = colors.accent,
                unfocusedBorderColor = colors.border,
                cursorColor = colors.accent,
            ),
        )
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val chipColors = FilterChipDefaults.filterChipColors(
                containerColor = colors.field,
                labelColor = colors.muted,
                selectedContainerColor = colors.accentSurface,
                selectedLabelColor = colors.accent,
            )
            FilterChip(
                selected = selectedGroup == null,
                onClick = { selectedGroup = null },
                label = { Text(stringResource(R.string.category_icon_group_all)) },
                shape = RoundedCornerShape(12.dp),
                colors = chipColors,
            )
            groups.forEach { (group, label) ->
                FilterChip(
                    selected = selectedGroup == group.titleRes,
                    onClick = { selectedGroup = group.titleRes },
                    label = { Text(label) },
                    shape = RoundedCornerShape(12.dp),
                    colors = chipColors,
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            pluralStringResource(R.plurals.category_icon_count, filteredIcons.size, filteredIcons.size),
            style = MaterialTheme.typography.labelMedium,
            color = colors.muted,
        )
        Spacer(Modifier.height(12.dp))
        Box(Modifier.fillMaxWidth().heightIn(max = 340.dp).weight(1f, fill = false)) {
            if (filteredIcons.isEmpty()) {
                Column(
                    Modifier.fillMaxWidth().padding(vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(Icons.Rounded.Search, contentDescription = null, tint = colors.muted, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.height(10.dp))
                    Text(stringResource(R.string.category_icon_empty_title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Text(stringResource(R.string.category_icon_empty_subtitle), style = MaterialTheme.typography.bodySmall, color = colors.muted, textAlign = TextAlign.Center)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(48.dp),
                    state = scrollState,
                    contentPadding = PaddingValues(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    items(filteredIcons, key = { it.key }) { (key, icon) ->
                        val selected = initialIcon == icon
                        val label = key.replaceFirstChar { it.titlecase() }
                        Box(
                            modifier = Modifier.aspectRatio(1f).clip(RoundedCornerShape(14.dp))
                                .background(if (selected) colors.accentSurface else colors.field)
                                .border(1.dp, if (selected) colors.accent else colors.border.copy(alpha = 0.65f), RoundedCornerShape(14.dp))
                                .selectable(selected, role = Role.RadioButton, onClick = {
                                    onIconSelected(icon)
                                    onIconSelectedString(key)
                                    onDismiss()
                                })
                                .semantics { contentDescription = label },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(icon, contentDescription = null, tint = colors.accent, modifier = Modifier.size(26.dp))
                        }
                    }
                }
                if (scrollState.canScrollForward) {
                    Box(
                        Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(18.dp)
                            .background(Brush.verticalGradient(listOf(Color.Transparent, colors.surface))),
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        CategoryPickerActions(onDismiss)
    }
}
