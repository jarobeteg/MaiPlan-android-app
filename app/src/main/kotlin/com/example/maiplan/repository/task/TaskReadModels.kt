package com.example.maiplan.repository.task

import com.example.maiplan.database.dao.TaskSummaryRecord
import com.example.maiplan.database.entities.TaskEntity
import java.time.LocalDate
import java.util.UUID

const val TASK_READ_PAGE_SIZE = 100
enum class TaskReadSort { PLANNED_DAY, CATEGORY, COMPLETED_DAY }

data class TaskCategoryPresentation(val localId: Long, val syncId: UUID, val name: String, val color: String, val icon: String)
sealed interface TaskReminderPresentation {
    data class Absolute(val triggerAtMillis: Long, val zoneId: String, val enabled: Boolean) : TaskReminderPresentation
    data class Relative(val rule: TaskRelativeReminder) : TaskReminderPresentation
}

data class TaskSummary(val task: TaskEntity, val category: TaskCategoryPresentation?, val stepCount: Int,
    val finishedSteps: Int, val doneSteps: Int, val estimate: TaskEstimate, val reminder: TaskReminderPresentation?) {
    val effectiveCategoryLocalId get() = category?.localId
    fun dayItem() = TaskDayItem(task.syncId, task.scheduledDate, task.title, category,
        TaskStatus.fromCode(task.status), estimate, stepCount, finishedSteps, doneSteps, reminder)
}

data class TaskDayItem(val taskId: UUID, val plannedDate: LocalDate?, val title: String,
    val category: TaskCategoryPresentation?, val status: TaskStatus, val estimate: TaskEstimate,
    val stepCount: Int, val finishedSteps: Int, val doneSteps: Int, val reminder: TaskReminderPresentation?)

data class TaskSummaryPage(val items: List<TaskSummary>, val offset: Int, val hasMore: Boolean,
    val hasAnyTasks: Boolean, val historyMayHaveMore: Boolean)
data class TaskDayPage(val items: List<TaskDayItem>, val offset: Int, val hasMore: Boolean)

internal fun TaskSummaryRecord.summary(): TaskSummary = TaskSummary(task,
    categoryLocalId?.let { TaskCategoryPresentation(it, checkNotNull(categorySyncId), checkNotNull(categoryName),
        checkNotNull(categoryColor), checkNotNull(categoryIcon)) }, stepCount, finishedSteps, doneSteps,
    TaskEstimate(task.estimatedMilliseconds ?: stepEstimate, task.estimatedMilliseconds == null && knownEstimates < stepCount,
        task.estimatedMilliseconds != null),
    if (reminderTime != null) TaskReminderPresentation.Absolute(reminderTime, checkNotNull(reminderZone), reminderStatus == 1)
    else task.relativeReminderJson?.let { TaskReminderPresentation.Relative(TaskRelativeReminder.decode(it)) })

fun TaskSnapshot.summary(category: TaskCategoryPresentation? = null) = TaskSummary(task, category, subtasks.size,
    finishedSteps, doneSteps, estimate, reminder?.let { TaskReminderPresentation.Absolute(it.reminderTime, it.zoneId, it.status == 1) }
        ?: task.relativeReminderJson?.let { TaskReminderPresentation.Relative(TaskRelativeReminder.decode(it)) })
