package com.example.maiplan.home.note.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.maiplan.R
import com.example.maiplan.components.AdjustableSpacer
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
        topBar = { NotesTopBar(onCreateClick) },
        bottomBar = { HomeNavigationBar(rootNavController, context) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { if (it.length <= 64) searchQuery = it },
                placeholder = {
                    Text(
                        text = stringResource(R.string.note_search),
                        fontSize = ui.fonts.generalTextSize
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        modifier = Modifier.size(ui.components.generalIconSize)
                    )
                },
                singleLine = true,
                textStyle = TextStyle(fontSize = ui.fonts.generalTextSize),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ui.components.generalFieldHeight),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onBackground,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

            NoteCategoryFilter(
                categories = categories,
                selectedCategoryId = selectedCategoryId,
                onCategorySelected = { selectedCategoryId = it }
            )

            if (filteredNotes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.note_empty),
                        fontSize = ui.fonts.generalTextSize,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
private fun NotesTopBar(onCreateClick: () -> Unit) {
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
        actions = {
            IconButton(
                onClick = onCreateClick,
                modifier = Modifier.size(ui.dimensions.generalTouchTarget)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(ui.components.generalTopBarIconSize),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
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

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        FilterChip(
            selected = selectedCategoryId == null,
            onClick = { onCategorySelected(null) },
            label = { Text(stringResource(R.string.all), fontSize = ui.fonts.generalTextSize) }
        )

        categories.forEach { category ->
            FilterChip(
                selected = selectedCategoryId == category.categoryId,
                onClick = { onCategorySelected(category.categoryId) },
                label = { Text(category.name, fontSize = ui.fonts.generalTextSize) }
            )
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Filled.Notes,
                contentDescription = null,
                modifier = Modifier.size(ui.components.cardIconSize),
                tint = MaterialTheme.colorScheme.primary
            )

            AdjustableSpacer(ui.dimensions.mediumSpacer)

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
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = category?.name ?: stringResource(R.string.uncategorized),
                        fontSize = ui.fonts.passwordStrengthTextSize,
                        color = category?.color?.toULongOrNull()?.let { Color(it) }
                            ?: MaterialTheme.colorScheme.primary
                    )
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
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
