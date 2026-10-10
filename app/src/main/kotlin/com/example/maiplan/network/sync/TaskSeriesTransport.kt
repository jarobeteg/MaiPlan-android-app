package com.example.maiplan.network.sync

import com.example.maiplan.database.MaiPlanDatabase
import com.example.maiplan.database.entities.*
import com.example.maiplan.repository.task.*
import com.example.maiplan.utils.common.OutboxStatus
import com.google.gson.JsonParser
import java.time.Instant
import java.util.UUID

internal class TaskSeriesTransport(private val db: MaiPlanDatabase) {
    suspend fun prepare(row: OutboxEntity): OutboxEntity {
        if (row.attemptCount > 0) return row
        val payload = JsonParser.parseString(checkNotNull(row.payloadJson)).asJsonObject
        val series = checkNotNull(db.taskSeriesDAO().get(row.userLocalId, row.entitySyncId))
        val version = if (payload["action"].asString == "CREATE_SERIES") null else series.serverVersion
        check(db.outboxDAO().prepareTaskIntent(row.mutationId, row.userLocalId, version, payload.toString()) == 1)
        return row.copy(baseVersion = version)
    }
    suspend fun ready(row: OutboxEntity): Boolean {
        if (row.attemptCount > 0) return true
        val data = JsonParser.parseString(checkNotNull(row.payloadJson)).asJsonObject
        if (data["action"].asString != "CREATE_SERIES" && db.taskSeriesDAO().get(row.userLocalId, row.entitySyncId)?.serverVersion == null) return false
        val definition = data["definition"]?.takeUnless { it.isJsonNull }?.let { TaskSeriesDefinition.decode(it.toString()) }
        val blockers = db.outboxDAO().allForUser(row.userLocalId)
        for (id in definition?.revisions?.mapNotNull { it.category_sync_id }?.distinct().orEmpty()) {
            val category = db.categoryDAO().getCategoryBySyncId(UUID.fromString(id), row.userLocalId)
            val pending = blockers.filter { it.entityType == TideEntityType.CATEGORY && it.entitySyncId.toString() == id }
            if (pending.any { it.status in listOf(OutboxStatus.CONFLICT, OutboxStatus.REJECTED) }) {
                db.outboxDAO().setMutationOutcome(row.mutationId, OutboxStatus.PENDING, OutboxStatus.REJECTED, "TASK_DEPENDENCY_REJECTED", null, null)
                return false
            }
            if (category?.serverVersion == null || pending.isNotEmpty()) return false
        }
        return true
    }

    suspend fun apply(user: Long, change: TideChange): Boolean {
        if (change.entityType == TideEntityType.TASK_EXCLUSION) {
            val data = checkNotNull(change.data)
            val series = UUID.fromString(data["series_id"].asString)
            val date = checkNotNull(taskDateFromEpochDay(data["slot_date"].asBigDecimal.longValueExact()))
            check(change.entitySyncId == taskExclusionUuid(series, date))
            db.taskSeriesDAO().exclude(TaskExclusionEntity(change.entitySyncId, user, series, date, change.serverVersion))
            val task = db.taskDAO().getTaskBySyncId(taskOccurrenceUuid(series, date), user)
            task?.let {
                db.taskDAO().updateTask(it.copy(deletedAt = it.deletedAt ?: Instant.EPOCH))
                db.scheduledReminderDAO().removeTaskPending("task:${it.taskLocalId}", user)
                for (row in db.outboxDAO().allForUser(user).filter { row -> row.entityType == TideEntityType.TASK_ACTION &&
                    row.entitySyncId == it.syncId && row.status == OutboxStatus.PENDING && row.attemptCount == 0 })
                    db.outboxDAO().setMutationOutcome(row.mutationId, OutboxStatus.PENDING, OutboxStatus.REJECTED, "TASK_OCCURRENCE_EXCLUDED", null, null)
            }
            return true
        }
        val old = db.taskSeriesDAO().get(user, change.entitySyncId)
        if ((old?.serverVersion ?: 0) > change.serverVersion) return false
        if (change.operation == TideOperation.DELETE) {
            if (old != null) db.taskSeriesDAO().update(old.copy(serverVersion = change.serverVersion, deletedAt = old.deletedAt ?: Instant.EPOCH))
            return true
        }
        val data = checkNotNull(change.data)
        val definition = TaskSeriesDefinition.decode(data["definition"].toString())
        val row = TaskSeriesEntity(seriesLocalId = old?.seriesLocalId ?: 0, userLocalId = user, syncId = change.entitySyncId,
            serverVersion = change.serverVersion, definitionJson = definition.json(), historyThrough = old?.historyThrough,
            pendingOperationJson = data["pending_operation"]?.takeUnless { it.isJsonNull }?.toString(),
            createdAt = data["created_at"]?.let { Instant.parse(it.asString) } ?: old?.createdAt ?: Instant.EPOCH,
            updatedAt = data["updated_at"]?.let { Instant.parse(it.asString) } ?: old?.updatedAt ?: Instant.EPOCH)
        if (old == null) db.taskSeriesDAO().insert(row) else db.taskSeriesDAO().update(row)
        return true
    }
}
