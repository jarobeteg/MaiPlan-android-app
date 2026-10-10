package com.example.maiplan.viewmodel.task

import com.example.maiplan.database.entities.CategoryEntity
import com.example.maiplan.repository.task.*
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Locale
import java.util.UUID

enum class TaskView { ACTIONABLE, ALL, TODO, IN_PROGRESS, DONE, SKIPPED, CANCELLED }
enum class TaskSort { PLANNED_DAY, CATEGORY }
data class TaskListOptions(val view: TaskView = TaskView.ACTIONABLE, val search: String = "",
    val categoryId: Long? = null, val sort: TaskSort = TaskSort.PLANNED_DAY)
data class TaskSection(val label: String?, val date: LocalDate?, val tasks: List<TaskSummary>)
data class TaskListState(val loading: Boolean = true, val tasks: List<TaskSummary> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(), val issues: List<TaskSyncIssue> = emptyList(),
    val error: String? = null, val hasMore: Boolean = false, val offset: Int = 0,
    val hasAnyTasks: Boolean = false, val historyMayHaveMore: Boolean = false)
data class TaskActionsState(val busyIds: Set<Long> = emptySet(), val message: String? = null)
data class TaskDetailState(val loading: Boolean = true, val task: TaskSnapshot? = null, val error: String? = null)

fun visibleTasks(tasks: List<TaskSummary>, options: TaskListOptions): List<TaskSummary> = tasks.filter {
    val status = TaskStatus.fromCode(it.task.status)
    val matchesStatus = when (options.view) {
        TaskView.ALL -> true
        TaskView.ACTIONABLE -> !status.isTerminal
        else -> status.name == options.view.name
    }
    matchesStatus && when (options.categoryId) {
        null -> true
        -1L -> it.effectiveCategoryLocalId == null
        else -> it.effectiveCategoryLocalId == options.categoryId
    } && (options.search.isBlank() || it.task.title.lowercase(Locale.ROOT).contains(options.search.trim().lowercase(Locale.ROOT)) ||
        it.task.description.orEmpty().lowercase(Locale.ROOT).contains(options.search.trim().lowercase(Locale.ROOT)))
}

fun taskSections(tasks: List<TaskSummary>, options: TaskListOptions, categories: List<CategoryEntity>): List<TaskSection> {
    val visible = visibleTasks(tasks, options)
    val planned = compareBy<TaskSummary> { it.task.scheduledDate == null }
        .thenBy { it.task.scheduledDate }.thenBy { it.task.title.lowercase(Locale.ROOT) }.thenBy { it.task.syncId.toString() }
    val sorted = if (options.view == TaskView.DONE) visible.sortedWith(
        compareByDescending<TaskSummary> { it.task.completedDate }.thenBy { it.task.scheduledDate == null }
            .thenBy { it.task.scheduledDate }.thenBy { it.task.title.lowercase(Locale.ROOT) }.thenBy { it.task.syncId.toString() })
    else if (options.view == TaskView.SKIPPED || options.view == TaskView.CANCELLED) visible.sortedWith(
        planned)
    else visible.sortedWith(planned)
    if (options.sort == TaskSort.CATEGORY) {
        val names = categories.associate { it.categoryLocalId to it.name }
        return sorted.groupBy { it.effectiveCategoryLocalId }.entries.sortedWith(
            compareBy<Map.Entry<Long?, List<TaskSummary>>> { it.key == null }
                .thenBy { names[it.key].orEmpty().lowercase(Locale.ROOT) }.thenBy { it.key })
            .map { TaskSection(names[it.key], null, it.value) }
    }
    if (options.view == TaskView.DONE) return listOf(TaskSection(null, null, sorted)).filter { it.tasks.isNotEmpty() }
    return sorted.groupBy { it.task.scheduledDate }.map { TaskSection(null, it.key, it.value) }
}

fun TaskListOptions.query() = TaskQuery(
    when (categoryId) { null -> TaskCategoryFilter.All; -1L -> TaskCategoryFilter.Uncategorized; else -> TaskCategoryFilter.Selected(checkNotNull(categoryId)) },
    statuses = when (view) { TaskView.ALL -> TaskStatus.entries.toSet(); TaskView.ACTIONABLE -> setOf(TaskStatus.TODO, TaskStatus.IN_PROGRESS)
        else -> setOf(TaskStatus.valueOf(view.name)) }, search = search)
fun TaskListOptions.readSort() = if (sort == TaskSort.CATEGORY) TaskReadSort.CATEGORY
    else if (view == TaskView.DONE) TaskReadSort.COMPLETED_DAY else TaskReadSort.PLANNED_DAY

data class TaskDraft(val title: String = "", val description: String = "", val date: String = "",
    val minutes: String = "", val seconds: String = "", val categoryId: Long? = null) {
    fun content(): TaskContent {
        val duration = if (minutes.isBlank() && seconds.isBlank()) null else try {
            val m = BigDecimal(minutes.trim().ifEmpty { "0" })
            val s = BigDecimal(seconds.trim().ifEmpty { "0" })
            require(m >= BigDecimal.ZERO && s >= BigDecimal.ZERO) { "Duration cannot be negative" }
            m.multiply(BigDecimal(60_000)).add(s.multiply(BigDecimal(1000))).longValueExact()
        } catch (_: ArithmeticException) { throw IllegalArgumentException("Enter a duration with millisecond precision or less") }
        catch (_: NumberFormatException) { throw IllegalArgumentException("Enter a valid duration") }
        validateTaskEstimate(duration)
        val day = if (date.isBlank()) null else try { LocalDate.parse(date) }
            catch (_: Exception) { throw IllegalArgumentException("Enter a planned date as YYYY-MM-DD") }
        validateTaskDate(day)
        return TaskContent(normalizeTaskTitle(title), normalizeTaskDescription(description), day, duration, categoryId)
    }
    companion object {
        fun from(content: TaskContent) = TaskDraft(content.title, content.description.orEmpty(),
            content.scheduledDate?.toString().orEmpty(), content.estimatedMilliseconds?.let { (it / 60_000).toString() }.orEmpty(),
            content.estimatedMilliseconds?.let { BigDecimal(it % 60_000).movePointLeft(3).stripTrailingZeros().toPlainString() }.orEmpty(),
            content.categoryLocalId)
    }
}

data class TaskStepDraft(val key: String = UUID.randomUUID().toString(), val title: String = "",
    val minutes: String = "", val seconds: String = "", val status: Int = TaskStatus.TODO.code) {
    fun input(): ChecklistStepInput {
        val content = TaskDraft(title = title, minutes = minutes, seconds = seconds).content()
        return ChecklistStepInput(UUID.fromString(key), content.title, content.estimatedMilliseconds)
    }
    companion object {
        fun from(row: com.example.maiplan.database.entities.SubtaskEntity): TaskStepDraft {
            val duration = TaskDraft.from(TaskContent(row.title, estimatedMilliseconds = row.estimatedMilliseconds))
            return TaskStepDraft(row.syncId.toString(), row.title, duration.minutes, duration.seconds, row.status)
        }
    }
}

data class TaskMerge(val content: TaskContent, val revision: String, val changedFields: List<String>, val collisions: List<String>,
    val steps: List<TaskStepDraft> = emptyList(), val checklistRevision: String? = null,
    val reminder: TaskReminderEditorDraft? = null, val reminderRevision: String? = null, val reminderChanged: Boolean = false)

data class TaskStepMerge(val steps: List<TaskStepDraft>, val changed: Boolean, val collisions: List<String>)
fun mergeTaskSteps(base: List<TaskStepDraft>, draft: List<TaskStepDraft>, latest: List<TaskStepDraft>): TaskStepMerge {
    val baseline = base.associateBy { it.key }
    val local = draft.associateBy { it.key }
    val accepted = latest.associateBy { it.key }
    val collisions = mutableListOf<String>()
    val rows = linkedMapOf<String, TaskStepDraft>()
    for (mine in draft) {
        val old = baseline[mine.key]
        val current = accepted[mine.key]
        if (old != null && current == null) {
            if (mine.input() != old.input()) {
                val kept = mine.copy(key = UUID.randomUUID().toString(), status = TaskStatus.TODO.code)
                rows[mine.key] = kept
                collisions += "Deleted subtask ‘${mine.title}’ will be kept as a new To-do subtask"
            }
        } else if (old == null || current == null) rows[mine.key] = mine
        else {
            val mineContent = mine.input(); val oldContent = old.input(); val currentContent = current.input()
            val editedTitle = mineContent.title != oldContent.title
            val editedEstimate = mineContent.estimatedMilliseconds != oldContent.estimatedMilliseconds
            if (editedTitle && currentContent.title != oldContent.title && currentContent.title != mineContent.title) collisions += "Subtask title: ${mine.title}"
            if (editedEstimate && currentContent.estimatedMilliseconds != oldContent.estimatedMilliseconds && currentContent.estimatedMilliseconds != mineContent.estimatedMilliseconds) collisions += "Subtask estimate: ${mine.title}"
            rows[mine.key] = current.copy(title = if (editedTitle) mine.title else current.title,
                minutes = if (editedEstimate) mine.minutes else current.minutes, seconds = if (editedEstimate) mine.seconds else current.seconds)
        }
    }
    latest.filter { it.key !in baseline && it.key !in local }.forEach { rows[it.key] = it }
    base.filter { it.key !in local }.forEach { old ->
        val current = accepted[old.key]
        if (current != null && current.input() != old.input()) collisions += "Removed subtask changed elsewhere: ${current.title}"
    }
    val orderChanged = draft.map { it.key } != base.map { it.key }
    if (orderChanged && latest.filter { it.key in baseline }.map { it.key } != base.filter { it.key in accepted }.map { it.key }) collisions += "Subtask order"
    val order = if (orderChanged) draft.map { it.key } + latest.map { it.key }
        else latest.map { it.key } + draft.map { it.key }
    return TaskStepMerge(order.distinct().mapNotNull { rows[it] }, draft.map { it.input() } != base.map { it.input() }, collisions)
}
fun mergeTaskContent(base: TaskContent, draft: TaskContent, latest: TaskContent): TaskContent = TaskContent(
    if (draft.title != base.title) draft.title else latest.title,
    if (draft.description != base.description) draft.description else latest.description,
    if (draft.scheduledDate != base.scheduledDate) draft.scheduledDate else latest.scheduledDate,
    if (draft.estimatedMilliseconds != base.estimatedMilliseconds) draft.estimatedMilliseconds else latest.estimatedMilliseconds,
    if (draft.categoryLocalId != base.categoryLocalId) draft.categoryLocalId else latest.categoryLocalId,
)
fun changedTaskFields(a: TaskContent, b: TaskContent): List<String> = buildList {
    if (a.title != b.title) add("Title")
    if (a.description != b.description) add("Description")
    if (a.scheduledDate != b.scheduledDate) add("Planned date")
    if (a.estimatedMilliseconds != b.estimatedMilliseconds) add("Estimate")
    if (a.categoryLocalId != b.categoryLocalId) add("Category")
}
data class TaskEditorState(val loading: Boolean = true, val unavailable: Boolean = false, val loadFailed: Boolean = false,
    val draft: TaskDraft = TaskDraft(), val dirty: Boolean = false, val stale: Boolean = false,
    val saving: Boolean = false, val error: String? = null, val savedId: Long? = null,
    val warning: String? = null, val merge: TaskMerge? = null,
    val steps: List<TaskStepDraft> = emptyList(), val checklistEditable: Boolean = true,
    val reminder: TaskReminderEditorDraft = TaskReminderEditorDraft(),
    val repeat: TaskRepeatEditorDraft = TaskRepeatEditorDraft(), val isSeries: Boolean = false,
    val futureScope: Boolean = false, val cutoff: String = LocalDate.now().plusDays(1).toString())
