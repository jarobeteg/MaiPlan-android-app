package com.example.maiplan.viewmodel.task

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.task.*
import com.google.gson.Gson
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TaskEditorViewModel(private val userId: Long, private val taskId: Long?,
    private val repository: TaskRepository, private val savedState: SavedStateHandle) : ViewModel() {
    private val gson = Gson()
    private fun restore(key: String): TaskDraft? = savedState.get<String>(key)?.let { gson.fromJson(it, TaskDraft::class.java) }
    private var baseline = restore("baseline")
    private var revision: String? = savedState["revision"]
    private fun restoreSteps(key: String): List<TaskStepDraft> = savedState.get<String>(key)?.let {
        gson.fromJson(it, Array<TaskStepDraft>::class.java).toList()
    } ?: emptyList()
    private var baselineSteps = restoreSteps("baselineSteps")
    private var checklistRevision: String? = savedState["checklistRevision"]
    private fun restoreReminder(key: String) = savedState.get<String>(key)?.let { gson.fromJson(it, TaskReminderEditorDraft::class.java) }
    private var baselineReminder = restoreReminder("baselineReminder") ?: TaskReminderEditorDraft()
    private var reminderRevision: String? = savedState["reminderRevision"]
    private var baselineRepeat = savedState.get<String>("baselineRepeat")?.let { gson.fromJson(it, TaskRepeatEditorDraft::class.java) } ?: TaskRepeatEditorDraft()
    private var seriesBaseline: String? = savedState["seriesBaseline"]
    private var latest: TaskSnapshot? = null
    private var observation: Job? = null
    private val _state = MutableStateFlow(TaskEditorState(draft = restore("draft") ?: TaskDraft(),
        savedId = savedState["savedId"], steps = restoreSteps("steps"), reminder = restoreReminder("reminder") ?: baselineReminder,
        repeat = savedState.get<String>("repeat")?.let { gson.fromJson(it, TaskRepeatEditorDraft::class.java) } ?: baselineRepeat,
        futureScope = savedState["futureScope"] ?: false, cutoff = savedState["cutoff"] ?: java.time.LocalDate.now().plusDays(1).toString()))
    val state = _state.asStateFlow()
    init {
        if (taskId == null) {
            if (baseline == null) baseline = TaskDraft().also { savedState["baseline"] = gson.toJson(it) }
            _state.update { it.copy(loading = false, dirty = dirty(it)) }
        } else observe()
    }
    fun retry() { observe() }
    private fun observe() {
        if (taskId == null) return
        observation?.cancel()
        observation = viewModelScope.launch {
            _state.update { it.copy(loading = true, loadFailed = false, error = null) }
            repository.observeTask(taskId, userId).catch {
                _state.update { it.copy(loading = false, loadFailed = true, error = "This Task could not be loaded. Try again.") }
            }.collect { snapshot ->
                latest = snapshot
                if (snapshot != null && baseline == null) loadLatest()
                _state.update { it.copy(loading = false, loadFailed = false, unavailable = snapshot == null,
                    stale = snapshot != null && (taskEditorRevision(snapshot.task) != revision || checklistEditorRevision(snapshot.subtasks) != checklistRevision || taskReminderRevision(snapshot.reminder) != reminderRevision),
                    checklistEditable = snapshot != null && (it.futureScope || !TaskStatus.fromCode(snapshot.task.status).isTerminal),
                    isSeries = snapshot?.task?.seriesId != null,
                    dirty = dirty(it)) }
            }
        }
    }
    fun edit(draft: TaskDraft) {
        if (_state.value.saving || _state.value.savedId != null) return
        savedState["draft"] = gson.toJson(draft)
        _state.update { it.copy(draft = draft, dirty = dirty(it.copy(draft = draft)), error = null, merge = null) }
    }
    fun editSteps(steps: List<TaskStepDraft>) {
        if (_state.value.saving || _state.value.savedId != null || !_state.value.checklistEditable) return
        require(steps.map { it.key }.distinct().size == steps.size)
        savedState["steps"] = gson.toJson(steps)
        _state.update { it.copy(steps = steps, dirty = dirty(it.copy(steps = steps)), error = null, merge = null) }
    }
    private fun dirty(state: TaskEditorState) = state.draft != baseline || state.steps != baselineSteps || state.reminder != baselineReminder || state.repeat != baselineRepeat
    fun editRepeat(value: TaskRepeatEditorDraft) {
        if (_state.value.saving) return
        savedState["repeat"] = gson.toJson(value)
        _state.update { it.copy(repeat = value, dirty = dirty(it.copy(repeat = value)), error = null) }
    }
    fun editCutoff(value: String) { savedState["cutoff"] = value; _state.update { it.copy(cutoff = value, dirty = true) } }
    fun chooseFutureScope(future: Boolean) {
        if (_state.value.saving) return
        if (!future) { loadLatest(); return }
        val task = latest?.task ?: return
        val id = task.seriesId?.let(java.util.UUID::fromString) ?: return
        viewModelScope.launch {
            runCatching {
                val row = checkNotNull(repository.seriesDefinition(userId, id))
                check(row.deletedAt == null && row.pendingOperationJson == null) { "Wait for the previous series operation to finish syncing." }
                seriesBaseline = row.definitionJson; savedState["seriesBaseline"] = seriesBaseline
                val revision = TaskSeriesDefinition.decode(row.definitionJson).revisions.last()
                val content = TaskContent(revision.title, revision.description, revision.rule().anchorDate,
                    revision.estimated_time, repository.categoryLocalId(userId, revision.category_sync_id))
                baseline = TaskDraft.from(content)
                baselineSteps = revision.steps.map { step ->
                    val duration = TaskDraft.from(TaskContent(step.title, estimatedMilliseconds = step.estimated_time))
                    TaskStepDraft(step.key, step.title, duration.minutes, duration.seconds)
                }
                baselineRepeat = TaskRepeatEditorDraft.from(revision)
                savedState["baseline"] = gson.toJson(baseline); savedState["draft"] = gson.toJson(baseline)
                savedState["baselineSteps"] = gson.toJson(baselineSteps); savedState["steps"] = gson.toJson(baselineSteps)
                savedState["baselineRepeat"] = gson.toJson(baselineRepeat); savedState["repeat"] = gson.toJson(baselineRepeat)
                savedState["futureScope"] = true
                _state.update { it.copy(futureScope = true, draft = baseline!!, steps = baselineSteps,
                    repeat = baselineRepeat, checklistEditable = true, dirty = false, stale = false, error = null) }
            }.onFailure { error -> _state.update { it.copy(error = error.message) } }
        }
    }
    fun editReminder(draft: TaskReminderEditorDraft) {
        if (_state.value.saving || _state.value.savedId != null) return
        savedState["reminder"] = gson.toJson(draft)
        _state.update { it.copy(reminder = draft, dirty = dirty(it.copy(reminder = draft)), error = null, merge = null) }
    }
    fun loadLatest() {
        if (_state.value.saving) return
        val snapshot = latest ?: return
        val task = snapshot.task
        baselineRepeat = if (task.seriesId == null) TaskRepeatEditorDraft() else TaskRepeatEditorDraft.from(TaskSeriesRevision(
            task.generationRevision ?: 1, checkNotNull(task.repeatAnchorDate).toEpochDay(), task.title, task.description,
            task.estimatedMilliseconds, repeat_unit = checkNotNull(task.repeatUnit), repeat_interval = checkNotNull(task.repeatInterval),
            repeat_weekdays = task.repeatWeekdays, repeat_anchor_date = task.repeatAnchorDate.toEpochDay(), repeat_end_date = task.repeatEndDate?.toEpochDay(),
            reminder = task.relativeReminderJson?.let(TaskRelativeReminder::decode)))
        savedState["baselineRepeat"] = gson.toJson(baselineRepeat); savedState["repeat"] = gson.toJson(baselineRepeat); savedState["futureScope"] = false
        baseline = TaskDraft.from(snapshot.editableContent())
        revision = taskEditorRevision(snapshot.task)
        baselineSteps = snapshot.subtasks.map(TaskStepDraft::from)
        checklistRevision = checklistEditorRevision(snapshot.subtasks)
        baselineReminder = TaskReminderEditorDraft.from(snapshot.reminder)
        reminderRevision = taskReminderRevision(snapshot.reminder)
        savedState["baselineReminder"] = gson.toJson(baselineReminder); savedState["reminder"] = gson.toJson(baselineReminder)
        savedState["reminderRevision"] = reminderRevision
        savedState["baselineSteps"] = gson.toJson(baselineSteps); savedState["steps"] = gson.toJson(baselineSteps)
        savedState["checklistRevision"] = checklistRevision
        savedState["baseline"] = gson.toJson(baseline); savedState["draft"] = gson.toJson(baseline)
        savedState["revision"] = revision
        _state.update { it.copy(draft = baseline!!, steps = baselineSteps, reminder = baselineReminder, repeat = baselineRepeat,
            futureScope = false, isSeries = snapshot.task.seriesId != null, stale = false, dirty = false, error = null, merge = null) }
    }
    fun prepareMerge() {
        if (_state.value.saving) return
        val current = latest ?: return
        try {
            val draft = _state.value.draft.content()
            val base = requireNotNull(baseline).content()
            val accepted = current.editableContent()
            val mine = changedTaskFields(base, draft)
            val theirs = changedTaskFields(base, accepted)
            val collisions = mine.intersect(theirs.toSet()).intersect(changedTaskFields(draft, accepted).toSet()).toList()
            val checklist = mergeTaskSteps(baselineSteps, _state.value.steps, current.subtasks.map(TaskStepDraft::from))
            val acceptedReminder = TaskReminderEditorDraft.from(current.reminder)
            val mineReminder = _state.value.reminder
            val editedReminder = mineReminder != baselineReminder
            val mergedReminder = if (editedReminder) mineReminder else acceptedReminder
            val reminderCollisions = if (editedReminder && acceptedReminder != baselineReminder && mineReminder != acceptedReminder) listOf("Reminder") else emptyList()
            require(!TaskStatus.fromCode(current.task.status).isTerminal || !checklist.changed) { "Reopen the Task before saving checklist changes. Your draft is still here." }
            _state.update { it.copy(merge = TaskMerge(mergeTaskContent(base, draft, accepted),
                taskEditorRevision(current.task), mine + (if (checklist.changed) listOf("Subtasks") else emptyList()) + (if (editedReminder) listOf("Reminder") else emptyList()),
                collisions + checklist.collisions + reminderCollisions, checklist.steps, checklistEditorRevision(current.subtasks),
                mergedReminder, taskReminderRevision(current.reminder), mergedReminder != acceptedReminder), error = null) }
        } catch (e: IllegalArgumentException) { _state.update { it.copy(error = e.message) } }
    }
    fun cancelMerge() { _state.update { it.copy(merge = null) } }
    fun save(merge: TaskMerge? = null) {
        val state = _state.value
        if (state.saving || state.savedId != null || state.loading || state.unavailable || state.loadFailed ||
            (taskId != null && revision == null)) return
        if (state.stale && merge == null && !state.futureScope) { prepareMerge(); return }
        val content = try { merge?.content ?: state.draft.content() }
            catch (e: IllegalArgumentException) { _state.update { it.copy(error = e.message) }; return }
        val steps = try { (merge?.steps ?: state.steps).map { it.input() } }
            catch (e: IllegalArgumentException) { _state.update { it.copy(error = e.message) }; return }
        val reminderDraft = merge?.reminder ?: state.reminder
        val reminderChanged = merge?.reminderChanged ?: (state.reminder != baselineReminder)
        val reminderChange = try {
            if (state.repeat.relative) TaskReminderChange.Relative(checkNotNull(state.repeat.reminder()))
            else if (taskId != null && !reminderChanged && state.repeat.relative == baselineRepeat.relative) TaskReminderChange.Keep
            else if (!reminderDraft.enabled) TaskReminderChange.Remove
            else TaskReminderChange.Set(reminderDraft.reminder())
        } catch (e: RuntimeException) { _state.update { it.copy(error = e.message ?: "Check the reminder date, time and time zone") }; return }
        _state.update { it.copy(saving = true, error = null, merge = null) }
        viewModelScope.launch {
            val result = try {
                if ((taskId == null && state.repeat.unit != 0) || state.futureScope) {
                    val template = state.repeat.revision(content, merge?.steps ?: state.steps, repository.categorySyncId(userId, content.categoryLocalId))
                    if (taskId == null) repository.createSeries(userId, CreateTaskSeriesInput(template))
                    else repository.editFuture(userId, java.util.UUID.fromString(checkNotNull(latest?.task?.seriesId)), template,
                        checkNotNull(seriesBaseline), java.time.LocalDate.parse(state.cutoff), taskId)
                } else if (taskId == null) repository.createTask(userId, CreateTaskInput(content,
                subtasks = steps.map { SubtaskDraft(it.title, it.estimatedMilliseconds) }, reminder = (reminderChange as? TaskReminderChange.Set)?.draft))
                else repository.updateTask(userId, UpdateTaskInput(taskId, content, expectedRevision = merge?.revision ?: revision,
                    checklist = steps, expectedChecklistRevision = merge?.checklistRevision ?: checklistRevision,
                    reminder = reminderChange, expectedReminderRevision = merge?.reminderRevision ?: reminderRevision))
            } catch (error: Exception) { Result.Error(error) }
            when (result) {
                is Result.Success -> {
                    savedState["savedId"] = result.data.task.taskLocalId
                    _state.update { it.copy(saving = false, savedId = result.data.task.taskLocalId,
                        warning = result.data.sideEffectWarning, dirty = false) }
                }
                is Result.Error -> _state.update { it.copy(saving = false,
                    stale = it.stale || result.exception is TaskEditorChangedException,
                    error = if (result.exception is TaskEditorChangedException) "This Task changed. Reload or review your edits before saving."
                        else if (result.exception is IllegalArgumentException || result.exception is IllegalStateException) result.exception.message
                        else "This Task could not be saved. Your draft is still here. Try again.") }
                else -> _state.update { it.copy(saving = false, error = "This Task could not be saved. Try again.") }
            }
        }
    }
}
