package com.example.maiplan.repository.task

import com.google.gson.JsonObject
import java.util.UUID

data class TaskMutationDefinition(
    val task: TaskDefinition,
    val categorySyncId: UUID?,
    val reminderSyncId: UUID?,
)

private val integerToken = Regex("-?(0|[1-9][0-9]*)")

private fun JsonObject.text(name: String, required: Boolean = false): String? {
    val value = get(name)
    if (value == null || value.isJsonNull) {
        require(!required) { "$name is required" }
        return null
    }
    require(value.isJsonPrimitive && value.asJsonPrimitive.isString) { "$name must be text" }
    return value.asString
}

private fun JsonObject.integer(name: String, required: Boolean = false): Long? {
    val value = get(name)
    if (value == null || value.isJsonNull) {
        require(!required) { "$name is required" }
        return null
    }
    require(value.isJsonPrimitive && value.asJsonPrimitive.isNumber && integerToken.matches(value.asString)) {
        "$name must be an integer"
    }
    return value.asString.toLong()
}

private fun JsonObject.int(name: String, required: Boolean = false): Int? = integer(name, required)?.let {
    require(it in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) { "$name is outside the integer range" }
    it.toInt()
}

private fun JsonObject.uuid(name: String, required: Boolean = false): UUID? = text(name, required)?.let {
    UUID.fromString(it).also { parsed -> require(parsed.toString().equals(it, ignoreCase = true)) { "$name must be a UUID" } }
}

private fun JsonObject.onlyFields(allowed: Set<String>) {
    require(keySet().all { it in allowed }) { "Unsupported Task payload field" }
}

fun decodeTaskMutationDefinition(data: JsonObject): TaskMutationDefinition {
    data.onlyFields(setOf("category_sync_id", "reminder_sync_id", "title", "description", "status",
        "scheduled_date", "estimated_time", "completed_date", "series_id", "occurrence_number",
        "repeat_unit", "repeat_interval", "repeat_weekdays", "repeat_end_date", "repeat_anchor_date",
        "slot_date", "generation_revision", "occurrence_override", "relative_reminder"))
    val definition = TaskDefinition(
        title = requireNotNull(data.text("title", required = true)),
        description = data.text("description"),
        status = TaskStatus.fromCode(requireNotNull(data.int("status", required = true))),
        scheduledDate = taskDateFromEpochDay(data.integer("scheduled_date")),
        estimatedMilliseconds = data.integer("estimated_time"),
        completedDate = taskDateFromEpochDay(data.integer("completed_date")),
        seriesId = data.text("series_id"),
        occurrenceNumber = data.int("occurrence_number"),
        slotDate = taskDateFromEpochDay(data.integer("slot_date")),
        generationRevision = data.int("generation_revision"),
        occurrenceOverride = data["occurrence_override"]?.also { require(it.isJsonPrimitive && it.asJsonPrimitive.isBoolean) }?.asBoolean ?: false,
        relativeReminder = data["relative_reminder"]?.takeUnless { it.isJsonNull }?.let { TaskRelativeReminder.decode(it.toString()) },
        repeatUnit = data.int("repeat_unit"),
        repeatInterval = data.int("repeat_interval"),
        repeatWeekdays = data.int("repeat_weekdays"),
        repeatEndDate = taskDateFromEpochDay(data.integer("repeat_end_date")),
        repeatAnchorDate = taskDateFromEpochDay(data.integer("repeat_anchor_date")),
    )
    require(definition.relativeReminder == null || data.uuid("reminder_sync_id") == null)
    return TaskMutationDefinition(normalizedTaskDefinition(definition), data.uuid("category_sync_id"), data.uuid("reminder_sync_id"))
}

fun decodeSubtaskMutationDefinition(data: JsonObject): SubtaskDefinition {
    data.onlyFields(setOf("parent_task_sync_id", "title", "status", "sort_order", "estimated_time", "completed_date"))
    return normalizedSubtaskDefinition(SubtaskDefinition(
        parentTaskSyncId = requireNotNull(data.uuid("parent_task_sync_id", required = true)),
        title = requireNotNull(data.text("title", required = true)),
        status = TaskStatus.fromCode(requireNotNull(data.int("status", required = true))),
        sortOrder = requireNotNull(data.int("sort_order", required = true)),
        estimatedMilliseconds = data.integer("estimated_time"),
        completedDate = taskDateFromEpochDay(data.integer("completed_date")),
    ))
}
