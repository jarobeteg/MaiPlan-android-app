package com.example.maiplan.repository.task

import androidx.room.withTransaction
import com.example.maiplan.database.MaiPlanDatabase
import com.example.maiplan.database.entities.*
import com.example.maiplan.network.sync.TideEntityType
import com.example.maiplan.utils.notifications.ReminderPlanWriter
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class TaskOccurrencePage(val taskIds: List<Long>, val nextDate: LocalDate?)
data class CreateTaskSeriesInput(val revision: TaskSeriesRevision)

class TaskSeriesStore(private val db: MaiPlanDatabase, private val clock: Clock = Clock.systemDefaultZone()) {
    private val gson = GsonBuilder().serializeNulls().create()

    suspend fun enqueue(series: TaskSeriesEntity, action: String, fields: Map<String, Any?> = emptyMap()) {
        val payload = linkedMapOf<String, Any?>("format_version" to 1, "action" to action).apply { putAll(fields) }
        db.outboxDAO().insertMutation(OutboxEntity(mutationId = UUID.randomUUID(), userLocalId = series.userLocalId,
            entityType = TideEntityType.TASK_SERIES_ACTION, entitySyncId = series.syncId,
            dependencyPriority = if (action == "RECONCILE") -1 else 0,
            operation = if (action == "CREATE_SERIES") "CREATE" else "UPDATE", baseVersion = series.serverVersion,
            payloadJson = gson.toJson(payload), createdAt = clock.instant()))
    }

    suspend fun create(user: Long, input: CreateTaskSeriesInput): TaskEntity {
        val definition = TaskSeriesDefinition(listOf(input.revision)).also { it.validate() }
        val row = db.withTransaction {
            checkNotNull(db.userDAO().getActiveUserByLocalId(user))
            input.revision.category_sync_id?.let {
                check(checkNotNull(db.categoryDAO().getCategoryBySyncId(UUID.fromString(it), user)) { "Series Category is not owned by this user" }.deletedAt == null)
            }
            val series = TaskSeriesEntity(userLocalId = user, definitionJson = definition.json(), createdAt = clock.instant(), updatedAt = clock.instant())
            val saved = series.copy(seriesLocalId = db.taskSeriesDAO().insert(series))
            enqueue(saved, "CREATE_SERIES", mapOf("definition" to JsonParser.parseString(definition.json())))
            saved
        }
        val anchor = input.revision.rule().anchorDate
        val first = ensurePage(user, row.syncId, anchor, anchor).taskIds.first()
        return checkNotNull(db.taskDAO().getTaskByLocalId(first, user))
    }

    suspend fun ensurePage(user: Long, id: UUID, from: LocalDate, through: LocalDate): TaskOccurrencePage = db.withTransaction {
        validateTaskDate(from); validateTaskDate(through); require(from <= through)
        val series = checkNotNull(db.taskSeriesDAO().get(user, id))
        if (series.deletedAt != null) return@withTransaction TaskOccurrencePage(emptyList(), null)
        check(series.pendingOperationJson == null) { "The series is being updated. Finish syncing and try again." }
        val definition = TaskSeriesDefinition.decode(series.definitionJson)
        val slots = definition.slots(from, through).take(TASK_SERIES_PAGE_SIZE + 1).toList()
        val ids = mutableListOf<Long>()
        var needsAcceptance = false
        for (slot in slots.take(TASK_SERIES_PAGE_SIZE)) {
            if (db.taskSeriesDAO().exclusion(user, id, slot.date) != null) continue
            val identity = taskOccurrenceUuid(id, slot.date)
            val old = db.taskDAO().getTaskBySyncId(identity, user)
            if (old != null) {
                if (old.deletedAt == null) ids += old.taskLocalId
                if (old.serverVersion == null) needsAcceptance = true
                continue
            }
            val revision = slot.revision
            val category = revision.category_sync_id?.let {
                val link = db.categoryDAO().getCategoryBySyncId(UUID.fromString(it), user)
                check(link != null || db.syncInboxDAO().get(user, TideEntityType.CATEGORY, UUID.fromString(it))?.operation == "DELETE") { "Series Category is still syncing. Try again after sync." }
                link
            }
                ?.takeIf { it.deletedAt == null }?.categoryLocalId
            val task = TaskEntity(userLocalId = user, title = revision.title, description = revision.description,
                scheduledDate = slot.date, estimatedMilliseconds = revision.estimated_time, categoryLocalId = category,
                seriesId = id.toString(), occurrenceNumber = slot.number, slotDate = slot.date, generationRevision = revision.revision,
                repeatUnit = revision.repeat_unit, repeatInterval = revision.repeat_interval, repeatWeekdays = revision.repeat_weekdays,
                repeatAnchorDate = revision.rule().anchorDate, repeatEndDate = revision.rule().endDate,
                relativeReminderJson = revision.reminder?.json(), syncId = identity, createdAt = clock.instant(), updatedAt = clock.instant())
            val saved = task.copy(taskLocalId = db.taskDAO().insertTask(task))
            for ((order, template) in revision.steps.withIndex()) db.subtaskDAO().insertSubtask(SubtaskEntity(
                userLocalId = user, taskLocalId = saved.taskLocalId, title = template.title, sortOrder = order,
                estimatedMilliseconds = template.estimated_time, syncId = taskTemplateStepUuid(identity, UUID.fromString(template.key)),
                createdAt = clock.instant(), updatedAt = clock.instant()))
            ReminderPlanWriter(db).task(saved, null, clock.instant())
            ids += saved.taskLocalId; needsAcceptance = true
        }
        val end = slots.take(TASK_SERIES_PAGE_SIZE).lastOrNull()?.date ?: through
        if (needsAcceptance) {
            val fields = mapOf("expected_revision" to definition.revisions.last().revision,
                "from_date" to from.toEpochDay(), "through_date" to end.toEpochDay())
            val duplicate = db.outboxDAO().entityIntents(user, TideEntityType.TASK_SERIES_ACTION, id).any { row ->
                if (row.entityType != TideEntityType.TASK_SERIES_ACTION || row.entitySyncId != id || row.payloadJson == null) false
                else JsonParser.parseString(row.payloadJson).asJsonObject.let {
                    it["action"].asString == "MATERIALIZE" && it["from_date"]?.asLong == from.toEpochDay() &&
                        it["through_date"]?.asLong == end.toEpochDay() && it["expected_revision"]?.asInt == definition.revisions.last().revision
                }
            }
            if (!duplicate) enqueue(series, "MATERIALIZE", fields)
        }
        TaskOccurrencePage(ids, slots.getOrNull(TASK_SERIES_PAGE_SIZE)?.date)
    }

    suspend fun prepareRange(user: Long, from: LocalDate, through: LocalDate) {
        require(through.toEpochDay() - from.toEpochDay() <= 366) { "Prepare ranges up to 366 days, or use ensurePage for longer calendar history." }
        resumeLocalOperations(user)
        for (series in db.taskSeriesDAO().all(user).filter { it.deletedAt == null }) {
            var next: LocalDate? = from
            while (next != null) { next = ensurePage(user, series.syncId, next, through).nextDate; kotlinx.coroutines.yield() }
        }
    }

    suspend fun prepareList(user: Long) {
        prepareUpcoming(user)
        loadMoreHistory(user, initialOnly = true)
    }
    suspend fun prepareUpcoming(user: Long) {
        val today = LocalDate.now(clock)
        prepareRange(user, today, minOf(TaskContract.MAX_DATE, today.plusDays(14)))
    }
    suspend fun loadMoreHistory(user: Long, initialOnly: Boolean = false): Boolean {
        var more = false
        val today = LocalDate.now(clock)
        for (row in db.taskSeriesDAO().all(user).filter { it.deletedAt == null && it.pendingOperationJson == null }) {
            if (initialOnly && row.historyThrough != null) continue
            val anchor = TaskSeriesDefinition.decode(row.definitionJson).revisions.first().rule().anchorDate
            val from = row.historyThrough?.takeIf { it < TaskContract.MAX_DATE }?.plusDays(1) ?: anchor
            if (from > today || row.historyThrough == TaskContract.MAX_DATE) continue
            val page = ensurePage(user, row.syncId, from, today)
            val reached = page.nextDate?.minusDays(1) ?: today
            db.withTransaction { db.taskSeriesDAO().get(user, row.syncId)?.let { db.taskSeriesDAO().update(it.copy(historyThrough = reached)) } }
            more = more || page.nextDate != null
        }
        return more
    }

    suspend fun editFuture(user: Long, id: UUID, revision: TaskSeriesRevision, expectedDefinition: String, cutoff: LocalDate) {
        val requestedOn = LocalDate.now(clock)
        require(cutoff > requestedOn) { "Future edits start tomorrow or later" }
        db.withTransaction {
            val row = checkNotNull(db.taskSeriesDAO().get(user, id))
            check(row.deletedAt == null && row.pendingOperationJson == null && row.definitionJson == expectedDefinition) { "The series changed. Reload before editing future occurrences." }
            val history = TaskSeriesDefinition.decode(row.definitionJson)
            val next = revision.copy(revision = history.revisions.size + 1, effective_from = cutoff.toEpochDay())
            val definition = TaskSeriesDefinition(history.revisions + next).also { it.validate() }
            val operation = UUID.randomUUID()
            val updated = row.copy(definitionJson = definition.json(), pendingOperationJson = gson.toJson(mapOf("id" to operation.toString(), "local" to true, "kind" to "EDIT", "cutoff" to cutoff.toEpochDay(), "action_date" to requestedOn.toEpochDay(), "after" to 0)), updatedAt = clock.instant())
            db.taskSeriesDAO().update(updated)
            enqueue(updated, "EDIT_FUTURE", mapOf("definition" to JsonParser.parseString(definition.json()),
                "cutoff" to cutoff.toEpochDay(), "requested_on" to requestedOn.toEpochDay(), "operation_id" to operation.toString()))
        }
        resumeLocalOperations(user)
    }
    suspend fun deleteAll(user: Long, id: UUID) {
        db.withTransaction {
            val row = checkNotNull(db.taskSeriesDAO().get(user, id))
            check(row.deletedAt == null && row.pendingOperationJson == null)
            val operation = UUID.randomUUID()
            val updated = row.copy(deletedAt = clock.instant(), pendingOperationJson = gson.toJson(mapOf("id" to operation.toString(), "local" to true, "kind" to "DELETE", "after" to 0)), updatedAt = clock.instant())
            db.taskSeriesDAO().update(updated)
            enqueue(updated, "DELETE_ALL", mapOf("operation_id" to operation.toString()))
        }
        resumeLocalOperations(user)
    }
    suspend fun resumeLocalOperations(user: Long) {
        for (series in db.taskSeriesDAO().all(user).filter { it.pendingOperationJson?.let { json -> JsonParser.parseString(json).asJsonObject["local"]?.asBoolean == true } == true }) {
            while (reconcileLocalPage(user, series.syncId)) kotlinx.coroutines.yield()
        }
    }
    private suspend fun reconcileLocalPage(user: Long, id: UUID): Boolean = db.withTransaction {
        val row = checkNotNull(db.taskSeriesDAO().get(user, id))
        val operation = row.pendingOperationJson?.let { JsonParser.parseString(it).asJsonObject } ?: return@withTransaction false
        val definition = TaskSeriesDefinition.decode(row.definitionJson)
        val tasks = db.taskDAO().seriesPage(user, id.toString(), operation["after"].asLong, TASK_SERIES_PAGE_SIZE + 1)
        for (task in tasks.take(TASK_SERIES_PAGE_SIZE)) {
            operation.addProperty("after", task.taskLocalId)
            if (task.deletedAt != null) continue
            val cutoff = operation["cutoff"]?.asLong
            if (operation["kind"].asString == "EDIT" && checkNotNull(task.scheduledDate).toEpochDay() < checkNotNull(cutoff)) continue
            val slot = checkNotNull(task.slotDate)
            val matching = definition.slots(slot, slot).firstOrNull()
            val now = clock.instant()
            val steps = db.subtaskDAO().allForTask(task.taskLocalId, user)
            val delete = operation["kind"].asString == "DELETE" || (matching == null && slot.toEpochDay() >= checkNotNull(cutoff))
            if (delete) {
                exclude(task)
                db.taskDAO().updateTask(task.copy(deletedAt = now, updatedAt = now))
                steps.filter { it.deletedAt == null }.forEach { db.subtaskDAO().updateSubtask(it.copy(deletedAt = now, updatedAt = now)) }
                ReminderPlanWriter(db).task(task.copy(deletedAt = now), null, now)
            } else {
                val revision = definition.revisions.last { it.effective_from <= checkNotNull(task.scheduledDate).toEpochDay() }
                val category = revision.category_sync_id?.let { db.categoryDAO().getCategoryBySyncId(UUID.fromString(it), user) }?.takeIf { it.deletedAt == null }?.categoryLocalId
                var updated = task.copy(title = revision.title, description = revision.description, estimatedMilliseconds = revision.estimated_time,
                    categoryLocalId = category, reminderLocalId = null, relativeReminderJson = revision.reminder?.json(),
                    generationRevision = revision.revision, occurrenceNumber = matching?.number ?: task.occurrenceNumber, repeatUnit = revision.repeat_unit,
                    repeatInterval = revision.repeat_interval, repeatWeekdays = revision.repeat_weekdays, repeatAnchorDate = revision.rule().anchorDate,
                    repeatEndDate = revision.rule().endDate, occurrenceOverride = false, updatedAt = now)
                db.taskDAO().updateTask(updated)
                val desired = revision.steps.mapIndexed { index, template -> taskTemplateStepUuid(task.syncId, UUID.fromString(template.key)) to (index to template) }.toMap().toMutableMap()
                for (step in steps.filter { it.deletedAt == null }) {
                    val item = desired.remove(step.syncId)
                    db.subtaskDAO().updateSubtask(if (item == null) step.copy(deletedAt = now, updatedAt = now) else
                        step.copy(title = item.second.title, estimatedMilliseconds = item.second.estimated_time, sortOrder = item.first, updatedAt = now))
                }
                for ((identity, item) in desired) {
                    if (steps.any { it.syncId == identity }) continue
                    val terminal = TaskStatus.fromCode(task.status).isTerminal
                    db.subtaskDAO().insertSubtask(SubtaskEntity(userLocalId = user, taskLocalId = task.taskLocalId, syncId = identity,
                        title = item.second.title, estimatedMilliseconds = item.second.estimated_time, sortOrder = item.first,
                        status = if (terminal) task.status else 0, completedDate = if (task.status == 1) task.completedDate else null, createdAt = now, updatedAt = now))
                }
                val remaining = db.subtaskDAO().getActiveSubtasks(task.taskLocalId, user)
                if (!TaskStatus.fromCode(updated.status).isTerminal && remaining.isNotEmpty() && remaining.all { TaskStatus.fromCode(it.status).isTerminal }) {
                    updated = updated.copy(status = TaskStatus.DONE.code,
                        completedDate = checkNotNull(taskDateFromEpochDay(operation["action_date"].asLong)))
                    db.taskDAO().updateTask(updated)
                }
                ReminderPlanWriter(db).task(updated, null, now)
            }
            task.reminderLocalId?.let { reminderId ->
                if (db.reminderDAO().countActiveReferences(reminderId) == 0 &&
                    db.reminderDAO().getReminderByLocalId(reminderId, user)?.deletedAt == null)
                    com.example.maiplan.repository.reminder.ReminderMutationWriter(db).delete(reminderId, user, now)
            }
        }
        val more = tasks.size > TASK_SERIES_PAGE_SIZE
        db.taskSeriesDAO().update(row.copy(pendingOperationJson = if (more) operation.toString() else null))
        more
    }
    suspend fun exclude(task: TaskEntity) {
        val id = task.seriesId?.let(UUID::fromString) ?: return
        val slot = checkNotNull(task.slotDate)
        db.taskSeriesDAO().exclude(TaskExclusionEntity(taskExclusionUuid(id, slot), task.userLocalId, id, slot))
    }
}
