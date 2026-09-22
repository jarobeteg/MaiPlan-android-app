package com.example.maiplan.network.sync

import androidx.room.withTransaction
import com.example.maiplan.database.MaiPlanDatabase
import com.example.maiplan.database.entities.OutboxEntity
import com.example.maiplan.utils.DeviceIdentityStore
import com.example.maiplan.utils.common.OutboxStatus
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.time.Instant
import java.util.UUID


data class PreparedTideRequest(
    val request: TideSyncRequest,
    val claimedMutationIds: List<UUID>
)

class TideRequestPreparer(
    private val database: MaiPlanDatabase,
    private val deviceIdentityStore: DeviceIdentityStore
) {
    private val outboxDao = database.outboxDAO()
    private val syncStateDao = database.syncStateDAO()

    suspend fun prepareRequest(
        userLocalId: Long,
        userSyncId: UUID,
        mutationLimit: Int = TideClientConfig.UPLOAD_BATCH_SIZE,
        dataLimit: Int = TideProtocol.DEFAULT_DATA_LIMIT
    ): PreparedTideRequest {
        require(mutationLimit > 0) {
            "Mutation limit must be positive"
        }

        require(dataLimit > 0) {
            "Data limit must be positive"
        }

        val requestId = UUID.randomUUID()
        val deviceId = deviceIdentityStore.getOrCreateDeviceId()
        val attemptedAt = Instant.now()

        val localBatch = database.withTransaction {
            outboxDao.recoverStaleMutations(
                userLocalId = userLocalId,
                entityTypes = TideEntityType.MUTABLE,
                staleBefore = attemptedAt.minusSeconds(TideClientConfig.STALE_CLAIM_SECONDS),
                pendingStatus = OutboxStatus.PENDING,
                inSyncStatus = OutboxStatus.IN_SYNC,
                recoveryMessage = "Recovered Stale TIDE claim"
            )

            val pending = outboxDao.getMutations(
                userLocalId = userLocalId,
                status = OutboxStatus.PENDING,
                entityTypes = TideEntityType.MUTABLE,
                blockingStatuses = listOf(
                    OutboxStatus.PENDING,
                    OutboxStatus.IN_SYNC,
                    OutboxStatus.CONFLICT,
                    OutboxStatus.REJECTED
                ),
                limit = mutationLimit
            )

            val mutations = pending.map { it.toTideMutation() }
            val mutationIds = pending.map { it.mutationId }

            if (mutationIds.isNotEmpty()) {
                val claimedCount = outboxDao.markInSync(
                    mutationIds = mutationIds,
                    attemptedAt = attemptedAt,
                    pendingStatus = OutboxStatus.PENDING,
                    inSyncStatus = OutboxStatus.IN_SYNC
                )

                check(claimedCount == mutationIds.size) {
                    "Could not atomically claim the selected mutations"
                }
            }

            LocalTideBatch(
                cursor = syncStateDao.getSyncState(userSyncId)?.cursor,
                mutations = mutations,
                mutationIds = mutationIds
            )
        }

        return PreparedTideRequest(
            request = TideSyncRequest(
                requestId = requestId,
                deviceId = deviceId,
                cursor = localBatch.cursor,
                dataLimit = dataLimit,
                mutations = localBatch.mutations
            ),
            claimedMutationIds = localBatch.mutationIds
        )
    }

    suspend fun releaseClaim(preparedRequest: PreparedTideRequest, diagnostic: String) {
        val mutationIds = preparedRequest.claimedMutationIds

        if (mutationIds.isEmpty()) {
            return
        }

        val releasedCount = outboxDao.returnToPending(
            mutationIds = mutationIds,
            error = diagnostic,
            pendingStatus = OutboxStatus.PENDING,
            inSyncStatus = OutboxStatus.IN_SYNC
        )

        check(releasedCount == mutationIds.size) {
            "Could not release the complete TIDE mutation claim"
        }
    }

    private fun OutboxEntity.toTideMutation(): TideMutation {
        check(entityType in TideEntityType.MUTABLE) {
            "Unsupported TIDE entity type: $entityType"
        }

        val payload = payloadJson?.toJsonObject()

        when (operation) {
            TideOperation.CREATE,
            TideOperation.UPDATE -> {
                check(payload != null) {
                    "$operation $entityType mutation requires data"
                }
            }

            TideOperation.DELETE -> {
                check(payload == null) {
                    "DELETE $entityType mutation must not contain data"
                }
            }

            else -> error("Unsupported TIDE operation: $operation")
        }

        return TideMutation(
            mutationId = mutationId,
            entityType = entityType,
            entitySyncId = entitySyncId,
            operation = operation,
            baseVersion = baseVersion,
            data = payload
        )
    }

    private fun String.toJsonObject(): JsonObject {
        val element = JsonParser.parseString(this)

        check(element.isJsonObject) {
            "TIDE mutation payload must be a JSON object"
        }

        return element.asJsonObject
    }

    private data class LocalTideBatch(
        val cursor: String?,
        val mutations: List<TideMutation>,
        val mutationIds: List<UUID>
    )
}
