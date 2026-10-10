package com.example.maiplan.network.sync

import androidx.room.withTransaction
import com.example.maiplan.database.MaiPlanDatabase
import com.example.maiplan.database.entities.CategoryEntity
import com.example.maiplan.database.entities.EventEntity
import com.example.maiplan.database.entities.NoteEntity
import com.example.maiplan.database.entities.ReminderEntity
import com.example.maiplan.database.entities.SyncStateEntity
import com.example.maiplan.database.entities.SyncInboxEntity
import com.google.gson.JsonParser
import com.example.maiplan.repository.event.eventTimeFromEpochMillis
import com.example.maiplan.repository.event.validateEventDefinition
import com.example.maiplan.utils.common.OutboxStatus
import com.google.gson.JsonObject
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.DateTimeException
import java.time.format.DateTimeParseException
import java.util.UUID

data class TideReconciliationResult(
    val acknowledgedCount: Int,
    val rejectedCount: Int,
    val conflictCount: Int,
    val appliedChangeCount: Int,
    val deferredChangeCount: Int,
    val nextCursor: String,
    val moreChanges: Boolean,
    val hasPendingUploads: Boolean = false,
    val changedTaskIds: Set<Long> = emptySet()
)

class TideReconciler(private val database: MaiPlanDatabase,
    private val taskChangesCommitted: suspend (Set<Long>) -> Unit = {}) {
    private val userDao = database.userDAO()
    private val categoryDao = database.categoryDAO()
    private val reminderDao = database.reminderDAO()
    private val eventDao = database.eventDAO()
    private val noteDao = database.noteDAO()
    private val outboxDao = database.outboxDAO()
    private val syncStateDao = database.syncStateDAO()

    suspend fun reconcile(
        userLocalId: Long,
        userSyncId: UUID,
        validated: ValidatedTideResponse
    ): TideReconciliationResult = database.withTransaction {
        val localUser = checkNotNull(userDao.getUserByLocalId(userLocalId)) {
            "Cannot reconcile TIDE data for a missing local user"
        }
        check(localUser.syncId == userSyncId) {
            "The TIDE user identity does not match the local user"
        }

        val preparedRequest = validated.preparedRequest
        val currentCursor = syncStateDao.getSyncState(userSyncId)?.cursor
        check(currentCursor == preparedRequest.request.cursor) {
            "The TIDE response was prepared from a stale cursor"
        }
        verifyClaimedMutations(userLocalId, preparedRequest.claimedMutationIds)

        val response = validated.response
        response.acknowledged.forEach { acknowledgement ->
            if (acknowledgement.entityType in setOf(TideEntityType.TASK_ACTION, TideEntityType.TASK_SERIES_ACTION)) {
                if (acknowledgement.entityType == TideEntityType.TASK_SERIES_ACTION) {
                    database.taskSeriesDAO().version(userLocalId, acknowledgement.entitySyncId, acknowledgement.serverVersion)
                    acknowledgement.continuation?.let { operation ->
                        val series = checkNotNull(database.taskSeriesDAO().get(userLocalId, acknowledgement.entitySyncId))
                        com.example.maiplan.repository.task.TaskSeriesStore(database).enqueue(series, "RECONCILE",
                            mapOf("operation_id" to operation["id"].asString))
                    }
                }
                for (effect in acknowledgement.effects) {
                    when (effect.entityType) {
                        TideEntityType.TASK -> database.taskDAO().updateServerVersion(effect.entitySyncId, userLocalId, effect.serverVersion)
                        TideEntityType.SUBTASK -> database.subtaskDAO().updateServerVersion(effect.entitySyncId, userLocalId, effect.serverVersion)
                        TideEntityType.TASK_SERIES -> database.taskSeriesDAO().version(userLocalId, effect.entitySyncId, effect.serverVersion)
                    }
                    retain(userLocalId, effect.change())
                }
                return@forEach
            }
            val updatedRows = updateAcknowledgedEntityVersion(
                userLocalId = userLocalId,
                entityType = acknowledgement.entityType,
                entitySyncId = acknowledgement.entitySyncId,
                serverVersion = acknowledgement.serverVersion
            )
            check(updatedRows == 1) {
                "An acknowledged ${acknowledgement.entityType} mutation has no matching local entity"
            }

            outboxDao.rebasePendingMutations(
                serverVersion = acknowledgement.serverVersion,
                userLocalId = userLocalId,
                entityType = acknowledgement.entityType,
                entitySyncId = acknowledgement.entitySyncId,
                pendingStatus = OutboxStatus.PENDING,
                operations = listOf(TideOperation.UPDATE, TideOperation.DELETE)
            )
        }

        response.rejected.forEach { rejection ->
            val retryable = rejection.errorCode in TideRejectionCode.RETRYABLE
            check(
                outboxDao.setMutationOutcome(
                    mutationId = rejection.mutationId,
                    expectedStatus = OutboxStatus.IN_SYNC,
                    status = if (retryable) OutboxStatus.PENDING else OutboxStatus.REJECTED,
                    error = rejection.errorCode,
                    conflictServerVersion = null,
                    conflictServerDataJson = null
                ) == 1
            ) { "Could not record a rejected ${rejection.entityType} mutation" }
            val rejectedRow = outboxDao.getMutation(rejection.mutationId)
            if (!retryable && rejectedRow?.entityType == TideEntityType.TASK_ACTION && rejectedRow.operation == TideOperation.CREATE) {
                for (later in outboxDao.allForUser(userLocalId).filter { it.entityType == TideEntityType.TASK_ACTION &&
                    it.entitySyncId == rejectedRow.entitySyncId && it.outboxLocalId > rejectedRow.outboxLocalId &&
                    it.status == OutboxStatus.PENDING && it.attemptCount == 0 }) {
                    outboxDao.setMutationOutcome(later.mutationId, OutboxStatus.PENDING, OutboxStatus.REJECTED,
                        "TASK_PARENT_CREATE_REJECTED", null, null)
                }
            }
        }

        response.conflicts.forEach { conflict ->
            check(
                outboxDao.setMutationOutcome(
                    mutationId = conflict.mutationId,
                    expectedStatus = OutboxStatus.IN_SYNC,
                    status = OutboxStatus.CONFLICT,
                    error = TideRejectionCode.VERSION_CONFLICT,
                    conflictServerVersion = conflict.serverVersion,
                    conflictServerDataJson = conflict.serverData?.toString()
                ) == 1
            ) { "Could not record a conflicted ${conflict.entityType} mutation" }
            if (conflict.entityType == TideEntityType.TASK_ACTION && conflict.serverData != null) {
                val snapshots = mutableListOf(conflict.serverData.getAsJsonObject("task"))
                snapshots += conflict.serverData.getAsJsonArray("subtasks").map { it.asJsonObject }
                for (snapshot in snapshots) retain(userLocalId, decodeSnapshot(snapshot).change())
            }
            if (conflict.entityType == TideEntityType.TASK_SERIES_ACTION && conflict.serverData != null)
                retain(userLocalId, decodeSnapshot(conflict.serverData.getAsJsonObject("series")).change())
        }

        val acknowledgedIds = response.acknowledged.map { it.mutationId }
        if (acknowledgedIds.isNotEmpty()) {
            check(outboxDao.deleteMutations(acknowledgedIds) == acknowledgedIds.size) {
                "Could not finalize every acknowledged TIDE mutation"
            }
        }

        response.changes.forEach { retain(userLocalId, it) }
        val replay = replayInbox(userLocalId, userSyncId)

        syncStateDao.upsertSyncState(
            SyncStateEntity(userSyncId = userSyncId, cursor = response.nextCursor)
        )

        TideReconciliationResult(
            acknowledgedCount = response.acknowledged.size,
            rejectedCount = response.rejected.size,
            conflictCount = response.conflicts.size,
            appliedChangeCount = replay.first,
            deferredChangeCount = database.syncInboxDAO().pending(userLocalId).size,
            nextCursor = response.nextCursor,
            moreChanges = response.moreChanges,
            hasPendingUploads = outboxDao.getMutations(userLocalId, OutboxStatus.PENDING, TideEntityType.MUTABLE,
                UNRESOLVED_OUTBOX_STATUSES, 1).isNotEmpty(),
            changedTaskIds = replay.second
        )
    }.also { result -> if (result.changedTaskIds.isNotEmpty()) runCatching { taskChangesCommitted(result.changedTaskIds) } }

    internal fun decodeSnapshot(data: JsonObject): TideActionSnapshot = TideActionSnapshot(
        data["entity_type"].asString, UUID.fromString(data["entity_sync_id"].asString), data["operation"].asString,
        data["server_version"].asLong, data["data"]?.takeUnless { it.isJsonNull }?.asJsonObject)

    internal suspend fun retain(user: Long, change: TideChange) {
        val old = database.syncInboxDAO().get(user, change.entityType, change.entitySyncId)
        if (old != null && old.serverVersion >= change.serverVersion) return
        database.syncInboxDAO().put(SyncInboxEntity(user, change.entityType, change.entitySyncId,
            change.serverVersion, change.operation, change.sequence.toLong(), change.data?.toString()))
        if (change.entityType == TideEntityType.TASK_SERIES && change.operation == TideOperation.DELETE) {
            database.taskSeriesDAO().get(user, change.entitySyncId)?.let {
                database.taskSeriesDAO().update(it.copy(deletedAt = it.deletedAt ?: Instant.EPOCH))
            }
        }
        if (change.entityType == TideEntityType.TASK && change.operation == TideOperation.DELETE) {
            val task = database.taskDAO().getTaskBySyncId(change.entitySyncId, user)
            if (task != null) {
                val deletedAt = task.deletedAt ?: Instant.EPOCH
                for (step in database.subtaskDAO().allForTask(task.taskLocalId, user).filter { it.deletedAt == null }) {
                    database.subtaskDAO().updateSubtask(step.copy(deletedAt = deletedAt))
                }
                database.taskDAO().updateTask(task.copy(deletedAt = deletedAt))
                database.scheduledReminderDAO().removeTaskPending("task:${task.taskLocalId}", user)
            }
        }
    }

    suspend fun replayDeferred(user: Long, userSyncId: UUID): Int {
        val replay = database.withTransaction { replayInbox(user, userSyncId) }
        if (replay.second.isNotEmpty()) runCatching { taskChangesCommitted(replay.second) }
        return replay.first
    }

    internal suspend fun replayWithinTransaction(user: Long, userSyncId: UUID) = replayInbox(user, userSyncId)

    private suspend fun replayInbox(user: Long, userSyncId: UUID): Pair<Int, Set<Long>> {
        var applied = 0
        val changedTasks = mutableSetOf<Long>()
        for (row in database.syncInboxDAO().pending(user).filter { it.entityType == TideEntityType.TASK && it.operation == TideOperation.DELETE }) {
            database.taskDAO().getTaskBySyncId(row.entitySyncId, user)?.let { changedTasks += it.taskLocalId }
        }
        do {
            var progress = false
            for (row in database.syncInboxDAO().pending(user)) {
                val data = row.dataJson?.let { JsonParser.parseString(it).asJsonObject }
                val change = TideChange(row.sequence.toString(), row.entityType, row.entitySyncId, row.operation, row.serverVersion, data)
                var blocked = row.entityType != TideEntityType.USER && outboxDao.countMutationsForEntity(user,
                    row.entityType, row.entitySyncId, UNRESOLVED_OUTBOX_STATUSES) > 0
                if (row.entityType == TideEntityType.TASK_SERIES) blocked = blocked || outboxDao.countMutationsForEntity(user,
                    TideEntityType.TASK_SERIES_ACTION, row.entitySyncId, UNRESOLVED_OUTBOX_STATUSES) > 0
                if (row.entityType in setOf(TideEntityType.TASK, TideEntityType.SUBTASK)) {
                    val parentId = if (row.entityType == TideEntityType.TASK) row.entitySyncId else
                        data?.get("parent_task_sync_id")?.asString?.let(UUID::fromString) ?: database.subtaskDAO()
                            .getSubtaskBySyncId(row.entitySyncId, user)?.let { database.taskDAO().getTaskByLocalId(it.taskLocalId, user)?.syncId }
                    if (parentId != null) blocked = blocked || outboxDao.countMutationsForEntity(user,
                        TideEntityType.TASK_ACTION, parentId, UNRESOLVED_OUTBOX_STATUSES) > 0
                }
                if (blocked) continue
                try {
                    if (applyRemoteChange(user, userSyncId, change)) applied++
                    database.syncInboxDAO().put(row.copy(applied = true))
                    if (row.entityType == TideEntityType.TASK) database.taskDAO().getTaskBySyncId(row.entitySyncId, user)?.let { changedTasks += it.taskLocalId }
                    if (row.entityType == TideEntityType.SUBTASK) database.subtaskDAO().getSubtaskBySyncId(row.entitySyncId, user)?.let { changedTasks += it.taskLocalId }
                    if (row.entityType == TideEntityType.REMINDER) {
                        val reminderId = reminderDao.getReminderBySyncId(row.entitySyncId, user)?.reminderLocalId
                        database.taskDAO().allForUser(user).filter { it.reminderLocalId == reminderId && reminderId != null }
                            .forEach { changedTasks += it.taskLocalId }
                    }
                    if (row.entityType in setOf(TideEntityType.TASK_SERIES, TideEntityType.TASK_EXCLUSION)) {
                        val seriesId = if (row.entityType == TideEntityType.TASK_SERIES) row.entitySyncId.toString() else data?.get("series_id")?.asString
                        var after = 0L
                        do {
                            val page = database.taskDAO().seriesPage(user, seriesId.orEmpty(), after)
                            page.forEach { changedTasks += it.taskLocalId }; after = page.lastOrNull()?.taskLocalId ?: after
                        } while (page.size == com.example.maiplan.repository.task.TASK_SERIES_PAGE_SIZE)
                    }
                    progress = true
                } catch (_: TideDependencyPending) {}
            }
        } while (progress)
        for (id in changedTasks) {
            val task = database.taskDAO().getTaskByLocalId(id, user) ?: continue
            com.example.maiplan.utils.notifications.ReminderPlanWriter(database).task(task,
                task.reminderLocalId?.let { reminderDao.getReminderByLocalId(it, user) })
        }
        return applied to changedTasks
    }

    private suspend fun updateAcknowledgedEntityVersion(
        userLocalId: Long,
        entityType: String,
        entitySyncId: UUID,
        serverVersion: Long
    ): Int {
        return when (entityType) {
            TideEntityType.CATEGORY -> categoryDao.updateServerVersion(
                entitySyncId,
                userLocalId,
                serverVersion
            )
            TideEntityType.REMINDER -> reminderDao.updateServerVersion(
                entitySyncId,
                userLocalId,
                serverVersion
            )
            TideEntityType.EVENT -> eventDao.updateServerVersion(
                entitySyncId,
                userLocalId,
                serverVersion
            )
            TideEntityType.NOTE -> noteDao.updateServerVersion(
                entitySyncId,
                userLocalId,
                serverVersion
            )
            else -> throw TideResponseValidationException(
                "Unsupported acknowledged entity type: $entityType"
            )
        }
    }

    private suspend fun verifyClaimedMutations(
        userLocalId: Long,
        claimedMutationIds: List<UUID>
    ) {
        if (claimedMutationIds.isEmpty()) return
        val claimedRows = outboxDao.getClaimedMutations(
            userLocalId = userLocalId,
            inSyncStatus = OutboxStatus.IN_SYNC,
            mutationIds = claimedMutationIds
        )
        check(claimedRows.map { it.mutationId }.toSet() == claimedMutationIds.toSet()) {
            "The claimed TIDE mutations changed before reconciliation"
        }
    }

    private suspend fun applyRemoteChange(
        userLocalId: Long,
        userSyncId: UUID,
        change: TideChange
    ): Boolean {
        return when (change.entityType) {
            TideEntityType.USER -> applyUserChange(userLocalId, userSyncId, change)
            TideEntityType.CATEGORY -> applyCategoryChange(userLocalId, change)
            TideEntityType.REMINDER -> applyReminderChange(userLocalId, change)
            TideEntityType.EVENT -> applyEventChange(userLocalId, change)
            TideEntityType.NOTE -> applyNoteChange(userLocalId, change)
            TideEntityType.TASK, TideEntityType.SUBTASK -> TaskRemoteStore(database).apply(userLocalId, change)
            TideEntityType.TASK_SERIES, TideEntityType.TASK_EXCLUSION -> TaskSeriesTransport(database).apply(userLocalId, change)
            else -> throw TideResponseValidationException(
                "Unsupported remote entity type: ${change.entityType}"
            )
        }
    }

    private suspend fun applyUserChange(
        userLocalId: Long,
        userSyncId: UUID,
        change: TideChange
    ): Boolean {
        if (change.entitySyncId != userSyncId) {
            throw TideResponseValidationException("A TIDE response referenced another user")
        }
        val existing = checkNotNull(userDao.getUserByLocalId(userLocalId)) {
            "The active TIDE user disappeared"
        }
        if (existing.serverVersion?.let { it > change.serverVersion } == true) return false

        val updated = when (change.operation) {
            TideOperation.CREATE,
            TideOperation.UPDATE -> {
                val data = checkNotNull(change.data)
                existing.copy(
                    email = data.stringOrExisting("email", existing.email, requireNonBlank = true),
                    username = data.stringOrExisting(
                        "username",
                        existing.username,
                        requireNonBlank = true
                    ),
                    serverVersion = change.serverVersion,
                    createdAt = data.instantOrExisting("created_at", existing.createdAt),
                    updatedAt = data.instantOrExisting("updated_at", existing.updatedAt),
                    deletedAt = null
                )
            }
            TideOperation.DELETE -> existing.copy(
                serverVersion = change.serverVersion,
                deletedAt = existing.deletedAt ?: Instant.EPOCH
            )
            else -> error("Unsupported User operation: ${change.operation}")
        }
        check(userDao.updateUser(updated) == 1) { "Could not apply a remote User change" }
        return true
    }

    private suspend fun applyCategoryChange(userLocalId: Long, change: TideChange): Boolean {
        val existing = categoryDao.getCategoryBySyncId(change.entitySyncId, userLocalId)
        if (existing?.serverVersion?.let { it > change.serverVersion } == true) return false

        return when (change.operation) {
            TideOperation.CREATE,
            TideOperation.UPDATE -> {
                val data = checkNotNull(change.data)
                val entity = CategoryEntity(
                    categoryLocalId = existing?.categoryLocalId ?: 0L,
                    userLocalId = userLocalId,
                    name = data.stringOrExisting("name", existing?.name, true),
                    description = data.nullableStringOrExisting("description", existing?.description).orEmpty(),
                    color = data.stringOrExisting("color", existing?.color, true),
                    icon = data.stringOrExisting("icon", existing?.icon, true),
                    syncId = change.entitySyncId,
                    serverVersion = change.serverVersion,
                    createdAt = data.instantOrExisting("created_at", existing?.createdAt ?: Instant.EPOCH),
                    updatedAt = data.instantOrExisting("updated_at", existing?.updatedAt ?: Instant.EPOCH),
                    deletedAt = null
                )
                if (existing == null) categoryDao.insertCategory(entity)
                else check(categoryDao.updateCategory(entity) == 1) {
                    "Could not apply a remote Category update"
                }
                true
            }
            TideOperation.DELETE -> tombstoneCategory(existing, change.serverVersion)
            else -> error("Unsupported Category operation: ${change.operation}")
        }
    }

    private suspend fun tombstoneCategory(
        existing: CategoryEntity?,
        serverVersion: Long
    ): Boolean {
        if (existing == null) return false
        check(
            categoryDao.updateCategory(
                existing.copy(
                    serverVersion = serverVersion,
                    deletedAt = existing.deletedAt ?: Instant.EPOCH
                )
            ) == 1
        ) { "Could not apply a remote Category deletion" }
        return true
    }

    private suspend fun applyReminderChange(userLocalId: Long, change: TideChange): Boolean {
        val existing = reminderDao.getReminderBySyncId(change.entitySyncId, userLocalId)
        if (existing?.serverVersion?.let { it > change.serverVersion } == true) return false

        return when (change.operation) {
            TideOperation.CREATE,
            TideOperation.UPDATE -> {
                val data = checkNotNull(change.data)
                val entity = ReminderEntity(
                    reminderLocalId = existing?.reminderLocalId ?: 0L,
                    userLocalId = userLocalId,
                    reminderTime = data.longOrExisting("reminder_time", existing?.reminderTime),
                    zoneId = data.stringOrExisting("zone_id", existing?.zoneId, true),
                    frequency = data.intOrExisting("frequency", existing?.frequency),
                    status = data.intOrExisting("status", existing?.status),
                    message = data.nullableStringOrExisting("message", existing?.message),
                    syncId = change.entitySyncId,
                    serverVersion = change.serverVersion,
                    createdAt = data.instantOrExisting("created_at", existing?.createdAt ?: Instant.EPOCH),
                    updatedAt = data.instantOrExisting("updated_at", existing?.updatedAt ?: Instant.EPOCH),
                    deletedAt = null
                )
                if (existing == null) reminderDao.insertReminder(entity)
                else check(reminderDao.updateReminder(entity) == 1) {
                    "Could not apply a remote Reminder update"
                }
                true
            }
            TideOperation.DELETE -> {
                if (existing == null) false else {
                    check(
                        reminderDao.updateReminder(
                            existing.copy(
                                serverVersion = change.serverVersion,
                                deletedAt = existing.deletedAt ?: Instant.EPOCH
                            )
                        ) == 1
                    ) { "Could not apply a remote Reminder deletion" }
                    true
                }
            }
            else -> error("Unsupported Reminder operation: ${change.operation}")
        }
    }

    private suspend fun applyEventChange(userLocalId: Long, change: TideChange): Boolean {
        val existing = eventDao.getEventBySyncId(change.entitySyncId, userLocalId)
        if (existing?.serverVersion?.let { it > change.serverVersion } == true) return false

        return when (change.operation) {
            TideOperation.CREATE,
            TideOperation.UPDATE -> {
                val data = checkNotNull(change.data)
                val startDate = data.eventDateOrExisting("start_date", existing?.startDate)
                val endDate = data.eventDateOrExisting("end_date", existing?.endDate)
                val zoneId = data.stringOrExisting("zone_id", existing?.zoneId, true)
                val entity = EventEntity(
                    eventLocalId = existing?.eventLocalId ?: 0L,
                    userLocalId = userLocalId,
                    categoryLocalId = resolveCategoryRelationship(
                        data,
                        "category_sync_id",
                        existing?.categoryLocalId,
                        userLocalId
                    ),
                    reminderLocalId = resolveReminderRelationship(
                        data,
                        "reminder_sync_id",
                        existing?.reminderLocalId,
                        userLocalId
                    ),
                    title = data.stringOrExisting("title", existing?.title, true),
                    description = data.nullableStringOrExisting("description", existing?.description),
                    startDate = startDate,
                    endDate = endDate,
                    startTime = data.eventTimeOrExisting(
                        "start_time", existing?.startTime, startDate, zoneId
                    ),
                    endTime = data.eventTimeOrExisting(
                        "end_time", existing?.endTime, endDate, zoneId
                    ),
                    recurrenceFrequency = data.nullableStringOrExisting(
                        "recurrence_frequency", existing?.recurrenceFrequency
                    ),
                    recurrenceInterval = data.nullableIntOrExisting(
                        "recurrence_interval", existing?.recurrenceInterval
                    ),
                    recurrenceWeekdays = data.nullableIntOrExisting(
                        "recurrence_weekdays", existing?.recurrenceWeekdays
                    ),
                    recurrenceMonthlyMode = data.nullableStringOrExisting(
                        "recurrence_monthly_mode", existing?.recurrenceMonthlyMode
                    ),
                    recurrenceUntilDate = data.nullableEventDateOrExisting(
                        "recurrence_until_date", existing?.recurrenceUntilDate
                    ),
                    reminderOffsetMinutes = data.nullableIntOrExisting(
                        "reminder_offset_minutes", existing?.reminderOffsetMinutes
                    ),
                    reminderLeadDays = data.nullableIntOrExisting(
                        "reminder_lead_days", existing?.reminderLeadDays
                    ),
                    reminderMinuteOfDay = data.nullableIntOrExisting(
                        "reminder_minute_of_day", existing?.reminderMinuteOfDay
                    ),
                    relativeReminderMessage = data.nullableStringOrExisting(
                        "relative_reminder_message", existing?.relativeReminderMessage
                    ),
                    zoneId = zoneId,
                    priority = data.intOrExisting("priority", existing?.priority),
                    location = data.nullableStringOrExisting("location", existing?.location),
                    syncId = change.entitySyncId,
                    serverVersion = change.serverVersion,
                    createdAt = data.instantOrExisting("created_at", existing?.createdAt ?: Instant.EPOCH),
                    updatedAt = data.instantOrExisting("updated_at", existing?.updatedAt ?: Instant.EPOCH),
                    deletedAt = null
                )
                try {
                    validateEventDefinition(entity)
                } catch (error: IllegalArgumentException) {
                    throw TideResponseValidationException("Invalid event definition", error)
                } catch (error: DateTimeException) {
                    throw TideResponseValidationException("Invalid event time zone", error)
                }
                if (existing == null) eventDao.insertEvent(entity)
                else check(eventDao.updateEvent(entity) == 1) {
                    "Could not apply a remote Event update"
                }
                true
            }
            TideOperation.DELETE -> {
                if (existing == null) false else {
                    check(
                        eventDao.updateEvent(
                            existing.copy(
                                serverVersion = change.serverVersion,
                                deletedAt = existing.deletedAt ?: Instant.EPOCH
                            )
                        ) == 1
                    ) { "Could not apply a remote Event deletion" }
                    true
                }
            }
            else -> error("Unsupported Event operation: ${change.operation}")
        }
    }

    private suspend fun applyNoteChange(userLocalId: Long, change: TideChange): Boolean {
        val existing = noteDao.getNoteBySyncId(change.entitySyncId, userLocalId)
        if (existing?.serverVersion?.let { it > change.serverVersion } == true) return false

        return when (change.operation) {
            TideOperation.CREATE,
            TideOperation.UPDATE -> {
                val data = checkNotNull(change.data)
                val entity = NoteEntity(
                    noteLocalId = existing?.noteLocalId ?: 0L,
                    userLocalId = userLocalId,
                    categoryLocalId = resolveCategoryRelationship(
                        data,
                        "category_sync_id",
                        existing?.categoryLocalId,
                        userLocalId
                    ),
                    reminderLocalId = resolveReminderRelationship(
                        data,
                        "reminder_sync_id",
                        existing?.reminderLocalId,
                        userLocalId
                    ),
                    title = data.stringOrExisting("title", existing?.title, true),
                    content = data.nullableStringOrExisting("content", existing?.content),
                    isPinned = data.booleanOrExisting("is_pinned", existing?.isPinned),
                    syncId = change.entitySyncId,
                    serverVersion = change.serverVersion,
                    createdAt = data.instantOrExisting("created_at", existing?.createdAt ?: Instant.EPOCH),
                    updatedAt = data.instantOrExisting("updated_at", existing?.updatedAt ?: Instant.EPOCH),
                    deletedAt = null
                )
                if (existing == null) noteDao.insertNote(entity)
                else check(noteDao.updateNote(entity) == 1) {
                    "Could not apply a remote Note update"
                }
                true
            }
            TideOperation.DELETE -> {
                if (existing == null) false else {
                    check(
                        noteDao.updateNote(
                            existing.copy(
                                serverVersion = change.serverVersion,
                                deletedAt = existing.deletedAt ?: Instant.EPOCH
                            )
                        ) == 1
                    ) { "Could not apply a remote Note deletion" }
                    true
                }
            }
            else -> error("Unsupported Note operation: ${change.operation}")
        }
    }

    private suspend fun resolveCategoryRelationship(
        data: JsonObject,
        field: String,
        existingLocalId: Long?,
        userLocalId: Long
    ): Long? {
        if (!data.has(field)) return existingLocalId
        val syncId = data.nullableUuid(field) ?: return null
        val category = categoryDao.getCategoryBySyncId(syncId, userLocalId)
            ?: return if (database.syncInboxDAO().get(userLocalId, TideEntityType.CATEGORY, syncId)?.operation == TideOperation.DELETE) null else throw TideDependencyPending()
        if (category.deletedAt != null) {
            return null
        }
        return category.categoryLocalId
    }

    private suspend fun resolveReminderRelationship(
        data: JsonObject,
        field: String,
        existingLocalId: Long?,
        userLocalId: Long
    ): Long? {
        if (!data.has(field)) return existingLocalId
        val syncId = data.nullableUuid(field) ?: return null
        val reminder = reminderDao.getReminderBySyncId(syncId, userLocalId)
            ?: return if (database.syncInboxDAO().get(userLocalId, TideEntityType.REMINDER, syncId)?.operation == TideOperation.DELETE) null else throw TideDependencyPending()
        if (reminder.deletedAt != null) {
            return null
        }
        return reminder.reminderLocalId
    }

    private fun JsonObject.stringOrExisting(
        name: String,
        existing: String?,
        requireNonBlank: Boolean
    ): String {
        val value = if (has(name)) requiredString(name) else existing
        val resolved = value ?: throw TideResponseValidationException("Missing field $name")
        if (requireNonBlank && resolved.isBlank()) {
            throw TideResponseValidationException("Field $name must not be blank")
        }
        return resolved
    }

    private fun JsonObject.nullableStringOrExisting(name: String, existing: String?): String? {
        if (!has(name)) return existing
        val value = get(name)
        if (value.isJsonNull) return null
        if (!value.isJsonPrimitive || !value.asJsonPrimitive.isString) {
            throw TideResponseValidationException("Field $name must be a string or null")
        }
        return value.asString
    }

    private fun JsonObject.requiredString(name: String): String {
        val value = get(name)
        if (value == null || value.isJsonNull || !value.isJsonPrimitive ||
            !value.asJsonPrimitive.isString
        ) {
            throw TideResponseValidationException("Field $name must be a string")
        }
        return value.asString
    }

    private fun JsonObject.longOrExisting(name: String, existing: Long?): Long {
        if (!has(name)) {
            return existing ?: throw TideResponseValidationException("Missing field $name")
        }
        return requiredLong(name)
    }

    private fun JsonObject.nullableLongOrExisting(name: String, existing: Long?): Long? {
        if (!has(name)) return existing
        val value = get(name)
        if (value.isJsonNull) return null
        return requiredLong(name)
    }

    private fun JsonObject.requiredLong(name: String): Long {
        val value = get(name)
        if (value == null || value.isJsonNull || !value.isJsonPrimitive ||
            !value.asJsonPrimitive.isNumber
        ) {
            throw TideResponseValidationException("Field $name must be an integer")
        }
        return runCatching { value.asBigDecimal.longValueExact() }.getOrElse {
            throw TideResponseValidationException("Field $name must be an integer", it)
        }
    }

    private fun JsonObject.intOrExisting(name: String, existing: Int?): Int {
        val value = longOrExisting(name, existing?.toLong())
        if (value !in Int.MIN_VALUE..Int.MAX_VALUE) {
            throw TideResponseValidationException("Field $name is outside the integer range")
        }
        return value.toInt()
    }

    private fun JsonObject.nullableIntOrExisting(name: String, existing: Int?): Int? {
        if (!has(name)) return existing
        if (get(name).isJsonNull) return null
        val value = requiredLong(name)
        if (value !in Int.MIN_VALUE..Int.MAX_VALUE) {
            throw TideResponseValidationException("Field $name is outside the integer range")
        }
        return value.toInt()
    }

    private fun eventDate(name: String, epochDay: Long): LocalDate {
        val min = LocalDate.of(1, 1, 1).toEpochDay()
        val max = LocalDate.of(9999, 12, 31).toEpochDay()
        if (epochDay !in min..max) {
            throw TideResponseValidationException("Field $name is outside the date range")
        }
        return try {
            LocalDate.ofEpochDay(epochDay)
        } catch (error: DateTimeException) {
            throw TideResponseValidationException("Field $name is outside the date range", error)
        }
    }

    private fun JsonObject.eventDateOrExisting(name: String, existing: LocalDate?): LocalDate =
        eventDate(name, longOrExisting(name, existing?.toEpochDay()))

    private fun JsonObject.nullableEventDateOrExisting(
        name: String, existing: LocalDate?
    ): LocalDate? = nullableLongOrExisting(name, existing?.toEpochDay())?.let {
        eventDate(name, it)
    }

    private fun JsonObject.eventTimeOrExisting(
        name: String, existing: LocalTime?, date: LocalDate, zoneId: String
    ): LocalTime? {
        if (!has(name)) return existing
        val millis = nullableLongOrExisting(name, null) ?: return null
        return try {
            eventTimeFromEpochMillis(millis, date, zoneId)
        } catch (error: RuntimeException) {
            throw TideResponseValidationException("Invalid $name timestamp", error)
        }
    }

    private fun JsonObject.booleanOrExisting(name: String, existing: Boolean?): Boolean {
        if (!has(name)) {
            return existing ?: throw TideResponseValidationException("Missing field $name")
        }
        val value = get(name)
        if (value.isJsonNull || !value.isJsonPrimitive || !value.asJsonPrimitive.isBoolean) {
            throw TideResponseValidationException("Field $name must be a boolean")
        }
        return value.asBoolean
    }

    private fun JsonObject.nullableUuid(name: String): UUID? {
        val value = get(name)
        if (value == null || value.isJsonNull) return null
        if (!value.isJsonPrimitive || !value.asJsonPrimitive.isString) {
            throw TideResponseValidationException("Field $name must be a UUID string or null")
        }
        return runCatching { UUID.fromString(value.asString) }.getOrElse {
            throw TideResponseValidationException("Field $name must be a UUID string", it)
        }
    }

    private fun JsonObject.instantOrExisting(name: String, existing: Instant): Instant {
        if (!has(name)) return existing
        val text = requiredString(name)
        return try {
            Instant.parse(text)
        } catch (exception: DateTimeParseException) {
            throw TideResponseValidationException("Field $name must be an ISO-8601 instant", exception)
        }
    }

    private companion object {
        val UNRESOLVED_OUTBOX_STATUSES = listOf(
            OutboxStatus.PENDING,
            OutboxStatus.IN_SYNC,
            OutboxStatus.CONFLICT,
            OutboxStatus.REJECTED
        )

    }
}
