package com.example.maiplan.network.sync

import com.example.maiplan.database.MaiPlanDatabase
import com.example.maiplan.database.entities.OutboxEntity
import com.example.maiplan.repository.task.*
import com.example.maiplan.utils.common.OutboxStatus
import com.google.gson.JsonNull
import com.google.gson.JsonParser

internal class TaskTransport(private val db: MaiPlanDatabase) {
    suspend fun prepare(row: OutboxEntity): OutboxEntity {
        if (row.attemptCount > 0) return row
        val payload = JsonParser.parseString(checkNotNull(row.payloadJson)).asJsonObject
        check(payload["format_version"].asInt == TaskLocalIntent.FORMAT_VERSION)
        val parent = payload.getAsJsonObject("task")
        check(parent["sync_id"].asString == row.entitySyncId.toString())
        decodeTaskMutationDefinition(parent.getAsJsonObject("data"))
        val data = parent.getAsJsonObject("data")
        val reminderId = data["reminder_sync_id"]?.takeUnless { it.isJsonNull }?.asString?.let(java.util.UUID::fromString)
        if (reminderId != null && db.reminderDAO().getReminderBySyncId(reminderId, row.userLocalId)?.deletedAt != null) {
            data.add("reminder_sync_id", JsonNull.INSTANCE)
        }
        val version = db.taskDAO().getTaskBySyncId(row.entitySyncId, row.userLocalId)?.serverVersion
        parent.add("base_version", version?.let { com.google.gson.JsonPrimitive(it) } ?: JsonNull.INSTANCE)
        for (element in payload.getAsJsonArray("subtask_changes")) {
            val step = element.asJsonObject
            decodeSubtaskMutationDefinition(step.getAsJsonObject("data"))
            val id = java.util.UUID.fromString(step["sync_id"].asString)
            val stepVersion = db.subtaskDAO().getSubtaskBySyncId(id, row.userLocalId)?.serverVersion
            step.add("base_version", stepVersion?.let { com.google.gson.JsonPrimitive(it) } ?: JsonNull.INSTANCE)
        }
        check(db.outboxDAO().prepareTaskIntent(row.mutationId, row.userLocalId, version, payload.toString()) == 1)
        return row.copy(baseVersion = version, payloadJson = payload.toString())
    }

    suspend fun dependenciesReady(row: OutboxEntity): Boolean {
        if (row.attemptCount > 0) return true
        val data = JsonParser.parseString(checkNotNull(row.payloadJson)).asJsonObject.getAsJsonObject("task").getAsJsonObject("data")
        val task = db.taskDAO().getTaskBySyncId(row.entitySyncId, row.userLocalId)
        if (task?.seriesId != null) {
            val seriesId = java.util.UUID.fromString(task.seriesId)
            val series = db.taskSeriesDAO().get(row.userLocalId, seriesId)
            val seriesActions = db.outboxDAO().allForUser(row.userLocalId).filter { it.entityType == TideEntityType.TASK_SERIES_ACTION && it.entitySyncId == seriesId }
            if (series?.deletedAt != null || seriesActions.any { it.status in listOf(OutboxStatus.REJECTED, OutboxStatus.CONFLICT) }) {
                db.outboxDAO().setMutationOutcome(row.mutationId, OutboxStatus.PENDING, OutboxStatus.REJECTED, "TASK_SERIES_CHANGED", null, null)
                return false
            }
            if (task.serverVersion == null || series?.serverVersion == null || seriesActions.isNotEmpty() || series.pendingOperationJson != null) return false
        }
        if (row.operation == TideOperation.DELETE) return true
        val all = db.outboxDAO().allForUser(row.userLocalId)
        for ((field, type) in listOf("category_sync_id" to TideEntityType.CATEGORY, "reminder_sync_id" to TideEntityType.REMINDER)) {
            val value = data[field]?.takeUnless { it.isJsonNull } ?: continue
            val id = java.util.UUID.fromString(value.asString)
            val version = if (type == TideEntityType.CATEGORY) db.categoryDAO().getCategoryBySyncId(id, row.userLocalId)?.serverVersion
                          else db.reminderDAO().getReminderBySyncId(id, row.userLocalId)?.serverVersion
            val blockers = all.filter { it.entityType == type && it.entitySyncId == id }
            if (blockers.any { it.status in listOf(OutboxStatus.REJECTED, OutboxStatus.CONFLICT) } || (version == null && blockers.isEmpty())) {
                check(db.outboxDAO().setMutationOutcome(row.mutationId, OutboxStatus.PENDING, OutboxStatus.REJECTED,
                    "TASK_DEPENDENCY_REJECTED", null, null) == 1)
                return false
            }
            if (version == null || blockers.isNotEmpty()) return false
        }
        return true
    }
}
