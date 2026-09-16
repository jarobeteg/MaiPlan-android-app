package com.example.maiplan.network.sync

import androidx.room.withTransaction
import com.example.maiplan.database.MaiPlanDatabase
import com.example.maiplan.database.entities.CategoryEntity
import com.example.maiplan.database.entities.SyncStateEntity
import com.example.maiplan.utils.common.OutboxStatus
import com.google.gson.JsonObject
import java.time.Instant
import java.time.format.DateTimeParseException
import java.util.UUID

data class CategoryReconciliationResult(
    val acknowledgedCount: Int,
    val rejectedCount: Int,
    val conflictCount: Int,
    val appliedChangeCount: Int,
    val deferredChangeCount: Int,
    val nextCursor: String,
    val moreChanges: Boolean
)

class CategoryTideReconciler(private val database: MaiPlanDatabase) {
    private val categoryDao = database.categoryDAO()
    private val outboxDao = database.outboxDAO()
    private val syncStateDao = database.syncStateDAO()
    private val userDao = database.userDAO()

    suspend fun reconcile(
        userLocalId: Long,
        userSyncId: UUID,
        validated: ValidatedCategoryResponse
    ): CategoryReconciliationResult = database.withTransaction {
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
            val updatedRows = categoryDao.updateServerVersion(
                syncId = acknowledgement.entitySyncId,
                userLocalId = userLocalId,
                serverVersion = acknowledgement.serverVersion
            )
            check(updatedRows == 1) {
                "An acknowledged Category mutation has no matching local entity"
            }

            outboxDao.rebasePendingMutations(
                serverVersion = acknowledgement.serverVersion,
                userLocalId = userLocalId,
                entityType = TideEntityType.CATEGORY,
                entitySyncId = acknowledgement.entitySyncId,
                pendingStatus = OutboxStatus.PENDING,
                operations = listOf(TideOperation.UPDATE, TideOperation.DELETE)
            )
        }

        response.rejected.forEach { rejection ->
            val retryable = rejection.errorCode in TideRejectionCode.RETRYABLE
            val changedRows = outboxDao.setMutationOutcome(
                mutationId = rejection.mutationId,
                expectedStatus = OutboxStatus.IN_SYNC,
                status = if (retryable) OutboxStatus.PENDING else OutboxStatus.REJECTED,
                error = rejection.errorCode,
                conflictServerVersion = null,
                conflictServerDataJson = null
            )
            check(changedRows == 1) {
                "Could not record a rejected Category mutation"
            }
        }

        response.conflicts.forEach { conflict ->
            val changedRows = outboxDao.setMutationOutcome(
                mutationId = conflict.mutationId,
                expectedStatus = OutboxStatus.IN_SYNC,
                status = OutboxStatus.CONFLICT,
                error = TideRejectionCode.VERSION_CONFLICT,
                conflictServerVersion = conflict.serverVersion,
                conflictServerDataJson = conflict.serverData?.toString()
            )
            check(changedRows == 1) {
                "Could not record a conflicted Category mutation"
            }
        }

        val acknowledgedIds = response.acknowledged.map { it.mutationId }
        if (acknowledgedIds.isNotEmpty()) {
            val deletedRows = outboxDao.deleteMutations(acknowledgedIds)
            check(deletedRows == acknowledgedIds.size) {
                "Could not finalize every acknowledged Category mutation"
            }
        }

        var appliedChanges = 0
        var deferredChanges = 0

        response.changes.forEach { change ->
            val hasUnresolvedLocalMutation = outboxDao.countMutationsForEntity(
                userLocalId = userLocalId,
                entityType = TideEntityType.CATEGORY,
                entitySyncId = change.entitySyncId,
                statuses = UNRESOLVED_OUTBOX_STATUSES
            ) > 0

            if (hasUnresolvedLocalMutation) {
                deferredChanges += 1
            } else if (applyRemoteChange(userLocalId, change)) {
                appliedChanges += 1
            }
        }

        syncStateDao.upsertSyncState(
            SyncStateEntity(
                userSyncId = userSyncId,
                cursor = response.nextCursor
            )
        )

        CategoryReconciliationResult(
            acknowledgedCount = response.acknowledged.size,
            rejectedCount = response.rejected.size,
            conflictCount = response.conflicts.size,
            appliedChangeCount = appliedChanges,
            deferredChangeCount = deferredChanges,
            nextCursor = response.nextCursor,
            moreChanges = response.moreChanges
        )
    }

    private suspend fun verifyClaimedMutations(
        userLocalId: Long,
        claimedMutationIds: List<UUID>
    ) {
        if (claimedMutationIds.isEmpty()) {
            return
        }

        val claimedRows = outboxDao.getClaimedMutations(
            userLocalId = userLocalId,
            inSyncStatus = OutboxStatus.IN_SYNC,
            mutationIds = claimedMutationIds
        )
        check(claimedRows.map { it.mutationId }.toSet() == claimedMutationIds.toSet()) {
            "The claimed Category mutations changed before reconciliation"
        }
    }

    private suspend fun applyRemoteChange(userLocalId: Long, change: TideChange): Boolean {
        val existing = categoryDao.getCategoryBySyncId(change.entitySyncId, userLocalId)

        if (existing?.serverVersion?.let { it > change.serverVersion } == true) {
            return false
        }

        return when (change.operation) {
            TideOperation.CREATE,
            TideOperation.UPDATE -> {
                val data = checkNotNull(change.data)
                applyRemoteUpsert(userLocalId, change, existing, data)
                true
            }

            TideOperation.DELETE -> {
                if (existing == null) {
                    false
                } else {
                    val tombstone = existing.copy(
                        serverVersion = change.serverVersion,
                        deletedAt = existing.deletedAt ?: Instant.EPOCH
                    )
                    check(categoryDao.updateCategory(tombstone) == 1) {
                        "Could not apply a remote Category deletion"
                    }
                    true
                }
            }

            else -> error("Unsupported Category change operation: ${change.operation}")
        }
    }

    private suspend fun applyRemoteUpsert(
        userLocalId: Long,
        change: TideChange,
        existing: CategoryEntity?,
        data: JsonObject
    ) {
        val name = (data.optionalString("name") ?: existing?.name)
            ?.takeIf { it.isNotBlank() }
            ?: throw TideResponseValidationException(
                "A remote Category change is missing a valid name"
            )
        val description = (data.optionalString("description") ?: existing?.description)
            ?.takeIf { it.isNotBlank() }
            ?: throw TideResponseValidationException(
                "A remote Category change is missing a valid description"
            )
        val color = data.optionalString("color") ?: existing?.color
            ?: throw TideResponseValidationException(
                "A remote Category change is missing its color"
            )
        val icon = data.optionalString("icon") ?: existing?.icon
            ?: throw TideResponseValidationException(
                "A remote Category change is missing its icon"
            )

        val serverCreatedAt = data.optionalInstant("created_at")
        val serverUpdatedAt = data.optionalInstant("updated_at")

        if (existing == null) {
            categoryDao.insertCategory(
                CategoryEntity(
                    userLocalId = userLocalId,
                    name = name,
                    description = description,
                    color = color,
                    icon = icon,
                    syncId = change.entitySyncId,
                    serverVersion = change.serverVersion,
                    createdAt = serverCreatedAt ?: Instant.EPOCH,
                    updatedAt = serverUpdatedAt ?: serverCreatedAt ?: Instant.EPOCH,
                    deletedAt = null
                )
            )
        } else {
            val updated = existing.copy(
                name = name,
                description = description,
                color = color,
                icon = icon,
                serverVersion = change.serverVersion,
                createdAt = serverCreatedAt ?: existing.createdAt,
                updatedAt = serverUpdatedAt ?: existing.updatedAt,
                deletedAt = null
            )
            check(categoryDao.updateCategory(updated) == 1) {
                "Could not apply a remote Category update"
            }
        }
    }

    private fun JsonObject.optionalString(name: String): String? {
        if (!has(name)) {
            return null
        }

        val value = get(name)
        if (value.isJsonNull || !value.isJsonPrimitive || !value.asJsonPrimitive.isString) {
            throw TideResponseValidationException(
                "Category field $name must be a string"
            )
        }
        return value.asString
    }

    private fun JsonObject.optionalInstant(name: String): Instant? {
        val value = optionalString(name) ?: return null
        return try {
            Instant.parse(value)
        } catch (exception: DateTimeParseException) {
            throw TideResponseValidationException(
                "Category field $name must be an ISO-8601 instant",
                exception
            )
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
