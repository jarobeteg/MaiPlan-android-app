package com.example.maiplan.repository.task

import androidx.room.withTransaction
import com.example.maiplan.database.MaiPlanDatabase
import com.example.maiplan.database.entities.ReminderEntity
import com.example.maiplan.database.entities.SubtaskEntity
import com.example.maiplan.database.entities.TaskEntity
import com.example.maiplan.network.sync.TideOperation
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.handleLocalResponse
import com.example.maiplan.repository.reminder.ReminderMutationWriter
import com.example.maiplan.utils.notifications.ReminderPlanWriter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

class TaskLocalDataSource(private val database: MaiPlanDatabase, private val clock: Clock = Clock.systemDefaultZone(),
    private val generationCommitted: () -> Unit = {}) {
    private val tasks = database.taskDAO()
    private val steps = database.subtaskDAO()
    private val intents = TaskIntentWriter(database)
    private val reminders = ReminderMutationWriter(database)
    val series = TaskSeriesStore(database, clock)
    suspend fun seriesDefinition(user: Long, id: java.util.UUID) = database.taskSeriesDAO().get(user, id)
    suspend fun taskEntity(user: Long, id: Long) = tasks.getTaskByLocalId(id, user)
    suspend fun categorySyncId(user: Long, id: Long?) = id?.let { database.categoryDAO().getCategoryByLocalId(it, user)?.takeIf { row -> row.deletedAt == null }?.syncId?.toString() }
    suspend fun categoryLocalId(user: Long, id: String?) = id?.let { database.categoryDAO().getCategoryBySyncId(java.util.UUID.fromString(it), user)?.takeIf { row -> row.deletedAt == null }?.categoryLocalId }
    suspend fun createSeries(user: Long, input: CreateTaskSeriesInput): Result<TaskWriteOutcome> = handleLocalResponse {
        TaskWriteOutcome(series.create(user, input), true)
    }
    suspend fun deleteAllSeries(user: Long, id: java.util.UUID, taskId: Long): Result<TaskWriteOutcome> = handleLocalResponse {
        series.deleteAll(user, id)
        TaskWriteOutcome(checkNotNull(tasks.getTaskByLocalId(taskId, user)), true)
    }

    fun observeTask(localId: Long, userLocalId: Long): Flow<TaskSnapshot?> =
        tasks.observeActiveTask(localId, userLocalId).map { it?.snapshot() }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun observeTaskSummaries(user: Long, query: TaskQuery, sort: TaskReadSort = TaskReadSort.PLANNED_DAY,
        limit: Int = TASK_READ_PAGE_SIZE, offset: Int = 0): Flow<TaskSummaryPage> {
        require(limit in 1..500 && offset >= 0 && offset <= Int.MAX_VALUE - limit)
        val args = query.sqlArguments()
        return database.taskSeriesDAO().observeAll(user).map { rows ->
            rows.map { listOf(it.syncId.toString(), it.definitionJson, it.pendingOperationJson, it.deletedAt?.toString()) }
        }.distinctUntilChanged().flatMapLatest { kotlinx.coroutines.flow.flow {
            if (args.dateMode == 2) series.prepareRange(user, checkNotNull(args.from), checkNotNull(args.through))
            else if (query.date != TaskDateFilter.Unscheduled) series.prepareList(user)
            generationCommitted()
            emitAll(kotlinx.coroutines.flow.combine(
                tasks.observeSummaries(user, args.categoryMode, args.categoryId, args.dateMode, args.from, args.through,
                    query.statuses.map { it.code }, query.search.trim().lowercase(java.util.Locale.ROOT), sort.ordinal, limit + 1, offset),
                tasks.observeHasTasks(user), database.taskSeriesDAO().observeAll(user)
            ) { records, hasAny, definitions ->
                val historyEnd = (query.date as? TaskDateFilter.Before)?.date?.minusDays(1) ?: LocalDate.now(clock)
                val incomplete = query.date != TaskDateFilter.Unscheduled && args.dateMode != 2 && definitions.any { row ->
                    row.deletedAt == null && TaskSeriesDefinition.decode(row.definitionJson).revisions.minOf { it.effective_from } <= historyEnd.toEpochDay() &&
                        (row.historyThrough == null || row.historyThrough < historyEnd)
                }
                TaskSummaryPage(records.take(limit).map { it.summary() }, offset, records.size > limit, hasAny, incomplete)
            }.distinctUntilChanged())
        } }.flowOn(kotlinx.coroutines.Dispatchers.IO)
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun observeTasks(userLocalId: Long, query: TaskQuery = TaskQuery()): Flow<List<TaskSnapshot>> {
        val args = query.sqlArguments()
        return database.taskSeriesDAO().observeAll(userLocalId).map { rows ->
            rows.map { listOf(it.syncId.toString(), it.definitionJson, it.pendingOperationJson, it.deletedAt?.toString()) }
        }.distinctUntilChanged().flatMapLatest { kotlinx.coroutines.flow.flow {
            if (args.dateMode == 2) series.prepareRange(userLocalId, checkNotNull(args.from), checkNotNull(args.through))
            else series.prepareList(userLocalId)
            generationCommitted()
            emitAll(tasks.observeActiveTasks(userLocalId, args.categoryMode, args.categoryId, args.dateMode,
                args.from, args.through, query.statuses.map { it.code }, query.search.trim().lowercase(java.util.Locale.ROOT)).map { rows -> rows.map { it.snapshot() } })
        } }
    }

    suspend fun getTask(localId: Long, userLocalId: Long): Result<TaskSnapshot> = handleLocalResponse {
        checkNotNull(tasks.getActiveTask(localId, userLocalId)) { "Task was not found for this user" }.snapshot()
    }

    suspend fun getTasks(userLocalId: Long, query: TaskQuery = TaskQuery()): Result<List<TaskSnapshot>> = handleLocalResponse {
        val args = query.sqlArguments()
        if (args.dateMode == 2) series.prepareRange(userLocalId, checkNotNull(args.from), checkNotNull(args.through)) else series.prepareList(userLocalId)
        generationCommitted()
        tasks.getActiveTasks(userLocalId, args.categoryMode, args.categoryId, args.dateMode,
            args.from, args.through, query.statuses.map { it.code }, query.search.trim().lowercase(java.util.Locale.ROOT)).map { it.snapshot() }
    }

    suspend fun createTask(userLocalId: Long, input: CreateTaskInput): Result<TaskWriteOutcome> = handleLocalResponse {
        database.withTransaction {
            ensureUser(userLocalId)
            val content = normalizeContent(input.content)
            val categoryId = categoryLink(content.categoryLocalId, userLocalId)
            val drafts = input.subtasks.map(::normalizeStep)
            val now = clock.instant()
            val reminder = input.reminder?.let { validateFutureReminder(it, now); reminders.create(it.entity(userLocalId), userLocalId, now) }
            val row = TaskEntity(userLocalId = userLocalId, title = content.title, description = content.description,
                scheduledDate = content.scheduledDate, estimatedMilliseconds = content.estimatedMilliseconds,
                categoryLocalId = categoryId, reminderLocalId = reminder?.reminderLocalId, createdAt = now, updatedAt = now)
            val stored = row.copy(taskLocalId = tasks.insertTask(row))
            val children = drafts.mapIndexed { index, draft ->
                val step = SubtaskEntity(userLocalId = userLocalId, taskLocalId = stored.taskLocalId, title = draft.title,
                    sortOrder = index, estimatedMilliseconds = draft.estimatedMilliseconds, createdAt = now, updatedAt = now)
                step.copy(subtaskLocalId = steps.insertSubtask(step))
            }
            intents.enqueue(stored, children, "CREATE_TASK", TideOperation.CREATE, now)
            invalidatePending(stored)
            TaskWriteOutcome(stored, changed = true, invalidateReminder = reminder != null)
        }
    }

    suspend fun updateTask(userLocalId: Long, input: UpdateTaskInput): Result<TaskWriteOutcome> = handleLocalResponse {
        database.withTransaction {
            val existing = ownedActiveTask(input.taskLocalId, userLocalId)
            if (input.expectedRevision != null && input.expectedRevision != taskEditorRevision(existing)) {
                throw TaskEditorChangedException()
            }
            val currentSteps = steps.getActiveSubtasks(existing.taskLocalId, userLocalId)
            if (input.expectedChecklistRevision != null && input.expectedChecklistRevision != checklistEditorRevision(currentSteps)) {
                throw TaskEditorChangedException()
            }
            val desiredSteps = input.checklist?.map { it.copy(title = normalizeTaskTitle(it.title)).also { row -> validateTaskEstimate(row.estimatedMilliseconds) } }
            require(desiredSteps == null || desiredSteps.map { it.syncId }.distinct().size == desiredSteps.size) { "Duplicate Subtask identity" }
            val content = normalizeContent(input.content)
            val categoryId = categoryLink(content.categoryLocalId, userLocalId, existing.categoryLocalId)
            val now = clock.instant()
            val existingReminder = existing.reminderLocalId?.let { ownedReminder(it, userLocalId) }
            if (input.expectedReminderRevision != null && input.expectedReminderRevision !=
                taskReminderRevision(existingReminder?.takeIf { it.deletedAt == null })) throw TaskEditorChangedException()
            var reminderEdited = false
            val reminderId = when (val change = input.reminder) {
                is TaskReminderChange.Relative -> { change.rule.validate(); require(content.scheduledDate != null); null }
                TaskReminderChange.Keep -> existingReminder?.takeIf { it.deletedAt == null }?.reminderLocalId
                TaskReminderChange.Remove -> null
                is TaskReminderChange.Set -> {
                    val draft = change.draft.entity(userLocalId)
                    if (existingReminder == null || existingReminder.deletedAt != null) {
                        validateFutureReminder(change.draft, now)
                        reminderEdited = true
                        reminders.create(draft, userLocalId, now).reminderLocalId
                    }
                    else {
                        check(database.reminderDAO().countActiveReferences(existingReminder.reminderLocalId) == 1) {
                            "A shared Reminder cannot be edited through a Task"
                        }
                        if (!existingReminder.sameConfiguration(draft)) {
                            validateFutureReminder(change.draft, now)
                            reminderEdited = true
                            reminders.update(draft.copy(reminderLocalId = existingReminder.reminderLocalId), userLocalId, now)
                        }
                        existingReminder.reminderLocalId
                    }
                }
            }
            val candidate = existing.copy(title = content.title, description = content.description, scheduledDate = content.scheduledDate,
                estimatedMilliseconds = content.estimatedMilliseconds, categoryLocalId = categoryId, reminderLocalId = reminderId,
                occurrenceOverride = existing.seriesId != null || existing.occurrenceOverride,
                relativeReminderJson = when (val change = input.reminder) {
                    TaskReminderChange.Keep -> existing.relativeReminderJson
                    is TaskReminderChange.Relative -> change.rule.json()
                    else -> null
                })
            val contentChanged = candidate != existing || reminderEdited
            var updated = if (contentChanged) candidate.copy(updatedAt = now) else existing
            if (contentChanged) {
                check(tasks.updateTask(updated) == 1)
                intents.enqueue(updated, emptyList(), "EDIT_TASK", TideOperation.UPDATE, now)
            }
            val checklistChanged = desiredSteps != null && desiredSteps != currentSteps.map { ChecklistStepInput(it.syncId, it.title, it.estimatedMilliseconds) }
            if (checklistChanged) {
                check(!TaskStatus.fromCode(existing.status).isTerminal) { "Reopen the Task before changing its checklist" }
                val byId = currentSteps.associateBy { it.syncId }
                val editedIds = mutableListOf<String>()
                val finalRows = requireNotNull(desiredSteps).mapIndexed { index, draft ->
                    val old = byId[draft.syncId]
                    if (old == null) {
                        check(steps.getSubtaskBySyncId(draft.syncId, userLocalId) == null) { "A deleted Subtask cannot be restored by editing its checklist" }
                        val row = SubtaskEntity(userLocalId = userLocalId, taskLocalId = existing.taskLocalId,
                            syncId = draft.syncId, title = draft.title, estimatedMilliseconds = draft.estimatedMilliseconds,
                            sortOrder = index, createdAt = now, updatedAt = now)
                        row.copy(subtaskLocalId = steps.insertSubtask(row))
                    } else {
                        if (old.title != draft.title || old.estimatedMilliseconds != draft.estimatedMilliseconds) editedIds += old.syncId.toString()
                        val row = old.copy(title = draft.title, estimatedMilliseconds = draft.estimatedMilliseconds, sortOrder = index)
                        if (row == old) old else row.copy(updatedAt = now).also { check(steps.updateSubtask(it) == 1) }
                    }
                }
                val retainedIds = desiredSteps.map { it.syncId }.toSet()
                val removed = currentSteps.filter { it.syncId !in retainedIds }.map { row ->
                    row.copy(deletedAt = now, updatedAt = now).also { check(steps.updateSubtask(it) == 1) }
                }
                updated = updated.copy(updatedAt = now)
                if (removed.isNotEmpty() && finalRows.isNotEmpty() && finalRows.all { TaskStatus.fromCode(it.status).isTerminal }) {
                    updated = updated.copy(status = TaskStatus.DONE.code, completedDate = LocalDate.now(clock))
                }
                check(tasks.updateTask(updated) == 1)
                intents.enqueue(updated, finalRows + removed, "EDIT_CHECKLIST", TideOperation.UPDATE, now,
                    mapOf("action_date" to taskDateToEpochDay(LocalDate.now(clock)), "edited_step_ids" to editedIds))
            }
            if (!contentChanged && !checklistChanged) return@withTransaction TaskWriteOutcome(existing, false)
            if (existing.reminderLocalId != null && existing.reminderLocalId != reminderId) deleteReminderIfUnreferenced(existing.reminderLocalId, userLocalId, now)
            invalidatePending(updated)
            TaskWriteOutcome(updated, true)
        }
    }

    suspend fun setTaskStatus(localId: Long, userLocalId: Long, status: TaskStatus): Result<TaskWriteOutcome> = handleLocalResponse {
        database.withTransaction {
            val task = ownedActiveTask(localId, userLocalId)
            val children = steps.getActiveSubtasks(localId, userLocalId)
            val today = LocalDate.now(clock)
            saveChecklist(task, children, applyTaskStatus(checklist(task, children), status, today), "SET_TASK_STATUS",
                mapOf("status" to status.code, "action_date" to taskDateToEpochDay(today)))
        }
    }

    suspend fun setSubtaskStatus(localId: Long, userLocalId: Long, status: TaskStatus): Result<TaskWriteOutcome> = handleLocalResponse {
        database.withTransaction {
            val step = ownedStep(localId, userLocalId)
            val task = ownedActiveTask(step.taskLocalId, userLocalId)
            val children = steps.getActiveSubtasks(task.taskLocalId, userLocalId)
            val today = LocalDate.now(clock)
            saveChecklist(task, children, applySubtaskStatus(checklist(task, children), step.syncId, status, today), "SET_SUBTASK_STATUS",
                mapOf("subtask_sync_id" to step.syncId.toString(), "status" to status.code, "action_date" to taskDateToEpochDay(today)))
        }
    }

    suspend fun addSubtask(taskLocalId: Long, userLocalId: Long, input: SubtaskDraft): Result<TaskWriteOutcome> = handleLocalResponse {
        database.withTransaction {
            val task = editableChecklist(taskLocalId, userLocalId)
            val draft = normalizeStep(input)
            val existing = steps.getActiveSubtasks(taskLocalId, userLocalId)
            val now = clock.instant()
            val reordered = existing.mapIndexedNotNull { index, row ->
                if (row.sortOrder == index) null else row.copy(sortOrder = index, updatedAt = now).also { check(steps.updateSubtask(it) == 1) }
            }
            val row = SubtaskEntity(userLocalId = userLocalId, taskLocalId = taskLocalId, title = draft.title,
                sortOrder = existing.size, estimatedMilliseconds = draft.estimatedMilliseconds, createdAt = now, updatedAt = now)
            val stored = row.copy(subtaskLocalId = steps.insertSubtask(row))
            recordStepEdit(task, reordered + stored, "ADD_SUBTASK", now)
        }
    }

    suspend fun updateSubtask(localId: Long, userLocalId: Long, input: SubtaskDraft): Result<TaskWriteOutcome> = handleLocalResponse {
        database.withTransaction {
            val existing = ownedStep(localId, userLocalId)
            val task = editableChecklist(existing.taskLocalId, userLocalId)
            val draft = normalizeStep(input)
            val candidate = existing.copy(title = draft.title, estimatedMilliseconds = draft.estimatedMilliseconds)
            if (candidate == existing) return@withTransaction TaskWriteOutcome(task, false)
            val now = clock.instant()
            val updated = candidate.copy(updatedAt = now)
            check(steps.updateSubtask(updated) == 1)
            recordStepEdit(task, listOf(updated), "EDIT_SUBTASK", now)
        }
    }

    suspend fun reorderSubtasks(taskLocalId: Long, userLocalId: Long, orderedLocalIds: List<Long>,
        expectedOrder: List<Long>? = null): Result<TaskWriteOutcome> = handleLocalResponse {
        database.withTransaction {
            val task = editableChecklist(taskLocalId, userLocalId)
            val active = steps.getActiveSubtasks(taskLocalId, userLocalId)
            if (expectedOrder != null && expectedOrder != active.map { it.subtaskLocalId }) throw TaskEditorChangedException()
            require(orderedLocalIds.size == active.size && orderedLocalIds.toSet() == active.map { it.subtaskLocalId }.toSet()) {
                "Reordering requires every active step exactly once"
            }
            val now = clock.instant()
            val byId = active.associateBy { it.subtaskLocalId }
            val changed = orderedLocalIds.mapIndexedNotNull { index, id ->
                val row = checkNotNull(byId[id])
                if (row.sortOrder == index) null else row.copy(sortOrder = index, updatedAt = now).also { check(steps.updateSubtask(it) == 1) }
            }
            if (changed.isEmpty()) TaskWriteOutcome(task, false) else recordStepEdit(task,
                steps.getActiveSubtasks(taskLocalId, userLocalId), "REORDER_SUBTASKS", now)
        }
    }

    suspend fun softDeleteSubtask(localId: Long, userLocalId: Long): Result<TaskWriteOutcome> = handleLocalResponse {
        database.withTransaction {
            val existing = checkNotNull(steps.getSubtaskByLocalId(localId, userLocalId)) { "Step was not found for this user" }
            val task = ownedActiveTask(existing.taskLocalId, userLocalId)
            if (existing.deletedAt != null) return@withTransaction TaskWriteOutcome(task, false)
            check(!TaskStatus.fromCode(task.status).isTerminal) { "Reopen the Task before changing its checklist" }
            val remaining = steps.getActiveSubtasks(task.taskLocalId, userLocalId).filter { it.subtaskLocalId != localId }
            val now = clock.instant()
            val tombstone = existing.copy(updatedAt = now, deletedAt = now)
            check(steps.updateSubtask(tombstone) == 1)
            val parent = when {
                remaining.isNotEmpty() && remaining.all { TaskStatus.fromCode(it.status).isTerminal } -> task.copy(status = TaskStatus.DONE.code, completedDate = LocalDate.now(clock))
                else -> task
            }.copy(updatedAt = now)
            check(tasks.updateTask(parent) == 1)
            intents.enqueue(parent, listOf(tombstone), "DELETE_SUBTASK", TideOperation.UPDATE, now,
                mapOf("action_date" to taskDateToEpochDay(LocalDate.now(clock))))
            invalidatePending(parent)
            TaskWriteOutcome(parent, true)
        }
    }

    suspend fun softDeleteTask(localId: Long, userLocalId: Long): Result<TaskWriteOutcome> = handleLocalResponse {
        database.withTransaction {
            ensureUser(userLocalId)
            val existing = checkNotNull(tasks.getTaskByLocalId(localId, userLocalId)) { "Task was not found for this user" }
            if (existing.deletedAt != null) return@withTransaction TaskWriteOutcome(existing, false)
            val now = clock.instant()
            val tombstones = steps.getActiveSubtasks(localId, userLocalId).map { row ->
                row.copy(updatedAt = now, deletedAt = now).also { check(steps.updateSubtask(it) == 1) }
            }
            val parent = existing.copy(updatedAt = now, deletedAt = now)
            series.exclude(existing)
            check(tasks.updateTask(parent) == 1)
            intents.supersedeNeverAttempted(parent)
            intents.enqueue(parent, tombstones, "DELETE_TASK", TideOperation.DELETE, now)
            invalidatePending(parent)
            existing.reminderLocalId?.let { deleteReminderIfUnreferenced(it, userLocalId, now) }
            TaskWriteOutcome(parent, true)
        }
    }

    suspend fun normalizePendingCategoryLinks(userLocalId: Long) = TaskCategoryLinks(database).normalizePending(userLocalId)

    private suspend fun saveChecklist(task: TaskEntity, children: List<SubtaskEntity>, desired: TaskChecklist,
        action: String, arguments: Map<String, Any?>): TaskWriteOutcome {
        val now = clock.instant()
        val desiredById = desired.subtasks.associateBy { it.syncId }
        val changed = children.mapNotNull { row ->
            val completion = checkNotNull(desiredById[row.syncId]).completion
            if (row.status == completion.status.code && row.completedDate == completion.completedDate) null
            else row.copy(status = completion.status.code, completedDate = completion.completedDate, updatedAt = now)
                .also { check(steps.updateSubtask(it) == 1) }
        }
        val candidate = task.copy(status = desired.completion.status.code, completedDate = desired.completion.completedDate)
        if (changed.isEmpty() && candidate == task) return TaskWriteOutcome(task, false)
        val updated = candidate.copy(updatedAt = now)
        check(tasks.updateTask(updated) == 1)
        intents.enqueue(updated, changed, action, TideOperation.UPDATE, now, arguments)
        invalidatePending(updated)
        return TaskWriteOutcome(updated, true)
    }

    private fun checklist(task: TaskEntity, children: List<SubtaskEntity>) = TaskChecklist(
        TaskCompletion(TaskStatus.fromCode(task.status), task.completedDate),
        children.map { SubtaskCompletion(it.syncId, TaskCompletion(TaskStatus.fromCode(it.status), it.completedDate)) })

    private suspend fun recordStepEdit(task: TaskEntity, changed: List<SubtaskEntity>, action: String, now: Instant): TaskWriteOutcome {
        val updated = task.copy(updatedAt = now)
        check(tasks.updateTask(updated) == 1)
        intents.enqueue(updated, changed, action, TideOperation.UPDATE, now)
        invalidatePending(updated)
        return TaskWriteOutcome(updated, true)
    }

    private suspend fun ensureUser(user: Long) {
        checkNotNull(database.userDAO().getActiveUserByLocalId(user)) { "Task user is missing or deleted" }
    }

    private suspend fun ownedActiveTask(localId: Long, user: Long): TaskEntity {
        ensureUser(user)
        val row = checkNotNull(tasks.getTaskByLocalId(localId, user)?.takeIf { it.deletedAt == null }) { "Task is missing, deleted, or not owned by this user" }
        row.seriesId?.let {
            val series = checkNotNull(database.taskSeriesDAO().get(user, java.util.UUID.fromString(it))) { "Series is still syncing" }
            check(series.deletedAt == null) { "Series was deleted" }
        }
        return row
    }

    private suspend fun editableChecklist(localId: Long, user: Long): TaskEntity = ownedActiveTask(localId, user).also {
        check(!TaskStatus.fromCode(it.status).isTerminal) { "Reopen the Task before changing its checklist" }
    }

    private suspend fun ownedStep(localId: Long, user: Long): SubtaskEntity =
        checkNotNull(steps.getSubtaskByLocalId(localId, user)?.takeIf { it.deletedAt == null }) { "Step is missing, deleted, or not owned by this user" }

    private suspend fun ownedReminder(localId: Long, user: Long): ReminderEntity =
        checkNotNull(database.reminderDAO().getReminderByLocalId(localId, user)) { "Reminder is not owned by this user" }

    private suspend fun categoryLink(id: Long?, user: Long, previousId: Long? = null): Long? {
        if (id == null) return null
        val row = checkNotNull(database.categoryDAO().getCategoryByLocalId(id, user)) { "Category is not owned by this user" }
        if (row.deletedAt == null) return id
        check(id == previousId) { "A deleted Category cannot be selected" }
        return null
    }

    private suspend fun deleteReminderIfUnreferenced(id: Long, user: Long, now: Instant) {
        ownedReminder(id, user)
        if (database.reminderDAO().countActiveReferences(id) == 0) reminders.delete(id, user, now)
    }

    private suspend fun invalidatePending(task: TaskEntity) {
        val reminder = task.reminderLocalId?.let { database.reminderDAO().getReminderByLocalId(it, task.userLocalId) }
        ReminderPlanWriter(database).task(task, reminder, clock.instant())
    }
}

private fun normalizeContent(input: TaskContent): TaskContent {
    validateTaskDate(input.scheduledDate)
    validateTaskEstimate(input.estimatedMilliseconds)
    return input.copy(title = normalizeTaskTitle(input.title), description = normalizeTaskDescription(input.description))
}

private fun normalizeStep(input: SubtaskDraft): SubtaskDraft {
    validateTaskEstimate(input.estimatedMilliseconds)
    return input.copy(title = normalizeTaskTitle(input.title))
}

private fun TaskReminderDraft.entity(user: Long): ReminderEntity {
    require(zoneId in ZoneId.getAvailableZoneIds()) { "Reminder requires a valid IANA zone" }
    require(Instant.ofEpochMilli(triggerAtMillis).atZone(ZoneOffset.UTC).year in 1..9999) { "Reminder time is outside the supported range" }
    require((message?.length ?: 0) <= 512) { "Reminder message cannot exceed 512 characters" }
    return ReminderEntity(userLocalId = user, reminderTime = triggerAtMillis, zoneId = zoneId, message = message)
}

private fun validateFutureReminder(draft: TaskReminderDraft, now: Instant) {
    require(draft.triggerAtMillis > now.toEpochMilli()) { "Choose a future reminder date and time" }
}

private fun ReminderEntity.sameConfiguration(other: ReminderEntity): Boolean =
    reminderTime == other.reminderTime && zoneId == other.zoneId && message == other.message &&
        frequency == other.frequency && status == other.status

private data class TaskSqlArguments(val categoryMode: Int, val categoryId: Long?, val dateMode: Int, val from: LocalDate?, val through: LocalDate?)
private fun TaskQuery.sqlArguments(): TaskSqlArguments {
    val categoryMode = when (category) { TaskCategoryFilter.All -> 0; TaskCategoryFilter.Uncategorized -> 1; is TaskCategoryFilter.Selected -> 2 }
    val dateMode = when (date) { TaskDateFilter.Any -> 0; TaskDateFilter.Unscheduled -> 1; is TaskDateFilter.Before -> 3; else -> 2 }
    val from = when (val value = date) { is TaskDateFilter.On -> value.date; is TaskDateFilter.Between -> value.from; else -> null }
    val through = when (val value = date) { is TaskDateFilter.On -> value.date; is TaskDateFilter.Between -> value.through; is TaskDateFilter.Before -> value.date; else -> null }
    return TaskSqlArguments(categoryMode, (category as? TaskCategoryFilter.Selected)?.localId, dateMode, from, through)
}
