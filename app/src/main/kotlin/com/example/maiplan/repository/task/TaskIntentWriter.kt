package com.example.maiplan.repository.task

import androidx.room.withTransaction
import com.example.maiplan.database.MaiPlanDatabase
import com.example.maiplan.database.entities.OutboxEntity
import com.example.maiplan.database.entities.SubtaskEntity
import com.example.maiplan.database.entities.TaskEntity
import com.google.gson.GsonBuilder
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.time.Instant
import java.util.UUID

object TaskLocalIntent {
    const val ENTITY_TYPE = "task_action"
    const val FORMAT_VERSION = 1
}

internal class TaskIntentWriter(private val database: MaiPlanDatabase) {
    private val gson = GsonBuilder().serializeNulls().create()

    suspend fun enqueue(task: TaskEntity, steps: List<SubtaskEntity>, action: String, operation: String, now: Instant,
        arguments: Map<String, Any?> = emptyMap()) {
        val categoryId = task.categoryLocalId?.let {
            checkNotNull(database.categoryDAO().getCategoryByLocalId(it, task.userLocalId)) { "Task Category is not owned by this user" }
                .takeIf { row -> row.deletedAt == null }?.syncId?.toString()
        }
        val reminderId = task.reminderLocalId?.let {
            checkNotNull(database.reminderDAO().getReminderByLocalId(it, task.userLocalId)) { "Task Reminder is not owned by this user" }
                .takeIf { row -> row.deletedAt == null }?.syncId?.toString()
        }
        val data = linkedMapOf<String, Any?>(
            "title" to task.title, "description" to task.description, "status" to task.status,
            "scheduled_date" to taskDateToEpochDay(task.scheduledDate), "estimated_time" to task.estimatedMilliseconds,
            "completed_date" to taskDateToEpochDay(task.completedDate), "category_sync_id" to categoryId,
            "reminder_sync_id" to reminderId, "series_id" to task.seriesId, "occurrence_number" to task.occurrenceNumber,
            "slot_date" to taskDateToEpochDay(task.slotDate), "generation_revision" to task.generationRevision,
            "occurrence_override" to task.occurrenceOverride,
            "relative_reminder" to task.relativeReminderJson?.let { JsonParser.parseString(it) },
            "repeat_unit" to task.repeatUnit, "repeat_interval" to task.repeatInterval, "repeat_weekdays" to task.repeatWeekdays,
            "repeat_end_date" to taskDateToEpochDay(task.repeatEndDate), "repeat_anchor_date" to taskDateToEpochDay(task.repeatAnchorDate),
        )
        decodeTaskMutationDefinition(gson.toJsonTree(data).asJsonObject)
        val changedSteps = steps.map { step ->
            check(step.userLocalId == task.userLocalId && step.taskLocalId == task.taskLocalId)
            val payload = linkedMapOf<String, Any?>("parent_task_sync_id" to task.syncId.toString(), "title" to step.title,
                "status" to step.status, "sort_order" to step.sortOrder, "estimated_time" to step.estimatedMilliseconds,
                "completed_date" to taskDateToEpochDay(step.completedDate))
            decodeSubtaskMutationDefinition(gson.toJsonTree(payload).asJsonObject)
            mapOf("sync_id" to step.syncId.toString(), "base_version" to step.serverVersion,
                "deleted" to (step.deletedAt != null), "data" to payload)
        }
        val intent = mapOf("format_version" to TaskLocalIntent.FORMAT_VERSION, "action" to action, "arguments" to arguments,
            "task" to mapOf("sync_id" to task.syncId.toString(), "base_version" to task.serverVersion,
                "deleted" to (task.deletedAt != null), "data" to data), "subtask_changes" to changedSteps)
        database.outboxDAO().insertMutation(OutboxEntity(mutationId = UUID.randomUUID(), userLocalId = task.userLocalId,
            entityType = TaskLocalIntent.ENTITY_TYPE, entitySyncId = task.syncId, operation = operation,
            baseVersion = task.serverVersion, payloadJson = gson.toJson(intent), createdAt = now))
    }

    suspend fun supersedeNeverAttempted(task: TaskEntity) {
        database.outboxDAO().deleteNeverAttemptedIntents(task.userLocalId, TaskLocalIntent.ENTITY_TYPE, task.syncId)
    }

    suspend fun normalizeDeletedCategory(userLocalId: Long, categorySyncId: UUID) {
        for (row in database.outboxDAO().getNeverAttemptedIntents(userLocalId, TaskLocalIntent.ENTITY_TYPE)) {
            val intent = JsonParser.parseString(checkNotNull(row.payloadJson)).asJsonObject
            val data = intent.getAsJsonObject("task").getAsJsonObject("data")
            val value = data["category_sync_id"]
            if (value != null && !value.isJsonNull && value.asString.equals(categorySyncId.toString(), ignoreCase = true)) {
                data.add("category_sync_id", JsonNull.INSTANCE)
                check(database.outboxDAO().rewriteNeverAttemptedPayload(row.mutationId, userLocalId, gson.toJson(intent)) == 1)
            }
        }
    }
}

class TaskCategoryLinks(private val database: MaiPlanDatabase) {
    suspend fun categoryDeleted(userLocalId: Long, categorySyncId: UUID) {
        database.withTransaction {
            TaskIntentWriter(database).normalizeDeletedCategory(userLocalId, categorySyncId)
            normalizeSeriesCategory(userLocalId, categorySyncId)
        }
    }

    private suspend fun normalizeSeriesCategory(user: Long, category: UUID) {
        fun detach(data: JsonObject): Boolean {
            var changed = false
            data.getAsJsonArray("revisions").forEach {
                val revision = it.asJsonObject
                if (revision["category_sync_id"]?.takeUnless { value -> value.isJsonNull }?.asString == category.toString()) {
                    revision.add("category_sync_id", JsonNull.INSTANCE); changed = true
                }
            }
            return changed
        }
        for (series in database.taskSeriesDAO().all(user)) {
            val definition = JsonParser.parseString(series.definitionJson).asJsonObject
            if (detach(definition)) database.taskSeriesDAO().update(series.copy(definitionJson = definition.toString()))
        }
        for (row in database.outboxDAO().getNeverAttemptedIntents(user, com.example.maiplan.network.sync.TideEntityType.TASK_SERIES_ACTION)) {
            val intent = JsonParser.parseString(checkNotNull(row.payloadJson)).asJsonObject
            val definition = intent["definition"]?.takeUnless { it.isJsonNull }?.asJsonObject ?: continue
            if (detach(definition)) database.outboxDAO().rewriteNeverAttemptedPayload(row.mutationId, user, intent.toString())
        }
    }

    suspend fun normalizePending(userLocalId: Long) {
        database.withTransaction {
            for (series in database.taskSeriesDAO().all(userLocalId)) {
                val definition = TaskSeriesDefinition.decode(series.definitionJson)
                for (id in definition.revisions.mapNotNull { it.category_sync_id }.distinct()) {
                    val category = database.categoryDAO().getCategoryBySyncId(UUID.fromString(id), userLocalId)
                    if (category?.deletedAt != null) normalizeSeriesCategory(userLocalId, category.syncId)
                }
            }
            for (row in database.outboxDAO().getNeverAttemptedIntents(userLocalId, TaskLocalIntent.ENTITY_TYPE)) {
                val data: JsonObject = JsonParser.parseString(checkNotNull(row.payloadJson)).asJsonObject.getAsJsonObject("task").getAsJsonObject("data")
                val value = data["category_sync_id"]
                if (value != null && !value.isJsonNull) {
                    val syncId = UUID.fromString(value.asString)
                    val category = database.categoryDAO().getCategoryBySyncId(syncId, userLocalId)
                    if (category == null || category.deletedAt != null) TaskIntentWriter(database).normalizeDeletedCategory(userLocalId, syncId)
                }
            }
        }
    }
}
