package com.example.maiplan.network.sync

import com.example.maiplan.network.TideExchangeClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.UUID

data class TideSyncResult(
    val pageCount: Int,
    val uploadedMutationCount: Int,
    val acknowledgedMutationCount: Int,
    val rejectedMutationCount: Int,
    val conflictMutationCount: Int,
    val appliedChangeCount: Int,
    val deferredChangeCount: Int,
    val hasMoreWork: Boolean
)

class TideSynchronizer(
    private val requestPreparer: TideRequestPreparer,
    private val exchangeClient: TideExchangeClient,
    private val responseValidator: TideResponseValidator,
    private val reconciler: TideReconciler
) {
    suspend fun sync(
        userLocalId: Long,
        userSyncId: UUID,
        maxPages: Int = TideClientConfig.MAX_PAGES_PER_RUN
    ): TideSyncResult = syncMutex.withLock {
        require(maxPages > 0) { "Maximum TIDE page count must be positive" }

        var pageCount = 0
        var uploadedCount = 0
        var acknowledgedCount = 0
        var rejectedCount = 0
        var conflictCount = 0
        var appliedChangeCount = 0
        var deferredChangeCount = 0
        var hasMoreWork: Boolean

        do {
            val preparedRequest = requestPreparer.prepareRequest(
                userLocalId = userLocalId,
                userSyncId = userSyncId
            )
            val exchange = exchangeClient.exchange(preparedRequest)

            val reconciliation = try {
                val validated = responseValidator.validate(
                    preparedRequest = exchange.preparedRequest,
                    response = exchange.response
                )
                reconciler.reconcile(
                    userLocalId = userLocalId,
                    userSyncId = userSyncId,
                    validated = validated
                )
            } catch (exception: CancellationException) {
                releaseAfterProcessingFailure(preparedRequest, "CANCELLED", exception)
                throw exception
            } catch (exception: TideResponseValidationException) {
                releaseAfterProcessingFailure(preparedRequest, "INVALID_RESPONSE", exception)
                throw exception
            } catch (exception: Exception) {
                releaseAfterProcessingFailure(preparedRequest, "RECONCILIATION_ERROR", exception)
                throw exception
            }

            pageCount += 1
            uploadedCount += preparedRequest.claimedMutationIds.size
            acknowledgedCount += reconciliation.acknowledgedCount
            rejectedCount += reconciliation.rejectedCount
            conflictCount += reconciliation.conflictCount
            appliedChangeCount += reconciliation.appliedChangeCount
            deferredChangeCount += reconciliation.deferredChangeCount

            val retryableRejection = exchange.response.rejected.any {
                it.errorCode in TideRejectionCode.RETRYABLE
            }
            hasMoreWork = reconciliation.moreChanges ||
                preparedRequest.claimedMutationIds.size >= TideClientConfig.UPLOAD_BATCH_SIZE ||
                retryableRejection
        } while (hasMoreWork && pageCount < maxPages)

        TideSyncResult(
            pageCount = pageCount,
            uploadedMutationCount = uploadedCount,
            acknowledgedMutationCount = acknowledgedCount,
            rejectedMutationCount = rejectedCount,
            conflictMutationCount = conflictCount,
            appliedChangeCount = appliedChangeCount,
            deferredChangeCount = deferredChangeCount,
            hasMoreWork = hasMoreWork
        )
    }

    private suspend fun releaseAfterProcessingFailure(
        preparedRequest: PreparedTideRequest,
        diagnostic: String,
        originalFailure: Throwable
    ) {
        try {
            withContext(NonCancellable) {
                requestPreparer.releaseClaim(preparedRequest, diagnostic)
            }
        } catch (cleanupFailure: Exception) {
            originalFailure.addSuppressed(cleanupFailure)
        }
    }

    private companion object {
        val syncMutex = Mutex()
    }
}
