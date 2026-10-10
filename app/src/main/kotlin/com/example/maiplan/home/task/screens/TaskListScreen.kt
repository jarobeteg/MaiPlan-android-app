package com.example.maiplan.home.task.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.maiplan.R
import com.example.maiplan.category.CategoryActivity
import com.example.maiplan.home.navigation.HomeNavigationBar
import com.example.maiplan.viewmodel.task.*
import com.example.maiplan.repository.task.TaskStatus
import com.example.maiplan.theme.LocalAppDarkTheme
import com.example.maiplan.utils.LocalAdaptiveLayout

@Composable
fun TaskListScreen(rootNavController: NavHostController, viewModel: TaskViewModel,
    onCreate: () -> Unit, onOpen: (Long) -> Unit) {
    val context = LocalContext.current
    val state by viewModel.collection.collectAsStateWithLifecycle()
    val options by viewModel.options.collectAsStateWithLifecycle()
    val actions by viewModel.actions.collectAsStateWithLifecycle()
    val sections = remember(state.tasks, options, state.categories) { taskSections(state.tasks, options, state.categories) }
    var sortMenu by remember { mutableStateOf(false) }
    var loadedOnce by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    LaunchedEffect(state.offset, options.view, options.categoryId, options.sort) { listState.scrollToItem(0) }
    LaunchedEffect(state.loading) { if (!state.loading) loadedOnce = true }
    val compact = LocalAdaptiveLayout.current.let { it.isLandscape && it.isShort }
    Scaffold(containerColor = Color.Transparent, topBar = { TaskTopBar(stringResource(R.string.task_title), actions = {
        IconButton(onClick = { context.startActivity(Intent(context, CategoryActivity::class.java)) }) {
            Icon(Icons.Rounded.Category, stringResource(R.string.task_categories))
        }
    }) }, bottomBar = { HomeNavigationBar(rootNavController, context) },
        floatingActionButton = {
            if (compact) FloatingActionButton(onClick = onCreate, shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary) {
                Icon(Icons.Rounded.Add, stringResource(R.string.task_new))
            } else ExtendedFloatingActionButton(onClick = onCreate, shape = RoundedCornerShape(17.dp),
                containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Rounded.Add, null) },
                text = { Text(stringResource(R.string.task_new), fontWeight = FontWeight.Bold) })
        }) { padding ->
        if ((state.loading && !loadedOnce) || state.error != null) {
            TaskLoadStateInPadding(padding, state.loading, state.error.orEmpty(), viewModel::retry)
        } else Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            LazyColumn(Modifier.widthIn(max = 1000.dp).fillMaxWidth().testTag("task-list"),
                state = listState,
                contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = if (compact) 8.dp else 14.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item("filters") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (!compact) TaskHeading(stringResource(R.string.task_collection_title), stringResource(R.string.task_collection_subtitle))
                        TaskTextField(value = options.search, onValueChange = { viewModel.filter(options.copy(search = it)) },
                            modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text(stringResource(R.string.task_search)) },
                            leadingIcon = { Icon(Icons.Rounded.Search, null) }, trailingIcon = {
                                if (options.search.isNotEmpty()) IconButton(onClick = { viewModel.filter(options.copy(search = "")) }) {
                                    Icon(Icons.Rounded.Close, stringResource(R.string.task_clear_search))
                                }
                            })
                        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TaskView.entries.forEach { view -> TaskFilterChip(selected = options.view == view,
                                onClick = { viewModel.filter(options.copy(view = view)) }, label = { Text(taskViewLabel(view)) }) }
                        }
                        Column {
                            TaskCategoryPicker(state.categories, options.categoryId, includeAll = true,
                                modifier = Modifier.fillMaxWidth()) { viewModel.filter(options.copy(categoryId = it)) }
                            Box(Modifier.align(Alignment.End)) {
                                TextButton(onClick = { sortMenu = true }) { Text(stringResource(R.string.task_sort,
                                    stringResource(if (options.sort == TaskSort.PLANNED_DAY) R.string.task_by_day else R.string.task_by_category)))
                                    Spacer(Modifier.width(6.dp)); Icon(Icons.Rounded.ExpandMore, null, modifier = Modifier.size(18.dp)) }
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
                    TaskPanel { Column(Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.task_history_partial), style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        TextButton(onClick = viewModel::loadMoreHistory) { Text(stringResource(R.string.task_load_history)) }
                    } }
                }
                items(state.issues.filter { it.state.needsAttention() }, key = { "sync-${it.syncId}" }) { issue ->
                    TaskSyncPanel(issue, issue.taskLocalId !in actions.busyIds) { choice -> viewModel.resolve(issue, choice) { id ->
                        if (id != issue.taskLocalId) onOpen(id)
                    } }
                }
                if (!state.loading && !state.hasAnyTasks) item("empty") {
                    TaskEmptyState(false, onCreate) { viewModel.filter(TaskListOptions(view = TaskView.ALL)) }
                } else if (!state.loading && sections.isEmpty()) item("no-match") {
                    TaskEmptyState(true, onCreate) { viewModel.filter(TaskListOptions(view = TaskView.ALL)) }
                }
                sections.forEachIndexed { index, section ->
                    if (options.view != TaskView.DONE || options.sort == TaskSort.CATEGORY) item("section-$index") {
                        Text(if (options.sort == TaskSort.CATEGORY) section.label ?: stringResource(R.string.uncategorized)
                            else section.date?.let(::taskDay) ?: stringResource(R.string.task_unscheduled),
                            style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp))
                    }
                    items(section.tasks, key = { it.task.syncId.toString() }) { snapshot ->
                        Surface(onClick = { onOpen(snapshot.task.taskLocalId) }, modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            shadowElevation = if (LocalAppDarkTheme.current) 0.dp else 2.dp) {
                            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    val status = TaskStatus.fromCode(snapshot.task.status)
                                    TaskSectionIcon(taskStatusIcon(status), taskStatusColor(status))
                                    Text(snapshot.task.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                                        maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                }
                                TaskMetadata(snapshot, state.categories.find { it.categoryLocalId == snapshot.effectiveCategoryLocalId },
                                    state.issues.find { it.syncId == snapshot.task.syncId }?.state)
                                Box(Modifier.align(Alignment.End)) {
                                    TaskStatusControl(snapshot, snapshot.task.taskLocalId !in actions.busyIds) { viewModel.setStatus(snapshot.task.taskLocalId, it) }
                                }
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
private fun TaskEmptyState(filtered: Boolean, onCreate: () -> Unit, onClear: () -> Unit) {
    TaskPanel {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)) {
            TaskSectionIcon(if (filtered) Icons.Rounded.Search else Icons.Rounded.TaskAlt)
            Text(stringResource(if (filtered) R.string.task_no_matches else R.string.task_empty),
                style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text(stringResource(if (filtered) R.string.task_no_matches_help else R.string.task_empty_help),
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            Button(onClick = if (filtered) onClear else onCreate, shape = RoundedCornerShape(15.dp),
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                Text(stringResource(if (filtered) R.string.task_clear_filters else R.string.task_new))
            }
        }
    }
}

@Composable
private fun TaskLoadStateInPadding(padding: PaddingValues, loading: Boolean, message: String, retry: () -> Unit) {
    Box(Modifier.fillMaxSize().padding(padding)) { TaskLoadState(loading, message, retry) }
}
