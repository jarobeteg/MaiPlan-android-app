package com.example.maiplan.home.task.screens

import android.content.Intent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.maiplan.R
import com.example.maiplan.category.CategoryActivity
import com.example.maiplan.home.navigation.HomeNavigationBar
import com.example.maiplan.viewmodel.task.*

@Composable
fun TaskListScreen(rootNavController: NavHostController, viewModel: TaskViewModel,
    onCreate: () -> Unit, onOpen: (Long) -> Unit) {
    val context = LocalContext.current
    val state by viewModel.collection.collectAsStateWithLifecycle()
    val options by viewModel.options.collectAsStateWithLifecycle()
    val actions by viewModel.actions.collectAsStateWithLifecycle()
    val sections = remember(state.tasks, options, state.categories) { taskSections(state.tasks, options, state.categories) }
    var categoryMenu by remember { mutableStateOf(false) }
    var sortMenu by remember { mutableStateOf(false) }
    var loadedOnce by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    LaunchedEffect(state.offset, options.view, options.categoryId, options.sort) { listState.scrollToItem(0) }
    LaunchedEffect(state.loading) { if (!state.loading) loadedOnce = true }
    Scaffold(topBar = { TaskTopBar(stringResource(R.string.task_title), actions = {
        IconButton(onClick = { context.startActivity(Intent(context, CategoryActivity::class.java)) }) {
            Icon(Icons.Rounded.Category, stringResource(R.string.task_categories))
        }
    }) }, bottomBar = { HomeNavigationBar(rootNavController, context) },
        floatingActionButton = { FloatingActionButton(onClick = onCreate) { Icon(Icons.Rounded.Add, stringResource(R.string.task_new)) } }) { padding ->
        if ((state.loading && !loadedOnce) || state.error != null) {
            TaskLoadStateInPadding(padding, state.loading, state.error.orEmpty(), viewModel::retry)
        } else Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            LazyColumn(Modifier.widthIn(max = 1000.dp).fillMaxWidth().testTag("task-list"),
                state = listState,
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item("filters") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(value = options.search, onValueChange = { viewModel.filter(options.copy(search = it)) },
                            modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text(stringResource(R.string.task_search)) })
                        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TaskView.entries.forEach { view -> FilterChip(selected = options.view == view,
                                onClick = { viewModel.filter(options.copy(view = view)) }, label = { Text(taskViewLabel(view)) }) }
                        }
                        Column {
                            Box {
                                OutlinedButton(onClick = { categoryMenu = true }) { Text(when (options.categoryId) {
                                    null -> stringResource(R.string.task_all_categories)
                                    -1L -> stringResource(R.string.uncategorized)
                                    else -> state.categories.find { it.categoryLocalId == options.categoryId }?.name ?: stringResource(R.string.uncategorized)
                                }, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                                DropdownMenu(expanded = categoryMenu, onDismissRequest = { categoryMenu = false }) {
                                    DropdownMenuItem(text = { Text(stringResource(R.string.task_all_categories)) }, onClick = {
                                        categoryMenu = false; viewModel.filter(options.copy(categoryId = null)) })
                                    DropdownMenuItem(text = { Text(stringResource(R.string.uncategorized)) }, onClick = {
                                        categoryMenu = false; viewModel.filter(options.copy(categoryId = -1)) })
                                    state.categories.forEach { category -> DropdownMenuItem(text = { Text(category.name) }, onClick = {
                                        categoryMenu = false; viewModel.filter(options.copy(categoryId = category.categoryLocalId)) }) }
                                }
                            }
                            Box {
                                TextButton(onClick = { sortMenu = true }) { Text(stringResource(R.string.task_sort,
                                    stringResource(if (options.sort == TaskSort.PLANNED_DAY) R.string.task_by_day else R.string.task_by_category))) }
                                DropdownMenu(expanded = sortMenu, onDismissRequest = { sortMenu = false }) {
                                    TaskSort.entries.forEach { sort -> DropdownMenuItem(text = { Text(stringResource(
                                        if (sort == TaskSort.PLANNED_DAY) R.string.task_by_day else R.string.task_by_category)) }, onClick = {
                                        sortMenu = false; viewModel.filter(options.copy(sort = sort)) }) }
                                }
                            }
                        }
                    }
                }
                actions.message?.let { message -> item("message") { TaskMessage(message, viewModel::dismissMessage) } }
                if (state.loading) item("loading-window") { CircularProgressIndicator() }
                if (state.historyMayHaveMore) item("history") {
                    Column { Text(stringResource(R.string.task_history_partial), style = MaterialTheme.typography.bodySmall)
                        TextButton(onClick = viewModel::loadMoreHistory) { Text(stringResource(R.string.task_load_history)) } }
                }
                items(state.issues.filter { it.state.needsAttention() }, key = { "sync-${it.syncId}" }) { issue ->
                    TaskSyncPanel(issue, issue.taskLocalId !in actions.busyIds) { choice -> viewModel.resolve(issue, choice) { id ->
                        if (id != issue.taskLocalId) onOpen(id)
                    } }
                }
                if (!state.loading && !state.hasAnyTasks) item("empty") {
                    Column(Modifier.fillMaxWidth().padding(vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.task_empty), style = MaterialTheme.typography.titleLarge)
                        Text(stringResource(R.string.task_empty_help))
                        Button(onClick = onCreate) { Text(stringResource(R.string.task_new)) }
                    }
                } else if (!state.loading && sections.isEmpty()) item("no-match") {
                    Column(Modifier.fillMaxWidth().padding(vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.task_no_matches))
                        TextButton(onClick = { viewModel.filter(TaskListOptions(view = TaskView.ALL)) }) { Text(stringResource(R.string.task_clear_filters)) }
                    }
                }
                sections.forEachIndexed { index, section ->
                    if (options.view != TaskView.DONE || options.sort == TaskSort.CATEGORY) item("section-$index") {
                        Text(if (options.sort == TaskSort.CATEGORY) section.label ?: stringResource(R.string.uncategorized)
                            else section.date?.let(::taskDay) ?: stringResource(R.string.task_unscheduled), style = MaterialTheme.typography.titleMedium)
                    }
                    items(section.tasks, key = { it.task.syncId.toString() }) { snapshot ->
                        ElevatedCard(onClick = { onOpen(snapshot.task.taskLocalId) }, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(snapshot.task.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                TaskMetadata(snapshot, state.categories.find { it.categoryLocalId == snapshot.effectiveCategoryLocalId },
                                    state.issues.find { it.syncId == snapshot.task.syncId }?.state)
                                TaskStatusControl(snapshot, snapshot.task.taskLocalId !in actions.busyIds) { viewModel.setStatus(snapshot.task.taskLocalId, it) }
                            }
                        }
                    }
                }
                if (state.offset > 0 || state.hasMore) item("pages") {
                    Column {
                        Text(stringResource(R.string.task_page_number, state.offset / 100 + 1), style = MaterialTheme.typography.bodySmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            TextButton(enabled = state.offset > 0, onClick = viewModel::previousPage) { Text(stringResource(R.string.task_previous_page)) }
                            TextButton(enabled = state.hasMore, onClick = viewModel::nextPage) { Text(stringResource(R.string.task_next_page)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskLoadStateInPadding(padding: PaddingValues, loading: Boolean, message: String, retry: () -> Unit) {
    Box(Modifier.fillMaxSize().padding(padding)) { TaskLoadState(loading, message, retry) }
}
