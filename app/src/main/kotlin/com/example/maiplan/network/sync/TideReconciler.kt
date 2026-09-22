package com.example.maiplan.network.sync

import androidx.room.withTransaction
import com.example.maiplan.database.MaiPlanDatabase
import com.example.maiplan.database.entities.CategoryEntity
import com.example.maiplan.database.entities.EventEntity
import com.example.maiplan.database.entities.NoteEntity
import com.example.maiplan.database.entities.ReminderEntity
import com.example.maiplan.database.entities.SyncStateEntity
import com.example.maiplan.utils.common.OutboxStatus
import com.google.gson.JsonObject
import java.time.Instant
import java.time.format.DateTimeParseException
import java.util.UUID

data class TideReconciliationResult(
    val acknowledgedCount: Int,
    val rejectedCount: Int,
    val conflictCount: Int,
    val appliedChangeCount: Int,
    val deferredChangeCount: Int,
    val nextCursor: String,
    val moreChanges: Boolean
)

class TideReconciler(private val database: MaiPlanDatabase) {
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
        }

        val acknowledgedIds = response.acknowledged.map { it.mutationId }
        if (acknowledgedIds.isNotEmpty()) {
            check(outboxDao.deleteMutations(acknowledgedIds) == acknowledgedIds.size) {
                "Could not finalize every acknowledged TIDE mutation"
            }
        }

        var appliedChanges = 0
        var deferredChanges = 0
        val orderedChanges = response.changes.sortedBy { ENTITY_APPLY_ORDER.getValue(it.entityType) }

        orderedChanges.forEach { change ->
            val hasUnresolvedLocalMutation = change.entityType != TideEntityType.USER &&
                outboxDao.countMutationsForEntity(
                    userLocalId = userLocalId,
                    entityType = change.entityType,
                    entitySyncId = change.entitySyncId,
                    statuses = UNRESOLVED_OUTBOX_STATUSES
                ) > 0

            if (hasUnresolvedLocalMutation) {
                deferredChanges += 1
            } else if (applyRemoteChange(userLocalId, userSyncId, change)) {
                appliedChanges += 1
            }
        }

        syncStateDao.upsertSyncState(
            SyncStateEntity(userSyncId = userSyncId, cursor = response.nextCursor)
        )

        TideReconciliationResult(
            acknowledgedCount = response.acknowledged.size,
            rejectedCount = response.rejected.size,
            conflictCount = response.conflicts.size,
            appliedChangeCount = appliedChanges,
            deferredChangeCount = deferredChanges,
            nextCursor = response.nextCursor,
            moreChanges = response.moreChanges
        )
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
                    description = data.stringOrExisting("description", existing?.description, true),
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
                    date = data.longOrExisting("date", existing?.date),
                    startTime = data.nullableLongOrExisting("start_time", existing?.startTime),
                    endTime = data.nullableLongOrExisting("end_time", existing?.endTime),
                    zoneId = data.stringOrExisting("zone_id", existing?.zoneId, true),
                    priority = data.intOrExisting("priority", existing?.priority),
                    location = data.nullableStringOrExisting("location", existing?.location),
                    syncId = change.entitySyncId,
                    serverVersion = change.serverVersion,
                    createdAt = data.instantOrExisting("created_at", existing?.createdAt ?: Instant.EPOCH),
                    updatedAt = data.instantOrExisting("updated_at", existing?.updatedAt ?: Instant.EPOCH),
                    deletedAt = null
                )
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
            ?: throw TideResponseValidationException("Referenced Category $syncId is missing")
        if (category.deletedAt != null) {
            throw TideResponseValidationException("Referenced Category $syncId is deleted")
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
            ?: throw TideResponseValidationException("Referenced Reminder $syncId is missing")
        if (reminder.deletedAt != null) {
            throw TideResponseValidationException("Referenced Reminder $syncId is deleted")
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

        val ENTITY_APPLY_ORDER = mapOf(
            TideEntityType.USER to 0,
            TideEntityType.CATEGORY to 1,
            TideEntityType.REMINDER to 2,
            TideEntityType.EVENT to 3,
            TideEntityType.NOTE to 4
        )
    }
}
