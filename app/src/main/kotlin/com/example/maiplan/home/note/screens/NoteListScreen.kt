package com.example.maiplan.home.note.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.maiplan.R
import com.example.maiplan.database.entities.CategoryEntity
import com.example.maiplan.database.entities.NoteEntity
import com.example.maiplan.home.navigation.HomeNavigationBar
import com.example.maiplan.utils.LocalAdaptiveLayout
import com.example.maiplan.utils.adaptiveContentWidth
import com.example.maiplan.utils.common.IconData
import com.example.maiplan.viewmodel.note.NoteViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun NoteListScreen(
    rootNavController: NavHostController,
    viewModel: NoteViewModel,
    onCreateClick: () -> Unit,
    onNoteClick: (NoteEntity) -> Unit,
    onDeleteClick: (NoteEntity) -> Unit,
) {
    val context = LocalContext.current
    val notes by viewModel.noteList.observeAsState(emptyList())
    val categories by viewModel.categoryList.observeAsState(emptyList())
    val adaptiveLayout = LocalAdaptiveLayout.current
    val compactLandscape = adaptiveLayout.isLandscape && adaptiveLayout.isShort
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    val filteredNotes = notes.filter { note ->
        (selectedCategoryId == null || note.categoryId == selectedCategoryId) &&
            (note.title.contains(searchQuery, ignoreCase = true) ||
                note.content.orEmpty().contains(searchQuery, ignoreCase = true))
    }
    val isFiltered = searchQuery.isNotBlank() || selectedCategoryId != null

    NoteScreenBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { NotesTopBar(compact = compactLandscape) },
            bottomBar = { HomeNavigationBar(rootNavController, context) },
            floatingActionButton = {
                if (compactLandscape) {
                    FloatingActionButton(
                        onClick = onCreateClick,
                        modifier = Modifier.padding(end = 6.dp, bottom = 6.dp),
                        containerColor = NotePrimary,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Icon(
                            Icons.Rounded.Add,
                            contentDescription = stringResource(R.string.note_new_action),
                        )
                    }
                } else {
                    ExtendedFloatingActionButton(
                        onClick = onCreateClick,
                        modifier = Modifier.padding(end = 8.dp, bottom = 8.dp),
                        containerColor = NotePrimary,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(17.dp),
                        icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                        text = {
                            Text(
                                text = stringResource(R.string.note_new_action),
                                fontWeight = FontWeight.Bold,
                            )
                        },
                    )
                }
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .adaptiveContentWidth()
                    .padding(
                        horizontal = if (compactLandscape) 12.dp else 18.dp,
                        vertical = if (compactLandscape) 8.dp else 14.dp,
                    ),
                verticalArrangement = Arrangement.spacedBy(if (compactLandscape) 8.dp else 14.dp),
            ) {
                if (compactLandscape) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.weight(0.9f)) {
                            NoteSearchField(searchQuery) { searchQuery = it }
                        }
                        Box(Modifier.weight(1.1f)) {
                            NoteCategoryFilter(
                                categories = categories,
                                selectedCategoryId = selectedCategoryId,
                                onCategorySelected = { selectedCategoryId = it },
                            )
                        }
                    }
                } else {
                    NoteSearchField(searchQuery) { searchQuery = it }
                    NoteCategoryFilter(
                        categories = categories,
                        selectedCategoryId = selectedCategoryId,
                        onCategorySelected = { selectedCategoryId = it },
                    )
                }
                NoteListHeader(filteredNotes.size, isFiltered, compact = compactLandscape)

                if (filteredNotes.isEmpty()) {
                    NoteEmptyState(
                        isFiltered = isFiltered,
                        onCreateClick = onCreateClick,
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = if (compactLandscape) 70.dp else 96.dp),
                        verticalArrangement = Arrangement.spacedBy(if (compactLandscape) 8.dp else 12.dp),
                    ) {
                        items(filteredNotes, key = { it.noteId }) { note ->
                            NoteCard(
                                note = note,
                                category = categories.find { it.categoryId == note.categoryId },
                                onClick = { onNoteClick(note) },
                                onDeleteClick = { onDeleteClick(note) },
                                compact = compactLandscape,
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotesTopBar(compact: Boolean) {
    val dark = isSystemInDarkTheme()
    CenterAlignedTopAppBar(
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.notes),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (dark) Color(0xFFF5F7FB) else NoteInk,
                )
                if (!compact) {
                    Text(
                        text = stringResource(R.string.note_collection_subtitle),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (dark) Color(0xFFAEB7C9) else NoteMuted,
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
    )
}

@Composable
private fun NoteSearchField(value: String, onValueChange: (String) -> Unit) {
    val dark = isSystemInDarkTheme()
    val field = if (dark) Color(0xFF191D2E) else Color.White
    val foreground = if (dark) Color(0xFFF5F7FB) else NoteInk
    val muted = if (dark) Color(0xFFAEB7C9) else NoteMuted
    OutlinedTextField(
        value = value,
        onValueChange = { if (it.length <= 32) onValueChange(it) },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(stringResource(R.string.note_search)) },
        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, modifier = Modifier.size(21.dp)) },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Search,
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = field,
            unfocusedContainerColor = field,
            focusedBorderColor = NotePrimary,
            unfocusedBorderColor = if (dark) Color(0xFF3A435C) else NoteBorder,
            focusedTextColor = foreground,
            unfocusedTextColor = foreground,
            focusedLeadingIconColor = NotePrimary,
            unfocusedLeadingIconColor = muted,
            focusedPlaceholderColor = muted,
            unfocusedPlaceholderColor = muted,
            cursorColor = NotePrimary,
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteCategoryFilter(
    categories: List<CategoryEntity>,
    selectedCategoryId: Int?,
    onCategorySelected: (Int?) -> Unit,
) {
    val dark = isSystemInDarkTheme()
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            NoteFilterChip(
                label = stringResource(R.string.all),
                color = NotePrimary,
                selected = selectedCategoryId == null,
                onClick = { onCategorySelected(null) },
                dark = dark,
            )
        }
        items(categories, key = { it.categoryId }) { category ->
            NoteFilterChip(
                label = category.name,
                color = category.color.toULongOrNull()?.let(::Color) ?: NotePrimary,
                selected = selectedCategoryId == category.categoryId,
                onClick = { onCategorySelected(category.categoryId) },
                dark = dark,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteFilterChip(
    label: String,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
    dark: Boolean,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium) },
        leadingIcon = {
            Box(
                Modifier
                    .size(8.dp)
                    .background(if (selected) Color.White else color, CircleShape),
            )
        },
        shape = RoundedCornerShape(13.dp),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = if (dark) Color(0xFF3A435C) else NoteBorder,
            selectedBorderColor = NotePrimary,
        ),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = if (dark) Color(0xFF191D2E) else Color.White,
            labelColor = if (dark) Color(0xFFAEB7C9) else NoteMuted,
            iconColor = color,
            selectedContainerColor = NotePrimary,
            selectedLabelColor = Color.White,
            selectedLeadingIconColor = Color.White,
        ),
    )
}

@Composable
private fun NoteListHeader(count: Int, isFiltered: Boolean, compact: Boolean) {
    val dark = isSystemInDarkTheme()
    val countText = when {
        count == 1 && isFiltered -> stringResource(R.string.note_result_single)
        count == 1 -> stringResource(R.string.note_count_single)
        isFiltered -> stringResource(R.string.note_result_count, count)
        else -> stringResource(R.string.note_count, count)
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.note_collection_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (dark) Color(0xFFF5F7FB) else NoteInk,
            )
            if (!compact) {
                Text(
                    text = stringResource(R.string.note_collection_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (dark) Color(0xFFAEB7C9) else NoteMuted,
                )
            }
        }
        Surface(
            color = NotePrimary.copy(alpha = if (dark) 0.24f else 0.10f),
            shape = RoundedCornerShape(18.dp),
        ) {
            Text(
                text = countText,
                modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = NotePrimaryLight,
            )
        }
    }
}

@Composable
private fun NoteEmptyState(
    isFiltered: Boolean,
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dark = isSystemInDarkTheme()
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = if (dark) Color(0xFF181C2B) else Color.White,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, if (dark) Color.White.copy(alpha = 0.08f) else NoteBorder),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Surface(
                modifier = Modifier.size(62.dp),
                shape = CircleShape,
                color = NotePrimary.copy(alpha = if (dark) 0.24f else 0.10f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.AutoMirrored.Rounded.Notes,
                        contentDescription = null,
                        tint = NotePrimaryLight,
                        modifier = Modifier.size(29.dp),
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = stringResource(if (isFiltered) R.string.note_empty_filtered else R.string.note_empty),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (dark) Color(0xFFF5F7FB) else NoteInk,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(
                    if (isFiltered) R.string.note_empty_filtered_subtitle else R.string.note_empty_subtitle,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = if (dark) Color(0xFFAEB7C9) else NoteMuted,
                textAlign = TextAlign.Center,
            )
            if (!isFiltered) {
                Spacer(Modifier.height(18.dp))
                Surface(
                    onClick = onCreateClick,
                    color = NotePrimary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(19.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.note_new_action), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteCard(
    note: NoteEntity,
    category: CategoryEntity?,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    compact: Boolean,
) {
    val dark = isSystemInDarkTheme()
    val foreground = if (dark) Color(0xFFF5F7FB) else NoteInk
    val muted = if (dark) Color(0xFFAEB7C9) else NoteMuted
    val categoryColor = category?.color?.toULongOrNull()?.let(::Color) ?: NotePrimary
    val icon = category?.let { IconData.getIconByKey(it.icon) } ?: Icons.AutoMirrored.Rounded.Notes
    val updatedAt = remember(note.updatedAt) {
        Instant.ofEpochMilli(note.updatedAt)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("MMM d, HH:mm"))
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = if (compact) 102.dp else 124.dp),
        onClick = onClick,
        color = if (dark) Color(0xFF181C2B) else Color.White,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, if (dark) Color.White.copy(alpha = 0.08f) else NoteBorder),
        shadowElevation = if (dark) 0.dp else 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(if (compact) 12.dp else 15.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Surface(
                modifier = Modifier.size(if (compact) 42.dp else 46.dp),
                color = categoryColor.copy(alpha = if (dark) 0.27f else 0.13f),
                shape = RoundedCornerShape(14.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = if (categoryColor.luminance() > 0.82f) NotePrimary else categoryColor,
                        modifier = Modifier.size(23.dp),
                    )
                }
            }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = foreground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!note.content.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = muted,
                        maxLines = if (compact) 1 else 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 20.sp,
                    )
                }
                Spacer(Modifier.height(if (compact) 7.dp else 10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = categoryColor.copy(alpha = if (dark) 0.22f else 0.09f),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(Modifier.size(6.dp).background(categoryColor, CircleShape))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = category?.name ?: stringResource(R.string.uncategorized),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (categoryColor.luminance() > 0.55f) foreground else categoryColor,
                            )
                        }
                    }
                    Spacer(Modifier.width(9.dp))
                    Text(
                        text = updatedAt,
                        style = MaterialTheme.typography.labelSmall,
                        color = muted,
                    )
                    if (note.reminderId != null) {
                        Spacer(Modifier.width(7.dp))
                        Icon(
                            Icons.Rounded.NotificationsNone,
                            contentDescription = stringResource(R.string.note_reminder_set),
                            tint = NoteTeal,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
            IconButton(onClick = onDeleteClick, modifier = Modifier.size(40.dp)) {
                Icon(
                    Icons.Rounded.DeleteOutline,
                    contentDescription = stringResource(R.string.note_delete),
                    tint = muted,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}
