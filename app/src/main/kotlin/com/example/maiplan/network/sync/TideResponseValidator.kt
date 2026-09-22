package com.example.maiplan.network.sync

import java.util.UUID

class TideResponseValidationException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

data class ValidatedTideResponse(
    val preparedRequest: PreparedTideRequest,
    val response: TideSyncResponse,
    val submittedMutationsById: Map<UUID, TideMutation>
)

class TideResponseValidator {
    fun validate(
        preparedRequest: PreparedTideRequest,
        response: TideSyncResponse
    ): ValidatedTideResponse {
        return try {
            validateInternal(preparedRequest, response)
        } catch (exception: TideResponseValidationException) {
            throw exception
        } catch (exception: Exception) {
            throw TideResponseValidationException("The TIDE response is malformed", exception)
        }
    }

    private fun validateInternal(
        preparedRequest: PreparedTideRequest,
        response: TideSyncResponse
    ): ValidatedTideResponse {
        val request = preparedRequest.request

        ensure(
            request.tideProtocolVersion == TideProtocol.VERSION,
            "The request uses an unsupported TIDE protocol version"
        )
        ensure(
            response.tideProtocolVersion == TideProtocol.VERSION,
            "The response uses an unsupported TIDE protocol version"
        )
        ensure(
            response.requestId == request.requestId,
            "The response request ID does not match the request"
        )
        ensure(request.dataLimit > 0, "The request data limit must be positive")

        val requestMutationIds = request.mutations.map { it.mutationId }
        ensureUnique(requestMutationIds, "Request mutation IDs")
        ensureUnique(preparedRequest.claimedMutationIds, "Claimed mutation IDs")
        ensure(
            requestMutationIds == preparedRequest.claimedMutationIds,
            "Request mutations do not exactly match the claimed outbox mutations"
        )

        request.mutations.forEach(::validateSubmittedMutation)
        val submittedMutations = request.mutations.associateBy { it.mutationId }
        val outcomeIds =
            response.acknowledged.map { it.mutationId } +
                response.rejected.map { it.mutationId } +
                response.conflicts.map { it.mutationId }

        ensureUnique(outcomeIds, "Response mutation outcome IDs")
        ensure(
            outcomeIds.toSet() == submittedMutations.keys,
            "Every submitted mutation must have exactly one response outcome"
        )

        response.acknowledged.forEach { acknowledgement ->
            validateOutcomeIdentity(
                acknowledgement.mutationId,
                acknowledgement.entityType,
                acknowledgement.entitySyncId,
                submittedMutations
            )
            ensure(
                acknowledgement.serverVersion > 0,
                "An acknowledged mutation has an invalid server version"
            )
        }

        response.rejected.forEach { rejection ->
            validateOutcomeIdentity(
                rejection.mutationId,
                rejection.entityType,
                rejection.entitySyncId,
                submittedMutations
            )
            ensure(rejection.errorCode.isNotBlank(), "A rejected mutation is missing its error code")
        }

        response.conflicts.forEach { conflict ->
            validateOutcomeIdentity(
                conflict.mutationId,
                conflict.entityType,
                conflict.entitySyncId,
                submittedMutations
            )
            ensure(
                conflict.serverVersion > 0,
                "A conflicted mutation has an invalid server version"
            )
        }

        ensure(
            response.changes.size <= request.dataLimit,
            "The response exceeds the requested data limit"
        )
        ensureUnique(response.changes.map { it.sequence }, "Response change sequences")
        response.changes.forEach(::validateChange)

        if (response.moreChanges) {
            ensure(
                response.changes.isNotEmpty(),
                "more_changes cannot be true when no changes were returned"
            )
        }

        if (response.changes.isNotEmpty()) {
            ensure(
                response.nextCursor == response.changes.last().sequence,
                "The next cursor does not match the final returned change"
            )
            ensure(
                response.nextCursor != request.cursor,
                "A non-empty response page did not advance the cursor"
            )
        }

        return ValidatedTideResponse(
            preparedRequest = preparedRequest,
            response = response,
            submittedMutationsById = submittedMutations
        )
    }

    private fun validateSubmittedMutation(mutation: TideMutation) {
        ensure(
            mutation.entityType in TideEntityType.MUTABLE,
            "The TIDE request contains an unsupported entity type"
        )
        validateOperationAndData(mutation.operation, mutation.data != null, "mutation")
    }

    private fun validateChange(change: TideChange) {
        ensure(
            change.entityType in TideEntityType.SUPPORTED,
            "The TIDE response contains an unsupported entity type"
        )
        ensure(change.serverVersion > 0, "A remote change has an invalid server version")
        validateOperationAndData(change.operation, change.data != null, "change")
    }

    private fun validateOperationAndData(
        operation: String,
        hasData: Boolean,
        description: String
    ) {
        when (operation) {
            TideOperation.CREATE,
            TideOperation.UPDATE -> ensure(hasData, "$operation requires $description data")
            TideOperation.DELETE -> ensure(!hasData, "DELETE must not contain $description data")
            else -> throw TideResponseValidationException(
                "The TIDE payload contains an unsupported operation"
            )
        }
    }

    private fun validateOutcomeIdentity(
        mutationId: UUID,
        entityType: String,
        entitySyncId: UUID,
        submittedMutations: Map<UUID, TideMutation>
    ) {
        val submittedMutation = submittedMutations[mutationId]
            ?: throw TideResponseValidationException(
                "The response contains an outcome for an unknown mutation"
            )
        ensure(
            entityType == submittedMutation.entityType,
            "The response outcome entity type does not match the submitted mutation"
        )
        ensure(
            entitySyncId == submittedMutation.entitySyncId,
            "The response outcome entity sync ID does not match the submitted mutation"
        )
    }

    private fun <T> ensureUnique(values: List<T>, description: String) {
        ensure(values.size == values.toSet().size, "$description must be unique")
    }

    private fun ensure(condition: Boolean, message: String) {
        if (!condition) throw TideResponseValidationException(message)
    }
}
