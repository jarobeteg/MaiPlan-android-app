package com.example.maiplan.repository.task

import java.time.LocalDate
import java.util.UUID

object TaskContract {
    const val MAX_TITLE_LENGTH = 255
    const val MAX_DESCRIPTION_LENGTH = 10_000
    const val MAX_ESTIMATED_MILLISECONDS = 365L * 24 * 60 * 60 * 1000
    val MIN_DATE: LocalDate = LocalDate.of(1, 1, 1)
    val MAX_DATE: LocalDate = LocalDate.of(9999, 12, 31)
}

enum class TaskStatus(val code: Int) {
    TODO(0), DONE(1), IN_PROGRESS(2), SKIPPED(3), CANCELLED(4);

    val isTerminal: Boolean get() = this == DONE || this == SKIPPED || this == CANCELLED

    companion object {
        fun fromCode(code: Int): TaskStatus = entries.firstOrNull { it.code == code }
            ?: throw IllegalArgumentException("Unknown Task status: $code")
    }
}

private val taskWhitespace = (9..13).toSet() + (28..32).toSet() +
    setOf(0x85, 0xA0, 0x1680, 0x2028, 0x2029, 0x202F, 0x205F, 0x3000) + (0x2000..0x200A)

private fun normalizeTaskText(value: String, name: String, limit: Int): String {
    require(value.codePoints().noneMatch { it == 0 || it in 0xD800..0xDFFF }) {
        "$name contains an invalid Unicode character"
    }
    val normalized = value.trim { it.code in taskWhitespace }
    require(normalized.codePointCount(0, normalized.length) <= limit) { "$name exceeds $limit characters" }
    return normalized
}

fun normalizeTaskTitle(value: String): String =
    normalizeTaskText(value, "title", TaskContract.MAX_TITLE_LENGTH).also {
        require(it.isNotEmpty()) { "Title must not be blank" }
    }

fun normalizeTaskDescription(value: String?): String? = value?.let {
    normalizeTaskText(it, "description", TaskContract.MAX_DESCRIPTION_LENGTH).takeIf(String::isNotEmpty)
}

fun validateTaskDate(date: LocalDate?) {
    require(date == null || date in TaskContract.MIN_DATE..TaskContract.MAX_DATE) {
        "Task date is outside years 1 through 9999"
    }
}

fun validateTaskEstimate(milliseconds: Long?) {
    require(milliseconds == null || milliseconds in 0..TaskContract.MAX_ESTIMATED_MILLISECONDS) {
        "Estimated duration must be between zero and 365 fixed days"
    }
}

fun validateTaskCompletion(status: TaskStatus, completedDate: LocalDate?) {
    validateTaskDate(completedDate)
    require((status == TaskStatus.DONE) == (completedDate != null)) {
        "Only Done has a completed date, and Done requires one"
    }
}

data class TaskDefinition(
    val title: String,
    val description: String? = null,
    val status: TaskStatus = TaskStatus.TODO,
    val scheduledDate: LocalDate? = null,
    val estimatedMilliseconds: Long? = null,
    val completedDate: LocalDate? = null,
    val seriesId: String? = null,
    val occurrenceNumber: Int? = null,
    val slotDate: LocalDate? = null,
    val generationRevision: Int? = null,
    val occurrenceOverride: Boolean = false,
    val relativeReminder: TaskRelativeReminder? = null,
    val repeatUnit: Int? = null,
    val repeatInterval: Int? = null,
    val repeatWeekdays: Int? = null,
    val repeatEndDate: LocalDate? = null,
    val repeatAnchorDate: LocalDate? = null,
)

data class SubtaskDefinition(
    val parentTaskSyncId: UUID,
    val title: String,
    val status: TaskStatus = TaskStatus.TODO,
    val sortOrder: Int,
    val estimatedMilliseconds: Long? = null,
    val completedDate: LocalDate? = null,
)

fun normalizedTaskDefinition(task: TaskDefinition): TaskDefinition = task.copy(
    title = normalizeTaskTitle(task.title),
    description = normalizeTaskDescription(task.description),
).also(::validateTaskDefinition)

fun validateTaskDefinition(task: TaskDefinition) {
    validateTaskDate(task.slotDate)
    if (task.seriesId == null) require(task.slotDate == null && task.generationRevision == null && !task.occurrenceOverride) {
        "A one-off Task cannot contain occurrence identity metadata"
    } else require(task.slotDate != null && task.generationRevision != null) {
        "A repeating Task requires its original slot and generation revision"
    }
    require(task.generationRevision == null || task.generationRevision > 0)
    task.relativeReminder?.validate()
    require(task.relativeReminder == null || task.scheduledDate != null) { "A relative reminder requires a planned date" }
    normalizeTaskTitle(task.title)
    normalizeTaskDescription(task.description)
    validateTaskCompletion(task.status, task.completedDate)
    validateTaskDate(task.scheduledDate)
    validateTaskDate(task.repeatAnchorDate)
    validateTaskDate(task.repeatEndDate)
    validateTaskEstimate(task.estimatedMilliseconds)
    taskRepeatRule(task)
}

fun normalizedSubtaskDefinition(step: SubtaskDefinition): SubtaskDefinition = step.copy(
    title = normalizeTaskTitle(step.title),
).also {
    validateTaskCompletion(it.status, it.completedDate)
    validateTaskEstimate(it.estimatedMilliseconds)
    require(it.sortOrder >= 0) { "Subtask order must be nonnegative" }
}

fun taskRepeatRule(task: TaskDefinition): TaskRepeatRule? {
    if (task.repeatUnit == null) {
        require(listOf(task.seriesId, task.occurrenceNumber, task.repeatInterval,
            task.repeatWeekdays, task.repeatEndDate, task.repeatAnchorDate).all { it == null }) {
            "A one-off Task must not contain repeat metadata"
        }
        return null
    }
    val seriesId = requireNotNull(task.seriesId) { "A repeating Task requires a series identity" }
    require(UUID.fromString(seriesId).toString() == seriesId) { "Series identity must be a canonical lowercase UUID" }
    requireNotNull(task.occurrenceNumber).also { require(it >= 0) }
    requireNotNull(task.scheduledDate) { "A repeating occurrence requires a planned date" }
    validateTaskDate(task.scheduledDate)
    return TaskRepeatRule(
        unit = TaskRepeatUnit.fromCode(task.repeatUnit),
        interval = requireNotNull(task.repeatInterval),
        anchorDate = requireNotNull(task.repeatAnchorDate),
        weekdays = task.repeatWeekdays,
        endDate = task.repeatEndDate,
    )
}
