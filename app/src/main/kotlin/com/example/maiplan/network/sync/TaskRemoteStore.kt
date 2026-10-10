package com.example.maiplan.network.sync

import com.example.maiplan.database.MaiPlanDatabase
import com.example.maiplan.database.entities.TaskEntity
import com.example.maiplan.database.entities.SubtaskEntity
import com.example.maiplan.repository.task.*
import com.google.gson.JsonObject
import java.time.Instant

internal class TideDependencyPending : Exception()

internal class TaskRemoteStore(private val db: MaiPlanDatabase) {
    suspend fun apply(user: Long, change: TideChange): Boolean {
        if (change.entityType == TideEntityType.TASK) {
            val old = db.taskDAO().getTaskBySyncId(change.entitySyncId, user)
            if ((old?.serverVersion ?: 0L) > change.serverVersion) return false
            if (change.operation == TideOperation.DELETE) {
                if (old != null) {
                    for (step in db.subtaskDAO().allForTask(old.taskLocalId, user).filter { it.deletedAt == null }) {
                        db.subtaskDAO().updateSubtask(step.copy(deletedAt = step.deletedAt ?: Instant.EPOCH))
                    }
                    db.taskDAO().updateTask(old.copy(serverVersion = change.serverVersion, deletedAt = old.deletedAt ?: Instant.EPOCH))
                    invalidate(user, old.taskLocalId)
                }
                return true
            }
            val data = checkNotNull(change.data)
            val decoded = decodeTaskMutationDefinition(data.business())
            val value = decoded.task
            value.seriesId?.let {
                val series = db.taskSeriesDAO().get(user, java.util.UUID.fromString(it)) ?: throw TideDependencyPending()
                if (series.deletedAt != null) return true
            }
            val category = decoded.categorySyncId?.let {
                val link = db.categoryDAO().getCategoryBySyncId(it, user) ?: run {
                    if (db.syncInboxDAO().get(user, TideEntityType.CATEGORY, it)?.operation == TideOperation.DELETE) null
                    else throw TideDependencyPending()
                }
                link?.takeIf { row -> row.deletedAt == null }?.categoryLocalId
            }
            val reminder = decoded.reminderSyncId?.let {
                val link = db.reminderDAO().getReminderBySyncId(it, user) ?: run {
                    if (db.syncInboxDAO().get(user, TideEntityType.REMINDER, it)?.operation == TideOperation.DELETE) null
                    else throw TideDependencyPending()
                }
                link?.takeIf { row -> row.deletedAt == null }?.reminderLocalId
            }
            val row = TaskEntity(taskLocalId = old?.taskLocalId ?: 0, userLocalId = user, title = value.title,
                description = value.description, status = value.status.code, scheduledDate = value.scheduledDate,
                estimatedMilliseconds = value.estimatedMilliseconds, completedDate = value.completedDate,
                categoryLocalId = category, reminderLocalId = reminder, seriesId = value.seriesId,
                occurrenceNumber = value.occurrenceNumber, repeatUnit = value.repeatUnit,
                slotDate = value.slotDate, generationRevision = value.generationRevision,
                occurrenceOverride = value.occurrenceOverride, relativeReminderJson = value.relativeReminder?.json(),
                repeatInterval = value.repeatInterval, repeatWeekdays = value.repeatWeekdays,
                repeatEndDate = value.repeatEndDate, repeatAnchorDate = value.repeatAnchorDate,
                syncId = change.entitySyncId, serverVersion = change.serverVersion,
                createdAt = data.instant("created_at", old?.createdAt), updatedAt = data.instant("updated_at", old?.updatedAt))
            val id = if (old == null) db.taskDAO().insertTask(row) else {
                check(db.taskDAO().updateTask(row) == 1); row.taskLocalId
            }
            invalidate(user, id)
            return true
        }
        val old = db.subtaskDAO().getSubtaskBySyncId(change.entitySyncId, user)
        if ((old?.serverVersion ?: 0L) > change.serverVersion) return false
        if (change.operation == TideOperation.DELETE) {
            if (old != null) {
                db.subtaskDAO().updateSubtask(old.copy(serverVersion = change.serverVersion, deletedAt = old.deletedAt ?: Instant.EPOCH))
                invalidate(user, old.taskLocalId)
            }
            return true
        }
        val data = checkNotNull(change.data)
        val value = decodeSubtaskMutationDefinition(data.business())
        val parent = db.taskDAO().getTaskBySyncId(value.parentTaskSyncId, user) ?: run {
            if (db.syncInboxDAO().get(user, TideEntityType.TASK, value.parentTaskSyncId)?.operation == TideOperation.DELETE) return true
            throw TideDependencyPending()
        }
        if (parent.deletedAt != null) return true
        check(old == null || old.taskLocalId == parent.taskLocalId) { "Remote step changed parent" }
        val row = SubtaskEntity(subtaskLocalId = old?.subtaskLocalId ?: 0, userLocalId = user,
            taskLocalId = parent.taskLocalId, title = value.title, status = value.status.code,
            sortOrder = value.sortOrder, estimatedMilliseconds = value.estimatedMilliseconds,
            completedDate = value.completedDate, syncId = change.entitySyncId, serverVersion = change.serverVersion,
            createdAt = data.instant("created_at", old?.createdAt), updatedAt = data.instant("updated_at", old?.updatedAt))
        if (old == null) db.subtaskDAO().insertSubtask(row) else check(db.subtaskDAO().updateSubtask(row) == 1)
        invalidate(user, parent.taskLocalId)
        return true
    }

    private suspend fun invalidate(user: Long, task: Long) {
        val row = db.taskDAO().getTaskByLocalId(task, user) ?: return
        com.example.maiplan.utils.notifications.ReminderPlanWriter(db).task(row,
            row.reminderLocalId?.let { db.reminderDAO().getReminderByLocalId(it, user) })
    }

    private fun JsonObject.business() = deepCopy().also { it.remove("created_at"); it.remove("updated_at") }
    private fun JsonObject.instant(name: String, old: Instant?) = get(name)?.let { Instant.parse(it.asString) } ?: old ?: Instant.EPOCH
}
