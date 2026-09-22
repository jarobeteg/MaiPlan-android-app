package com.example.maiplan.repository.reminder

import com.example.maiplan.database.MaiPlanDatabase
import com.example.maiplan.database.entities.OutboxEntity
import com.example.maiplan.database.entities.ReminderEntity
import com.example.maiplan.network.sync.ReminderMutationPayload
import com.example.maiplan.network.sync.TideEntityType
import com.example.maiplan.network.sync.TideOperation
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.time.Instant
import java.util.UUID

internal class ReminderMutationWriter(private val database: MaiPlanDatabase) {
    private val reminderDao = database.reminderDAO()
    private val outboxDao = database.outboxDAO()
    private val gson: Gson = GsonBuilder().serializeNulls().create()

    suspend fun create(
        reminder: ReminderEntity,
        userLocalId: Long,
        now: Instant = Instant.now()
    ): ReminderEntity {
        check(reminder.userLocalId == userLocalId) {
            "Reminder ownership does not match the active user"
        }

        val created = reminder.copy(
            reminderLocalId = 0L,
            serverVersion = null,
            createdAt = now,
            updatedAt = now,
            deletedAt = null
        )
        val localId = reminderDao.insertReminder(created)
        val stored = created.copy(reminderLocalId = localId)

        enqueue(stored, TideOperation.CREATE, baseVersion = null, now = now)
        return stored
    }

    suspend fun update(
        reminder: ReminderEntity,
        userLocalId: Long,
        now: Instant = Instant.now()
    ): ReminderEntity {
        val existing = checkNotNull(
            reminderDao.getReminderByLocalId(reminder.reminderLocalId, userLocalId)
        ) { "Reminder ${reminder.reminderLocalId} was not found" }
        check(existing.deletedAt == null) { "A deleted Reminder cannot be updated" }

        val updated = existing.copy(
            reminderTime = reminder.reminderTime,
            zoneId = reminder.zoneId,
            frequency = reminder.frequency,
            status = reminder.status,
            message = reminder.message,
            updatedAt = now
        )
        check(reminderDao.updateReminder(updated) == 1) {
            "Reminder update affected an unexpected number of rows"
        }

        enqueue(
            updated,
            TideOperation.UPDATE,
            baseVersion = existing.serverVersion,
            now = now
        )
        return updated
    }

    suspend fun delete(
        reminderLocalId: Long,
        userLocalId: Long,
        now: Instant = Instant.now()
    ): ReminderEntity? {
        val existing = checkNotNull(
            reminderDao.getReminderByLocalId(reminderLocalId, userLocalId)
        ) { "Reminder $reminderLocalId was not found" }

        if (existing.deletedAt != null) {
            return null
        }

        val tombstone = existing.copy(updatedAt = now, deletedAt = now)
        check(reminderDao.updateReminder(tombstone) == 1) {
            "Reminder deletion affected an unexpected number of rows"
        }
        enqueue(
            tombstone,
            TideOperation.DELETE,
            baseVersion = existing.serverVersion,
            now = now
        )
        return tombstone
    }

    private suspend fun enqueue(
        reminder: ReminderEntity,
        operation: String,
        baseVersion: Long?,
        now: Instant
    ) {
        outboxDao.insertMutation(
            OutboxEntity(
                mutationId = UUID.randomUUID(),
                userLocalId = reminder.userLocalId,
                entityType = TideEntityType.REMINDER,
                entitySyncId = reminder.syncId,
                operation = operation,
                baseVersion = baseVersion,
                payloadJson = if (operation == TideOperation.DELETE) {
                    null
                } else {
                    gson.toJson(
                        ReminderMutationPayload(
                            reminderTime = reminder.reminderTime,
                            zoneId = reminder.zoneId,
                            frequency = reminder.frequency,
                            status = reminder.status,
                            message = reminder.message
                        )
                    )
                },
                createdAt = now
            )
        )
    }
}
