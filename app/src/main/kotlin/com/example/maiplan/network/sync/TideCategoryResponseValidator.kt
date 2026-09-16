package com.example.maiplan.network.sync

import java.util.UUID

class TideResponseValidationException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

data class ValidatedCategoryResponse(
    val preparedRequest: PreparedTideRequest,
    val response: TideSyncResponse,
    val submittedMutationsById: Map<UUID, TideMutation>
)

class TideCategoryResponseValidator {
    fun validate(
        preparedRequest: PreparedTideRequest,
        response: TideSyncResponse
    ): ValidatedCategoryResponse {
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
                mutationId = acknowledgement.mutationId,
                entityType = acknowledgement.entityType,
                entitySyncId = acknowledgement.entitySyncId,
                submittedMutations = submittedMutations
            )
            ensure(
                acknowledgement.serverVersion > 0,
                "An acknowledged mutation has an invalid server version"
            )
        }

        response.rejected.forEach { rejection ->
            validateOutcomeIdentity(
                mutationId = rejection.mutationId,
                entityType = rejection.entityType,
                entitySyncId = rejection.entitySyncId,
                submittedMutations = submittedMutations
            )
            ensure(
                rejection.errorCode.isNotBlank(),
                "A rejected mutation is missing its error code"
            )
        }

        response.conflicts.forEach { conflict ->
            validateOutcomeIdentity(
                mutationId = conflict.mutationId,
                entityType = conflict.entityType,
                entitySyncId = conflict.entitySyncId,
                submittedMutations = submittedMutations
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
        response.changes.forEach(::validateCategoryChange)

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

        return ValidatedCategoryResponse(
            preparedRequest = preparedRequest,
            response = response,
            submittedMutationsById = submittedMutations
        )
    }

    private fun validateSubmittedMutation(mutation: TideMutation) {
        ensure(
            mutation.entityType == TideEntityType.CATEGORY,
            "The Category synchronizer received an unsupported entity type"
        )

        when (mutation.operation) {
            TideOperation.CREATE,
            TideOperation.UPDATE -> ensure(
                mutation.data != null,
                "${mutation.operation} requires mutation data"
            )

            TideOperation.DELETE -> ensure(
                mutation.data == null,
                "DELETE must not contain mutation data"
            )

            else -> throw TideResponseValidationException(
                "The request contains an unsupported mutation operation"
            )
        }
    }

    private fun validateCategoryChange(change: TideChange) {
        ensure(
            change.entityType == TideEntityType.CATEGORY,
            "The Category synchronizer received an unsupported remote entity type"
        )
        ensure(
            change.serverVersion > 0,
            "A remote change has an invalid server version"
        )

        when (change.operation) {
            TideOperation.CREATE,
            TideOperation.UPDATE -> ensure(
                change.data != null,
                "${change.operation} requires change data"
            )

            TideOperation.DELETE -> ensure(
                change.data == null,
                "DELETE must not contain change data"
            )

            else -> throw TideResponseValidationException(
                "The response contains an unsupported change operation"
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
        if (!condition) {
            throw TideResponseValidationException(message)
        }
    }
}
