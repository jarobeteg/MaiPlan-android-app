package com.example.maiplan.viewmodel.task

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.maiplan.database.entities.CategoryEntity
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.task.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModel(private val userId: Long, private val repository: TaskRepository,
    private val categorySource: Flow<List<CategoryEntity>>, private val conflicts: TaskConflictService,
    private val savedState: SavedStateHandle) : ViewModel() {
    private val retry = MutableStateFlow(0)
    private val _options = MutableStateFlow(TaskListOptions(
        view = TaskView.valueOf(savedState["view"] ?: TaskView.ACTIONABLE.name),
        search = savedState["search"] ?: "", categoryId = savedState["category"],
        sort = TaskSort.valueOf(savedState["sort"] ?: TaskSort.PLANNED_DAY.name)))
    val options = _options.asStateFlow()
    private val offset = MutableStateFlow(savedState["pageOffset"] ?: 0)
    val metadata = retry.flatMapLatest {
        combine(categorySource, conflicts.observeIssues(userId)) { categories, issues ->
            TaskListState(loading = false, categories = categories, issues = issues)
        }.catch { emit(TaskListState(loading = false, error = "Task sync information could not be loaded. Try again.")) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TaskListState())
    val collection = combine(retry, _options, offset) { attempt, options, page -> Triple(attempt, options, page) }.flatMapLatest { (_, options, page) ->
        combine(repository.observeTaskSummaries(userId, options.query(), options.readSort(), offset = page), metadata) { result, info ->
            info.copy(tasks = result.items, hasMore = result.hasMore, offset = result.offset,
                hasAnyTasks = result.hasAnyTasks, historyMayHaveMore = result.historyMayHaveMore)
        }.onStart { emit(metadata.value.copy(loading = true, offset = page)) }.catch { emit(TaskListState(loading = false,
            error = "Tasks could not be loaded. Try again.")) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TaskListState())
    private val _actions = MutableStateFlow(TaskActionsState())
    val actions = _actions.asStateFlow()
    private val details = mutableMapOf<Long, StateFlow<TaskDetailState>>()

    fun detail(id: Long): StateFlow<TaskDetailState> = details.getOrPut(id) {
        retry.flatMapLatest { repository.observeTask(id, userId).map { TaskDetailState(false, it) }
            .onStart { emit(TaskDetailState()) }.catch { emit(TaskDetailState(loading = false,
                error = "This Task could not be loaded. Try again.")) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TaskDetailState())
    }
    fun filter(value: TaskListOptions) {
        offset.value = 0; savedState["pageOffset"] = 0
        _options.value = value
        savedState["view"] = value.view.name; savedState["search"] = value.search
        savedState["category"] = value.categoryId; savedState["sort"] = value.sort.name
    }
    fun retry() { retry.value++ }
    fun nextPage() { if (collection.value.hasMore) setPage(offset.value + TASK_READ_PAGE_SIZE) }
    fun previousPage() { setPage((offset.value - TASK_READ_PAGE_SIZE).coerceAtLeast(0)) }
    private fun setPage(value: Int) { offset.value = value; savedState["pageOffset"] = value }
    fun dismissMessage() { _actions.update { it.copy(message = null) } }
    fun setStatus(id: Long, status: TaskStatus) = write(id) { repository.setTaskStatus(id, userId, status) }
    fun setStepStatus(taskId: Long, stepId: Long, status: TaskStatus) = write(taskId) { repository.setSubtaskStatus(stepId, userId, status) }
    fun reorderSteps(taskId: Long, original: List<Long>, order: List<Long>) = write(taskId) {
        repository.reorderSubtasks(taskId, userId, order, expectedOrder = original)
    }
    fun delete(id: Long, onDeleted: () -> Unit = {}) = write(id, onDeleted) { repository.softDeleteTask(id, userId) }
    fun deleteOccurrence(id: Long, onDeleted: (Long?) -> Unit) {
        var next: Long? = null
        write(id, { onDeleted(next) }) {
            val snapshot = repository.getTask(id, userId)
            val result = repository.softDeleteTask(id, userId)
            if (result is Result.Success && snapshot is Result.Success) snapshot.data.task.let { task ->
                task.seriesId?.let { next = repository.nextOccurrence(userId, java.util.UUID.fromString(it), checkNotNull(task.slotDate)) }
            }
            result
        }
    }
    fun deleteAll(id: Long, series: String, onDeleted: () -> Unit) = write(id, onDeleted) {
        repository.deleteAllSeries(id, userId, java.util.UUID.fromString(series))
    }
    fun loadMoreHistory() { viewModelScope.launch { runCatching { repository.loadMoreHistory(userId) }
        .onSuccess { more -> _actions.update { it.copy(message = if (more) "Earlier scheduled Tasks loaded. More history is available." else "All scheduled history is loaded.") } }
        .onFailure { _actions.update { it.copy(message = "History could not be loaded. Try again.") } } } }
    private fun write(id: Long, success: () -> Unit = {}, operation: suspend () -> Result<TaskWriteOutcome>) {
        if (id in _actions.value.busyIds) return
        _actions.update { it.copy(busyIds = it.busyIds + id, message = null) }
        viewModelScope.launch {
            try {
                when (val result = operation()) {
                    is Result.Success -> { _actions.update { it.copy(message = result.data.sideEffectWarning) }; success() }
                    is Result.Error -> _actions.update { it.copy(message = if (result.exception is TaskEditorChangedException)
                        "The checklist changed. Review its latest order and try again." else "The change could not be saved. Try again.") }
                    else -> _actions.update { it.copy(message = "The change could not be saved. Try again.") }
                }
            } finally { _actions.update { it.copy(busyIds = it.busyIds - id) } }
        }
    }
    fun resolve(issue: TaskSyncIssue, choice: TaskConflictChoice, onResolved: (Long) -> Unit) {
        if (issue.taskLocalId in _actions.value.busyIds) return
        _actions.update { it.copy(busyIds = it.busyIds + issue.taskLocalId, message = null) }
        viewModelScope.launch {
            try {
                val resolution = if (issue.seriesIssue) conflicts.resolveSeries(userId, issue.syncId, choice) else conflicts.resolve(userId, issue.syncId, choice)
                _actions.update { it.copy(message = resolution.warning) }
                onResolved(resolution.taskLocalId)
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { _actions.update { it.copy(message = e.message ?: "Sync could not be resolved. Try again.") } }
            finally { _actions.update { it.copy(busyIds = it.busyIds - issue.taskLocalId) } }
        }
    }
}

fun <T : ViewModel> taskViewModelFactory(create: (SavedStateHandle) -> T): ViewModelProvider.Factory =
    object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <VM : ViewModel> create(modelClass: Class<VM>, extras: CreationExtras): VM =
            create(extras.createSavedStateHandle()) as VM
    }
