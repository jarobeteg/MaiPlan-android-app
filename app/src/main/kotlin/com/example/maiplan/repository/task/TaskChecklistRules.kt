package com.example.maiplan.repository.task

import java.time.LocalDate
import java.util.UUID

data class TaskCompletion(val status: TaskStatus = TaskStatus.TODO, val completedDate: LocalDate? = null) {
    init { validateTaskCompletion(status, completedDate) }
}

data class SubtaskCompletion(val syncId: UUID, val completion: TaskCompletion = TaskCompletion())

data class TaskChecklist(val completion: TaskCompletion, val subtasks: List<SubtaskCompletion> = emptyList()) {
    init { require(subtasks.map { it.syncId }.distinct().size == subtasks.size) { "Duplicate Subtask identity" } }
}

private fun TaskCompletion.withStatus(status: TaskStatus, today: LocalDate): TaskCompletion {
    validateTaskDate(today)
    return if (status == this.status) this else TaskCompletion(status, today.takeIf { status == TaskStatus.DONE })
}

fun applyTaskStatus(state: TaskChecklist, status: TaskStatus, today: LocalDate): TaskChecklist {
    require(status != TaskStatus.IN_PROGRESS || !state.completion.status.isTerminal) {
        "Reopen the Task before starting it"
    }
    val parent = state.completion.withStatus(status, today)
    val steps = if (status == TaskStatus.IN_PROGRESS) state.subtasks else state.subtasks.map {
        it.copy(completion = it.completion.withStatus(status, today))
    }
    return TaskChecklist(parent, steps)
}

fun applySubtaskStatus(state: TaskChecklist, syncId: UUID, status: TaskStatus, today: LocalDate): TaskChecklist {
    val step = requireNotNull(state.subtasks.firstOrNull { it.syncId == syncId }) {
        "Subtask does not belong to this checklist"
    }
    if (step.completion.status == status) return state
    require(!state.completion.status.isTerminal) { "Reopen the Task before changing its checklist" }
    val steps = state.subtasks.map {
        if (it.syncId == syncId) it.copy(completion = it.completion.withStatus(status, today)) else it
    }
    val parent = when {
        steps.isNotEmpty() && steps.all { it.completion.status.isTerminal } ->
            state.completion.withStatus(TaskStatus.DONE, today)
        state.completion.status == TaskStatus.IN_PROGRESS || steps.any { it.completion.status != TaskStatus.TODO } ->
            TaskCompletion(TaskStatus.IN_PROGRESS)
        else -> TaskCompletion()
    }
    return TaskChecklist(parent, steps)
}

data class TaskEstimate(val milliseconds: Long?, val isPartial: Boolean, val isManual: Boolean)

fun effectiveTaskEstimate(manual: Long?, steps: List<Long?>): TaskEstimate {
    validateTaskEstimate(manual)
    steps.forEach(::validateTaskEstimate)
    if (manual != null) return TaskEstimate(manual, isPartial = false, isManual = true)
    val known = steps.filterNotNull()
    val sum = known.takeIf { it.isNotEmpty() }?.fold(0L) { total, value -> Math.addExact(total, value) }
    return TaskEstimate(sum, isPartial = steps.isNotEmpty() && known.size != steps.size, isManual = false)
}
