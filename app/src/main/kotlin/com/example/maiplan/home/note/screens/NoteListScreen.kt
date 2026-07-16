package com.example.maiplan.home.note.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.maiplan.R
import com.example.maiplan.components.AdjustableSpacer
import com.example.maiplan.components.SearchFieldComponent
import com.example.maiplan.database.entities.CategoryEntity
import com.example.maiplan.database.entities.NoteEntity
import com.example.maiplan.home.navigation.HomeNavigationBar
import com.example.maiplan.utils.LocalUiScale
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
    onDeleteClick: (Int) -> Unit
) {
    val ui = LocalUiScale.current
    val context = LocalContext.current
    val notes by viewModel.noteList.observeAsState(emptyList())
    val categories by viewModel.categoryList.observeAsState(emptyList())
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }

    val filteredNotes = notes.filter { note ->
        val matchesCategory = selectedCategoryId == null || note.categoryId == selectedCategoryId
        val matchesSearch = note.title.contains(searchQuery, ignoreCase = true) ||
            note.content.orEmpty().contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Scaffold(
        topBar = { NotesTopBar() },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(ui.components.generalIconSize)
                )
            }
        },
        bottomBar = { HomeNavigationBar(rootNavController, context) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = ui.dimensions.largePaddingValue, vertical = ui.dimensions.mediumPaddingValue),
            verticalArrangement = Arrangement.spacedBy(ui.dimensions.mediumArrangementSpace)
        ) {
            SearchFieldComponent(R.string.note_search, searchQuery, 32) { searchQuery = it }

            NoteCategoryFilter(
                categories = categories,
                selectedCategoryId = selectedCategoryId,
                onCategorySelected = { selectedCategoryId = it }
            )

            NoteListSummary(
                count = filteredNotes.size,
                isFiltered = searchQuery.isNotBlank() || selectedCategoryId != null
            )

            if (filteredNotes.isEmpty()) {
                NoteEmptyState(
                    isFiltered = searchQuery.isNotBlank() || selectedCategoryId != null,
                    onCreateClick = onCreateClick
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(ui.dimensions.mediumArrangementSpace)
                ) {
                    items(filteredNotes, key = { it.noteId }) { note ->
                        NoteCard(
                            note = note,
                            category = categories.find { it.categoryId == note.categoryId },
                            onClick = { onNoteClick(note) },
                            onDeleteClick = { onDeleteClick(note.noteId) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotesTopBar() {
    val ui = LocalUiScale.current

    CenterAlignedTopAppBar(
        title = {
            Box(
                modifier = Modifier.fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.notes),
                    fontSize = ui.fonts.generalTopBarTitleSize,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier.height(ui.components.generalTopBarHeight)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteCategoryFilter(
    categories: List<CategoryEntity>,
    selectedCategoryId: Int?,
    onCategorySelected: (Int?) -> Unit
) {
    val ui = LocalUiScale.current

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ui.dimensions.smallArrangementSpace)
    ) {
        item {
            FilterChip(
                selected = selectedCategoryId == null,
                onClick = { onCategorySelected(null) },
                label = { Text(stringResource(R.string.all), fontSize = ui.fonts.generalTextSize) },
                shape = MaterialTheme.shapes.small,
                colors = neutralFilterChipColors()
            )
        }

        items(categories, key = { it.categoryId }) { category ->
            val color = category.color.toULongOrNull()?.let { Color(it) } ?: MaterialTheme.colorScheme.primary
            FilterChip(
                selected = selectedCategoryId == category.categoryId,
                onClick = { onCategorySelected(category.categoryId) },
                label = { Text(category.name, fontSize = ui.fonts.generalTextSize) },
                leadingIcon = {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(color, CircleShape)
                    )
                },
                shape = MaterialTheme.shapes.small,
                colors = neutralFilterChipColors()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun neutralFilterChipColors() = FilterChipDefaults.filterChipColors(
    containerColor = Color.Transparent,
    labelColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.78f),
    iconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.78f),
    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
    selectedLabelColor = MaterialTheme.colorScheme.onBackground,
    selectedLeadingIconColor = MaterialTheme.colorScheme.primary
)

@Composable
private fun NoteListSummary(count: Int, isFiltered: Boolean) {
    val ui = LocalUiScale.current
    val label = when {
        count == 1 && isFiltered -> stringResource(R.string.note_result_single)
        count == 1 -> stringResource(R.string.note_count_single)
        isFiltered -> stringResource(R.string.note_result_count, count)
        else -> stringResource(R.string.note_count, count)
    }

    Text(
        text = label,
        fontSize = ui.fonts.passwordStrengthTextSize,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
        fontWeight = FontWeight.Medium
    )
}

@Composable
private fun NoteEmptyState(
    isFiltered: Boolean,
    onCreateClick: () -> Unit
) {
    val ui = LocalUiScale.current

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ui.dimensions.mediumArrangementSpace)
        ) {
            Surface(
                modifier = Modifier.size(58.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Notes,
                        contentDescription = null,
                        modifier = Modifier.size(ui.components.generalIconSize)
                    )
                }
            }

            Text(
                text = if (isFiltered) stringResource(R.string.note_empty_filtered) else stringResource(R.string.note_empty),
                fontSize = ui.fonts.generalTextSize,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (!isFiltered) {
                Button(onClick = onCreateClick, shape = MaterialTheme.shapes.small) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(ui.components.generalIconSize)
                    )
                    AdjustableSpacer(ui.dimensions.mediumSpacer)
                    Text(text = stringResource(R.string.note_new), fontSize = ui.fonts.generalTextSize)
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
    onDeleteClick: () -> Unit
) {
    val ui = LocalUiScale.current
    val formatter = remember { DateTimeFormatter.ofPattern("MMM d, HH:mm") }
    val updatedAt = remember(note.updatedAt) {
        Instant.ofEpochMilli(note.updatedAt)
            .atZone(ZoneId.systemDefault())
            .format(formatter)
    }
    val categoryColor = category?.color?.toULongOrNull()?.let { Color(it) } ?: MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 112.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .heightIn(min = 112.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(categoryColor)
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp, top = 14.dp, end = 6.dp, bottom = 14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = note.title,
                        fontSize = ui.fonts.generalTextSize,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (!note.content.isNullOrBlank()) {
                        Text(
                            text = note.content,
                            fontSize = ui.fonts.generalTextSize,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            contentColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.78f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.10f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .background(categoryColor, CircleShape)
                                )
                                Text(
                                    text = category?.name ?: stringResource(R.string.uncategorized),
                                    fontSize = ui.fonts.passwordStrengthTextSize
                                )
                            }
                        }
                        Text(
                            text = updatedAt,
                            fontSize = ui.fonts.passwordStrengthTextSize,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
                        )
                    }
                }

                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
                    )
                }
            }
        }
    }
}
