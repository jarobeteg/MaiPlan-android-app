package com.example.maiplan.network.sync

import com.example.maiplan.database.MaiPlanDatabase
import com.example.maiplan.database.entities.OutboxEntity
import com.example.maiplan.utils.common.OutboxStatus
import com.google.gson.JsonParser
import java.util.UUID

internal class EventTransport(private val db: MaiPlanDatabase) {
    suspend fun dependenciesReady(row: OutboxEntity): Boolean {
        if (row.attemptCount > 0 || row.operation == TideOperation.DELETE) return true
        val data = JsonParser.parseString(checkNotNull(row.payloadJson)).asJsonObject
        val parentId = data["parent_series_sync_id"]?.takeUnless { it.isJsonNull }
            ?.asString?.let(UUID::fromString) ?: return true
        val parent = db.eventDAO().getEventBySyncId(parentId, row.userLocalId)
        val blockers = db.outboxDAO().entityIntents(row.userLocalId, TideEntityType.EVENT, parentId)
            .filter { it.status != OutboxStatus.SYNCED }
        if (parent == null || parent.deletedAt != null || parent.recurrenceFrequency == null ||
            blockers.any { it.status in listOf(OutboxStatus.CONFLICT, OutboxStatus.REJECTED) }) {
            db.outboxDAO().setMutationOutcome(row.mutationId, OutboxStatus.PENDING, OutboxStatus.REJECTED,
                "EVENT_SERIES_CHANGED", null, null)
            return false
        }
        return parent.serverVersion != null && blockers.isEmpty()
    }
}
