package com.example.maiplan.repository.task

import com.example.maiplan.database.dao.TaskRecord
import com.example.maiplan.database.entities.SubtaskEntity
import com.example.maiplan.database.entities.TaskEntity
import java.time.LocalDate
import java.util.UUID

data class TaskContent(
    val title: String,
    val description: String? = null,
    val scheduledDate: LocalDate? = null,
    val estimatedMilliseconds: Long? = null,
    val categoryLocalId: Long? = null,
)

data class SubtaskDraft(val title: String, val estimatedMilliseconds: Long? = null)
data class ChecklistStepInput(val syncId: UUID, val title: String, val estimatedMilliseconds: Long? = null)
data class TaskReminderDraft(val triggerAtMillis: Long, val zoneId: String, val message: String? = null)

sealed interface TaskReminderChange {
    data object Keep : TaskReminderChange
    data object Remove : TaskReminderChange
    data class Set(val draft: TaskReminderDraft) : TaskReminderChange
    data class Relative(val rule: TaskRelativeReminder) : TaskReminderChange
}

data class CreateTaskInput(val content: TaskContent, val subtasks: List<SubtaskDraft> = emptyList(), val reminder: TaskReminderDraft? = null)
data class UpdateTaskInput(val taskLocalId: Long, val content: TaskContent, val reminder: TaskReminderChange = TaskReminderChange.Keep,
    val expectedRevision: String? = null, val checklist: List<ChecklistStepInput>? = null,
    val expectedChecklistRevision: String? = null, val expectedReminderRevision: String? = null)

sealed interface TaskCategoryFilter {
    data object All : TaskCategoryFilter
    data object Uncategorized : TaskCategoryFilter
    data class Selected(val localId: Long) : TaskCategoryFilter { init { require(localId > 0) } }
}

sealed interface TaskDateFilter {
    data object Any : TaskDateFilter
    data object Unscheduled : TaskDateFilter
    data class On(val date: LocalDate) : TaskDateFilter { init { validateTaskDate(date) } }
    data class Before(val date: LocalDate) : TaskDateFilter { init { validateTaskDate(date) } }
    data class Between(val from: LocalDate, val through: LocalDate) : TaskDateFilter {
        init { validateTaskDate(from); validateTaskDate(through); require(from <= through) }
    }
}

data class TaskQuery(
    val category: TaskCategoryFilter = TaskCategoryFilter.All,
    val date: TaskDateFilter = TaskDateFilter.Any,
    val statuses: Set<TaskStatus> = setOf(TaskStatus.TODO, TaskStatus.IN_PROGRESS),
    val search: String = "",
) {
    init { require(statuses.isNotEmpty()) { "Select at least one Task status" } }
}

data class TaskSnapshot(
    val task: TaskEntity,
    val effectiveCategoryLocalId: Long?,
    val subtasks: List<SubtaskEntity>,
    val finishedSteps: Int,
    val doneSteps: Int,
    val estimate: TaskEstimate,
    val reminder: com.example.maiplan.database.entities.ReminderEntity? = null,
)

internal fun TaskRecord.snapshot(): TaskSnapshot {
    val active = subtasks.filter { it.deletedAt == null && it.userLocalId == task.userLocalId }
        .sortedWith(compareBy<SubtaskEntity> { it.sortOrder }.thenBy { it.syncId.toString() })
    return TaskSnapshot(task, effectiveCategoryLocalId, active,
        active.count { TaskStatus.fromCode(it.status).isTerminal },
        active.count { it.status == TaskStatus.DONE.code },
        effectiveTaskEstimate(task.estimatedMilliseconds, active.map { it.estimatedMilliseconds }),
        reminder?.takeIf { it.userLocalId == task.userLocalId && it.deletedAt == null })
}

data class TaskWriteOutcome(
    val task: TaskEntity,
    val changed: Boolean,
    val invalidateReminder: Boolean = changed,
    val sideEffectWarning: String? = null,
)
