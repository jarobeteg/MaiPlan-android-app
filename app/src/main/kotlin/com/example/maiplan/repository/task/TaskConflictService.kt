package com.example.maiplan.repository.task

import android.content.Context
import androidx.room.withTransaction
import com.example.maiplan.database.MaiPlanDatabase
import com.example.maiplan.network.sync.*
import com.example.maiplan.repository.Result
import com.example.maiplan.utils.common.OutboxStatus
import com.google.gson.JsonParser
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonNull
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.combine
import java.time.Instant
import java.util.UUID

enum class TaskConflictChoice { SERVER, LOCAL, COPY }
data class TaskSyncState(val pending: Boolean, val conflicted: Boolean, val rejected: Boolean,
    val diagnostics: List<String>, val serverDeleted: Boolean = false)
data class TaskSyncIssue(val taskLocalId: Long, val syncId: UUID, val title: String, val state: TaskSyncState, val seriesIssue: Boolean = false)
data class TaskResolution(val taskLocalId: Long, val warning: String? = null)

class TaskConflictService(private val db: MaiPlanDatabase, private val requestSync: () -> Unit = {},
    private val invalidateAlarm: (Long) -> Unit = {}, private val recoverReminder: suspend (Long) -> Unit = {}) {
    private fun state(rows: List<com.example.maiplan.database.dao.TaskIntentState>, deleted: Boolean) =
        TaskSyncState(rows.any { it.status in listOf(OutboxStatus.PENDING, OutboxStatus.IN_SYNC) },
            rows.any { it.status == OutboxStatus.CONFLICT }, rows.any { it.status == OutboxStatus.REJECTED },
            rows.mapNotNull { it.lastError }.distinct(), deleted)
    private suspend fun seriesUnavailable(user: Long, task: com.example.maiplan.database.entities.TaskEntity?): Boolean {
        val id = task?.seriesId?.let(UUID::fromString) ?: return false
        return db.taskSeriesDAO().get(user, id)?.deletedAt != null || task.slotDate?.let { db.taskSeriesDAO().exclusion(user, id, it) != null } == true
    }
    private suspend fun remotelyUnavailable(user: Long, task: com.example.maiplan.database.entities.TaskEntity?): Boolean {
        val id = task?.seriesId?.let(UUID::fromString) ?: return false
        return db.syncInboxDAO().get(user, TideEntityType.TASK_SERIES, id)?.operation == TideOperation.DELETE ||
            task.slotDate?.let { db.taskSeriesDAO().exclusion(user, id, it)?.serverVersion != null } == true
    }

    fun observe(user: Long, task: UUID) = combine(db.outboxDAO().observeTaskChain(user, task), db.syncInboxDAO().observeTaskDeleted(user, task)) { rows, deleted ->
        state(rows, deleted || remotelyUnavailable(user, db.taskDAO().getTaskBySyncId(task, user)))
    }

    fun observeIssues(user: Long) = combine(db.outboxDAO().observeTaskIntents(user), db.syncInboxDAO().observeTaskTombstones(user)) { rows, inbox ->
        rows.groupBy { it.entityType to it.entitySyncId }.mapNotNull { (identity, chain) ->
            val (kind, id) = identity
            if (kind == TideEntityType.TASK_SERIES_ACTION) {
                val series = db.taskSeriesDAO().get(user, id) ?: return@mapNotNull null
                val task = db.taskDAO().seriesPage(user, id.toString(), 0, 1).firstOrNull()
                return@mapNotNull TaskSyncIssue(task?.taskLocalId ?: 0, id,
                    TaskSeriesDefinition.decode(series.definitionJson).revisions.last().title,
                    state(chain, inbox.any { it.entityType == TideEntityType.TASK_SERIES && it.entitySyncId == id }), seriesIssue = true)
            }
            db.taskDAO().getTaskBySyncId(id, user)?.let { task -> TaskSyncIssue(task.taskLocalId, id, task.title,
                state(chain, inbox.any { it.entityType == TideEntityType.TASK && it.entitySyncId == id } || remotelyUnavailable(user, task))) }
        }
    }

    suspend fun resolveSeries(user: Long, id: UUID, choice: TaskConflictChoice): TaskResolution = withContext(NonCancellable + Dispatchers.IO) {
        require(choice != TaskConflictChoice.LOCAL) { "Reload the accepted series or copy the retained work." }
        val result = db.withTransaction {
            val series = checkNotNull(db.taskSeriesDAO().get(user, id))
            val owner = checkNotNull(db.userDAO().getActiveUserByLocalId(user))
            val chain = db.outboxDAO().allForUser(user).filter { it.entityType == TideEntityType.TASK_SERIES_ACTION && it.entitySyncId == id }
            check(chain.any { it.status in listOf(OutboxStatus.CONFLICT, OutboxStatus.REJECTED) })
            check(chain.none { it.status == OutboxStatus.IN_SYNC || it.status == OutboxStatus.PENDING && it.attemptCount > 0 }) { "Finish replaying outstanding series attempts first." }
            var after = 0L
            val copies = mutableListOf<Long>()
            var scheduleCopy: Long? = null
            if (choice == TaskConflictChoice.COPY) {
                val template = TaskSeriesDefinition.decode(series.definitionJson).revisions.last()
                val copied = TaskLocalDataSource(db).createSeries(user, CreateTaskSeriesInput(template.copy(revision = 1, effective_from = template.repeat_anchor_date)))
                check(copied is Result.Success); scheduleCopy = copied.data.task.taskLocalId
            }
            do {
                val page = db.taskDAO().seriesPage(user, id.toString(), after)
                for (task in page) {
                    if (task.serverVersion == null) {
                        if (choice == TaskConflictChoice.COPY) {
                            val children = db.subtaskDAO().allForTask(task.taskLocalId, user).filter { it.deletedAt == null }
                            val copy = TaskLocalDataSource(db).createTask(user, CreateTaskInput(TaskContent(task.title, task.description,
                                task.scheduledDate, task.estimatedMilliseconds, task.categoryLocalId), children.map { SubtaskDraft(it.title, it.estimatedMilliseconds) }))
                            check(copy is Result.Success); copies += copy.data.task.taskLocalId
                        }
                        db.taskDAO().updateTask(task.copy(deletedAt = task.deletedAt ?: Instant.now()))
                        db.scheduledReminderDAO().removeTaskPending("task:${task.taskLocalId}", user)
                    }
                }
                after = page.lastOrNull()?.taskLocalId ?: after
            } while (page.size == TASK_SERIES_PAGE_SIZE)
            check(db.outboxDAO().deleteMutations(chain.map { it.mutationId }) == chain.size)
            val server = db.syncInboxDAO().get(user, TideEntityType.TASK_SERIES, id)
            if (server == null) db.taskSeriesDAO().update(series.copy(deletedAt = series.deletedAt ?: Instant.now()))
            else db.syncInboxDAO().put(server.copy(applied = false))
            for (row in db.syncInboxDAO().allForUser(user)) {
                if (row.entityType == TideEntityType.TASK && row.dataJson?.let { JsonParser.parseString(it).asJsonObject["series_id"]?.takeUnless { value -> value.isJsonNull }?.asString } == id.toString())
                    db.syncInboxDAO().put(row.copy(applied = false))
            }
            TideReconciler(db).replayWithinTransaction(user, owner.syncId)
            TaskResolution(scheduleCopy ?: copies.firstOrNull() ?: db.taskDAO().seriesPage(user, id.toString(), 0, 1).firstOrNull()?.taskLocalId ?: 0,
                if (scheduleCopy != null) "Copied the schedule and ${copies.size} unsynced occurrences for review. Check their reminders." else null)
        }
        runCatching(requestSync)
        runCatching { recoverReminder(result.taskLocalId) }
        result
    }

    suspend fun resolve(user: Long, taskId: UUID, choice: TaskConflictChoice): TaskResolution =
        withContext(NonCancellable + Dispatchers.IO) {
        var originalLocalId = 0L
        var omittedReminder = false
        val resolved = db.withTransaction {
            val owner = checkNotNull(db.userDAO().getActiveUserByLocalId(user))
            val task = checkNotNull(db.taskDAO().getTaskBySyncId(taskId, user)) { "Task is unavailable" }
            originalLocalId = task.taskLocalId
            val localSteps = db.subtaskDAO().allForTask(task.taskLocalId, user)
            val chain = db.outboxDAO().allForUser(user).filter { it.entityType == TaskLocalIntent.ENTITY_TYPE && it.entitySyncId == taskId }
            val server = db.syncInboxDAO().get(user, TideEntityType.TASK, taskId)
            check(chain.isNotEmpty() && (server?.operation == TideOperation.DELETE || chain.any { it.status in listOf(OutboxStatus.CONFLICT, OutboxStatus.REJECTED) })) { "Task has no conflict or rejection" }
            check(chain.none { it.status == OutboxStatus.IN_SYNC || (it.status == OutboxStatus.PENDING && it.attemptCount > 0) }) {
                "Finish replaying the outstanding sync attempt before resolving this Task"
            }
            if (choice == TaskConflictChoice.LOCAL) {
                check(!seriesUnavailable(user, task)) { "The occurrence or series was deleted. Copy the retained work as a new Task." }
                check(server?.operation != TideOperation.DELETE) { "The server deleted this Task. Use server state or copy your work as a new Task." }
                check(chain.none { it.operation == TideOperation.CREATE } || server == null) { "Copy this work to a new Task or use the existing server Task." }
            }
            check(db.outboxDAO().deleteMutations(chain.map { it.mutationId }) == chain.size)
            for (row in db.syncInboxDAO().allForUser(user)) {
                val belongs = (row.entityType == TideEntityType.TASK && row.entitySyncId == taskId) ||
                    (row.entityType == TideEntityType.SUBTASK && (localSteps.any { it.syncId == row.entitySyncId } ||
                        row.dataJson?.let { JsonParser.parseString(it).asJsonObject["parent_task_sync_id"]?.asString } == taskId.toString()))
                if (belongs) db.syncInboxDAO().put(row.copy(applied = false))
            }
            val reconciler = TideReconciler(db)
            reconciler.replayWithinTransaction(user, owner.syncId)
            val resolvedId = when (choice) {
                TaskConflictChoice.SERVER, TaskConflictChoice.COPY -> {
                    if (server == null && task.serverVersion == null) {
                        for (step in localSteps) db.subtaskDAO().updateSubtask(step.copy(deletedAt = step.deletedAt ?: Instant.now()))
                        db.taskDAO().updateTask(task.copy(deletedAt = task.deletedAt ?: Instant.now()))
                    }
                    if (choice == TaskConflictChoice.COPY) {
                        val category = task.categoryLocalId?.takeIf { db.categoryDAO().getCategoryByLocalId(it, user)?.deletedAt == null }
                        val originalReminder = task.reminderLocalId?.let { db.reminderDAO().getReminderByLocalId(it, user) }?.takeIf { it.deletedAt == null }
                        val relative = task.relativeReminderJson?.let(TaskRelativeReminder::decode)
                        val relativeTrigger = task.scheduledDate?.let { relative?.trigger(it) }?.takeIf { it > System.currentTimeMillis() }
                        val reminder = originalReminder?.takeIf { it.reminderTime > System.currentTimeMillis() && it.status == 1 && it.frequency == 0 }
                            ?.let { TaskReminderDraft(it.reminderTime, it.zoneId, it.message) }
                            ?: relativeTrigger?.let { TaskReminderDraft(it, checkNotNull(relative).zone_id, relative.message) }
                        omittedReminder = (originalReminder != null || relative != null) && reminder == null
                        val copiedSteps = localSteps.filter { it.deletedAt == null || (task.deletedAt != null && it.deletedAt == task.deletedAt) }
                        val result = TaskLocalDataSource(db).createTask(user, CreateTaskInput(
                            TaskContent(task.title, task.description, task.scheduledDate, task.estimatedMilliseconds, category),
                            copiedSteps.map { SubtaskDraft(it.title, it.estimatedMilliseconds) }, reminder))
                        check(result is Result.Success) { "Could not copy the Task: $result" }
                        result.data.task.taskLocalId
                    } else task.taskLocalId
                }
                TaskConflictChoice.LOCAL -> {
                    val current = checkNotNull(db.taskDAO().getTaskBySyncId(taskId, user))
                    check(current.deletedAt == null) { "Copy deleted work as a new Task" }
                    val replayChain = chain.filterNot { row ->
                        val journal = JsonParser.parseString(row.payloadJson).asJsonObject
                        journal["action"].asString == "DELETE_SUBTASK" && journal.getAsJsonArray("subtask_changes").all {
                            db.syncInboxDAO().get(user, TideEntityType.SUBTASK, UUID.fromString(it.asJsonObject["sync_id"].asString))?.operation == TideOperation.DELETE
                        }
                    }
                    val needsReopen = TaskStatus.fromCode(current.status).isTerminal && replayChain.any {
                        JsonParser.parseString(it.payloadJson).asJsonObject["action"].asString in
                            setOf("ADD_SUBTASK", "EDIT_SUBTASK", "REORDER_SUBTASKS", "EDIT_CHECKLIST", "DELETE_SUBTASK", "SET_SUBTASK_STATUS") }
                    if (needsReopen) {
                        val reset = current.copy(status = TaskStatus.TODO.code, completedDate = null)
                        val resetSteps = db.subtaskDAO().getActiveSubtasks(current.taskLocalId, user)
                            .map { it.copy(status = TaskStatus.TODO.code, completedDate = null) }
                        for (step in resetSteps) db.subtaskDAO().updateSubtask(step)
                        TaskIntentWriter(db).enqueue(reset, resetSteps,
                            "SET_TASK_STATUS", TideOperation.UPDATE, Instant.now(),
                            mapOf("status" to 0, "action_date" to java.time.LocalDate.now().toEpochDay()))
                    }
                    val stateAction = replayChain.any { JsonParser.parseString(it.payloadJson).asJsonObject["action"].asString.startsWith("SET_") }
                    val editAction = replayChain.any { JsonParser.parseString(it.payloadJson).asJsonObject["action"].asString in setOf("EDIT_TASK", "CREATE_TASK") }
                    var merged = current
                    if (editAction) merged = merged.copy(title = task.title, description = task.description,
                        scheduledDate = task.scheduledDate, estimatedMilliseconds = task.estimatedMilliseconds,
                        categoryLocalId = task.categoryLocalId?.takeIf { db.categoryDAO().getCategoryByLocalId(it, user)?.deletedAt == null },
                        relativeReminderJson = task.relativeReminderJson,
                        reminderLocalId = task.reminderLocalId?.takeIf { db.reminderDAO().getReminderByLocalId(it, user)?.deletedAt == null })
                    if (stateAction) merged = merged.copy(status = task.status, completedDate = task.completedDate)
                    else if (needsReopen) merged = merged.copy(status = TaskStatus.TODO.code, completedDate = null)
                    if (task.deletedAt != null) merged = merged.copy(deletedAt = task.deletedAt)
                    db.taskDAO().updateTask(merged)
                    for (step in localSteps) {
                        val journals = replayChain.map { JsonParser.parseString(it.payloadJson).asJsonObject }.filter {
                            it.getAsJsonArray("subtask_changes").any { item -> item.asJsonObject["sync_id"].asString == step.syncId.toString() }
                        }
                        val actions = journals.map { it["action"].asString }
                        if (actions.isNotEmpty()) {
                            val accepted = db.subtaskDAO().getSubtaskBySyncId(step.syncId, user)
                            val reorderedOnly = actions.all { it == "REORDER_SUBTASKS" }
                            if (reorderedOnly && accepted?.deletedAt != null) continue
                            check(accepted?.deletedAt == null || step.deletedAt != null) { "A step was deleted on the server; copy this Task to keep that work." }
                            var kept = accepted ?: step
                            val batchEdit = journals.any { it["action"].asString == "EDIT_CHECKLIST" &&
                                (accepted == null || it.getAsJsonObject("arguments").getAsJsonArray("edited_step_ids").any { id -> id.asString == step.syncId.toString() }) }
                            if (batchEdit || actions.any { it in setOf("CREATE_TASK", "ADD_SUBTASK", "EDIT_SUBTASK") }) kept = kept.copy(title = step.title, estimatedMilliseconds = step.estimatedMilliseconds)
                            if (actions.any { it in setOf("CREATE_TASK", "ADD_SUBTASK", "REORDER_SUBTASKS", "EDIT_CHECKLIST") }) kept = kept.copy(sortOrder = step.sortOrder)
                            if (actions.any { it.startsWith("SET_") }) kept = kept.copy(status = step.status, completedDate = step.completedDate)
                            if (actions.any { it.startsWith("DELETE_") } || journals.any { it["action"].asString == "EDIT_CHECKLIST" &&
                                it.getAsJsonArray("subtask_changes").any { item -> item.asJsonObject["sync_id"].asString == step.syncId.toString() && item.asJsonObject["deleted"].asBoolean } }) kept = kept.copy(deletedAt = step.deletedAt)
                            db.subtaskDAO().updateSubtask(kept)
                        }
                    }
                    for (row in replayChain) {
                        val journal = JsonParser.parseString(row.payloadJson).asJsonObject
                        if (current.seriesId != null) {
                            val payload = journal.getAsJsonObject("task").getAsJsonObject("data")
                            val accepted = server?.dataJson?.let { JsonParser.parseString(it).asJsonObject }
                            if (accepted != null) for (field in listOf("series_id", "slot_date", "generation_revision", "occurrence_number", "repeat_unit", "repeat_interval", "repeat_weekdays", "repeat_end_date", "repeat_anchor_date"))
                                payload.add(field, accepted[field] ?: JsonNull.INSTANCE)
                        }
                        if (journal["action"].asString in setOf("EDIT_CHECKLIST", "REORDER_SUBTASKS")) {
                            val records = journal.getAsJsonArray("subtask_changes").map { it.asJsonObject }.filterNot {
                                (it["deleted"].asBoolean || journal["action"].asString == "REORDER_SUBTASKS") &&
                                    db.syncInboxDAO().get(user, TideEntityType.SUBTASK, UUID.fromString(it["sync_id"].asString))?.operation == TideOperation.DELETE
                            }.toMutableList()
                            val known = records.map { it["sync_id"].asString }.toSet()
                            val acceptedRows = db.subtaskDAO().allForTask(task.taskLocalId, user).filter {
                                it.serverVersion != null && it.deletedAt == null && it.syncId.toString() !in known
                            }
                            for (step in acceptedRows) records += JsonObject().apply {
                                addProperty("sync_id", step.syncId.toString()); addProperty("base_version", step.serverVersion)
                                addProperty("deleted", false)
                                add("data", JsonObject().apply {
                                    addProperty("parent_task_sync_id", taskId.toString()); addProperty("title", step.title)
                                    addProperty("status", step.status); addProperty("sort_order", step.sortOrder)
                                    add("estimated_time", step.estimatedMilliseconds?.let { com.google.gson.JsonPrimitive(it) } ?: JsonNull.INSTANCE)
                                    add("completed_date", taskDateToEpochDay(step.completedDate)?.let { com.google.gson.JsonPrimitive(it) } ?: JsonNull.INSTANCE)
                                })
                            }
                            val ordered = records.filter { !it["deleted"].asBoolean && it["sync_id"].asString in known }
                                .sortedWith(compareBy<JsonObject> { it.getAsJsonObject("data")["sort_order"].asInt }.thenBy { it["sync_id"].asString }) +
                                records.filter { !it["deleted"].asBoolean && it["sync_id"].asString !in known }
                            ordered.forEachIndexed { index, item -> item.getAsJsonObject("data").addProperty("sort_order", index) }
                            journal.add("subtask_changes", JsonArray().apply { records.forEach { add(it) } })
                            for (item in ordered) {
                                val step = db.subtaskDAO().getSubtaskBySyncId(UUID.fromString(item["sync_id"].asString), user) ?: continue
                                db.subtaskDAO().updateSubtask(step.copy(sortOrder = item.getAsJsonObject("data")["sort_order"].asInt))
                            }
                        }
                        db.outboxDAO().insertMutation(row.copy(outboxLocalId = 0, mutationId = UUID.randomUUID(),
                        status = OutboxStatus.PENDING, attemptCount = 0, lastAttemptAt = null, lastError = null,
                        payloadJson = journal.toString(), conflictServerVersion = null, conflictServerDataJson = null, createdAt = Instant.now()))
                    }
                    if (!stateAction && replayChain.any { JsonParser.parseString(it.payloadJson).asJsonObject["action"].asString == "EDIT_CHECKLIST" }) {
                        val remaining = db.subtaskDAO().getActiveSubtasks(task.taskLocalId, user)
                        if (remaining.isNotEmpty() && remaining.all { TaskStatus.fromCode(it.status).isTerminal }) {
                            db.taskDAO().updateTask(merged.copy(status = TaskStatus.DONE.code, completedDate = java.time.LocalDate.now()))
                        }
                    }
                    task.taskLocalId
                }
            }
            for (id in setOf(task.taskLocalId, resolvedId)) {
                val current = db.taskDAO().getTaskByLocalId(id, user) ?: continue
                com.example.maiplan.utils.notifications.ReminderPlanWriter(db).task(current,
                    current.reminderLocalId?.let { db.reminderDAO().getReminderByLocalId(it, user) })
            }
            resolvedId
        }
        val warnings = mutableListOf<String>()
        if (omittedReminder) warnings += "The old or disabled reminder was not copied. Choose a future reminder for the new Task."
        runCatching { setOf(originalLocalId, resolved).forEach(invalidateAlarm) }.onFailure { warnings += "Reminder cancellation needs recovery." }
        runCatching { setOf(originalLocalId, resolved).forEach { recoverReminder(it) } }.onFailure { warnings += "Reminder scheduling needs recovery." }
        runCatching(requestSync).onFailure { warnings += "Sync scheduling needs retry." }
        TaskResolution(resolved, warnings.takeIf { it.isNotEmpty() }?.joinToString(" "))
    }

    companion object {
        fun create(context: Context): TaskConflictService {
            val app = context.applicationContext
            return TaskConflictService(MaiPlanDatabase.getDatabase(app), { SyncScheduler.runOneTimeSync(app) },
                recoverReminder = {
                    com.example.maiplan.utils.notifications.enqueueEventAlarmRecovery(app)
                    com.example.maiplan.utils.notifications.ReminderCoordinator(app).refreshTask(it)
                })
        }
    }
}
