package com.example.maiplan.network.sync

import java.util.UUID
import com.example.maiplan.repository.task.decodeTaskMutationDefinition
import com.example.maiplan.repository.task.decodeSubtaskMutationDefinition

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
            ensureUnique(acknowledgement.effects.map { it.entityType to it.entitySyncId }, "Action effect identities")
            acknowledgement.continuation?.let { continuation ->
                ensure(acknowledgement.entityType == TideEntityType.TASK_SERIES_ACTION, "Only series actions may continue")
                UUID.fromString(continuation["id"].asString)
                ensure(continuation["kind"].asString in setOf("EDIT", "DELETE"), "Unknown series continuation")
                ensure(continuation["after"].asBigDecimal.longValueExact() >= 0, "Invalid continuation checkpoint")
            }
            acknowledgement.effects.forEach { effect ->
                ensure(acknowledgement.entityType in setOf(TideEntityType.TASK_ACTION, TideEntityType.TASK_SERIES_ACTION), "Only Task actions have aggregate effects")
                ensure(effect.entityType in setOf(TideEntityType.TASK, TideEntityType.SUBTASK, TideEntityType.TASK_SERIES, TideEntityType.TASK_EXCLUSION), "Invalid action effect type")
                validateChange(effect.change())
                if (acknowledgement.entityType == TideEntityType.TASK_SERIES_ACTION) {
                    if (effect.entityType == TideEntityType.TASK_SERIES) ensure(effect.entitySyncId == acknowledgement.entitySyncId, "Effect belongs to another series")
                    if (effect.entityType in setOf(TideEntityType.TASK, TideEntityType.TASK_EXCLUSION) && effect.data != null)
                        ensure(effect.data["series_id"].asString == acknowledgement.entitySyncId.toString(), "Occurrence effect belongs to another series")
                    if (effect.entityType == TideEntityType.SUBTASK && effect.data != null)
                        ensure(acknowledgement.effects.any { it.entityType == TideEntityType.TASK && it.entitySyncId.toString() == effect.data["parent_task_sync_id"].asString }, "Series step is missing its parent effect")
                }
                if (effect.entityType == TideEntityType.TASK && acknowledgement.entityType == TideEntityType.TASK_ACTION) ensure(effect.entitySyncId == acknowledgement.entitySyncId, "Action effect references a different Task")
                if (effect.entityType == TideEntityType.SUBTASK && effect.data != null && acknowledgement.entityType == TideEntityType.TASK_ACTION) ensure(
                    effect.data["parent_task_sync_id"].asString == acknowledgement.entitySyncId.toString(), "Action step references a different Task")
            }
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
            if (conflict.entityType == TideEntityType.TASK_ACTION) {
                val server = conflict.serverData ?: throw TideResponseValidationException("Task conflict needs an aggregate snapshot")
                fun snapshot(data: com.google.gson.JsonObject) = TideActionSnapshot(data["entity_type"].asString,
                    UUID.fromString(data["entity_sync_id"].asString), data["operation"].asString,
                    data["server_version"].asBigDecimal.longValueExact(), data["data"]?.takeUnless { it.isJsonNull }?.asJsonObject)
                val parent = snapshot(server.getAsJsonObject("task"))
                ensure(parent.entityType == TideEntityType.TASK && parent.entitySyncId == conflict.entitySyncId &&
                    parent.serverVersion == conflict.serverVersion, "Conflict Task identity/version mismatch")
                validateChange(parent.change())
                val children = server.getAsJsonArray("subtasks").map { snapshot(it.asJsonObject) }
                ensureUnique(children.map { it.entitySyncId }, "Conflict step IDs")
                children.forEach { child ->
                    ensure(child.entityType == TideEntityType.SUBTASK, "Invalid conflict step type")
                    validateChange(child.change())
                    if (child.data != null) ensure(UUID.fromString(child.data["parent_task_sync_id"].asString) == conflict.entitySyncId,
                        "Conflict step belongs to another Task")
                }
            }
            if (conflict.entityType == TideEntityType.TASK_SERIES_ACTION) {
                val series = checkNotNull(conflict.serverData).getAsJsonObject("series")
                ensure(series["entity_type"].asString == TideEntityType.TASK_SERIES && series["entity_sync_id"].asString == conflict.entitySyncId.toString(), "Invalid series conflict identity")
            }
        }

        ensure(
            response.changes.size <= request.dataLimit,
            "The response exceeds the requested data limit"
        )
        ensureUnique(response.changes.map { it.sequence }, "Response change sequences")
        var previousSequence = request.cursor?.toLong() ?: 0L
        response.changes.forEach {
            val sequence = it.sequence.toLong()
            ensure(sequence > previousSequence, "Change sequences must advance in feed order")
            previousSequence = sequence
        }
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
        } else {
            ensure(response.nextCursor == (request.cursor ?: "0"), "An empty page must retain its cursor")
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
        if (mutation.entityType in setOf(TideEntityType.TASK_ACTION, TideEntityType.TASK_SERIES_ACTION)) {
            ensure(mutation.data != null, "Task action requires aggregate data")
            ensure(mutation.operation in setOf(TideOperation.CREATE, TideOperation.UPDATE, TideOperation.DELETE), "Invalid Task action operation")
        } else validateOperationAndData(mutation.operation, mutation.data != null, "mutation")
    }

    private fun validateChange(change: TideChange) {
        ensure(
            change.entityType in TideEntityType.SUPPORTED,
            "The TIDE response contains an unsupported entity type"
        )
        ensure(change.serverVersion > 0, "A remote change has an invalid server version")
        validateOperationAndData(change.operation, change.data != null, "change")
        if (change.data != null && change.entityType in setOf(TideEntityType.TASK, TideEntityType.SUBTASK)) {
            val business = change.data.deepCopy().also { it.remove("created_at"); it.remove("updated_at") }
            if (change.entityType == TideEntityType.TASK) decodeTaskMutationDefinition(business)
            else decodeSubtaskMutationDefinition(business)
        }
        if (change.data != null && change.entityType == TideEntityType.TASK_SERIES)
            com.example.maiplan.repository.task.TaskSeriesDefinition.decode(change.data["definition"].toString())
        if (change.entityType == TideEntityType.TASK_EXCLUSION) {
            ensure(change.data != null && change.operation != TideOperation.DELETE, "Exclusions persist for the series lifetime")
            val data = checkNotNull(change.data)
            val id = UUID.fromString(data["series_id"].asString)
            val date = checkNotNull(com.example.maiplan.repository.task.taskDateFromEpochDay(data["slot_date"].asBigDecimal.longValueExact()))
            ensure(change.entitySyncId == com.example.maiplan.repository.task.taskExclusionUuid(id, date), "Invalid exclusion UUID")
        }
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
