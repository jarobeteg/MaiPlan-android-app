package com.example.maiplan.repository.event

import android.content.Context
import androidx.room.withTransaction
import com.example.maiplan.database.MaiPlanDatabase
import com.example.maiplan.database.entities.EventEntity
import com.example.maiplan.database.entities.OutboxEntity
import com.example.maiplan.database.entities.ReminderEntity
import com.example.maiplan.network.sync.EventMutationPayload
import com.example.maiplan.network.sync.TideEntityType
import com.example.maiplan.network.sync.TideOperation
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.handleLocalResponse
import com.example.maiplan.repository.reminder.ReminderMutationWriter
import com.example.maiplan.utils.notifications.ReminderPlanWriter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class StoredEventWithReminder(
    val event: EventEntity,
    val reminder: ReminderEntity?,
    val reminderWarning: String? = null,
)

class EventLocalDataSource(
    context: Context,
    private val databaseOverride: MaiPlanDatabase? = null
) {
    private val database: MaiPlanDatabase by lazy {
        databaseOverride ?: MaiPlanDatabase.getDatabase(context.applicationContext)
    }
    private val eventDao by lazy { database.eventDAO() }
    private val categoryDao by lazy { database.categoryDAO() }
    private val reminderDao by lazy { database.reminderDAO() }
    private val outboxDao by lazy { database.outboxDAO() }
    private val reminderWriter by lazy { ReminderMutationWriter(database) }
    private val gson: Gson by lazy { GsonBuilder().serializeNulls().create() }

    suspend fun createEventWithReminder(
        reminder: ReminderEntity?,
        event: EventEntity
    ): Result<StoredEventWithReminder> {
        if (event.title.isBlank()) return Result.Failure(EMPTY_EVENT_TITLE_ERROR)
        validateEventDefinition(event)
        require(reminder == null || (event.reminderOffsetMinutes == null &&
            event.reminderLeadDays == null && event.recurrenceFrequency == null))

        return handleLocalResponse {
            database.withTransaction {
                event.parentSeriesSyncId?.let { id ->
                    val parent = checkNotNull(eventDao.getEventBySyncId(id, event.userLocalId))
                    check(parent.deletedAt == null && parent.recurrenceFrequency != null &&
                        event.originalOccurrenceDate in parent.excludedDates()) {
                        "The event series has changed. Reopen the occurrence to continue."
                    }
                }
                val now = Instant.now()
                val storedReminder = reminder?.let {
                    reminderWriter.create(it, event.userLocalId, now)
                }
                val created = event.copy(
                    eventLocalId = 0L,
                    reminderLocalId = storedReminder?.reminderLocalId,
                    title = event.title.trim(),
                    serverVersion = null,
                    createdAt = now,
                    updatedAt = now,
                    deletedAt = null
                )
                val eventLocalId = eventDao.insertEvent(created)
                val storedEvent = created.copy(eventLocalId = eventLocalId)
                enqueue(storedEvent, TideOperation.CREATE, null, now)
                ReminderPlanWriter(database).event(storedEvent, storedReminder)
                StoredEventWithReminder(storedEvent, storedReminder)
            }
        }
    }

    suspend fun updateEventWithReminder(
        reminder: ReminderEntity?,
        event: EventEntity,
        occurrenceDate: LocalDate? = null,
    ): Result<StoredEventWithReminder> {
        if (event.title.isBlank()) return Result.Failure(EMPTY_EVENT_TITLE_ERROR)
        validateEventDefinition(event)
        require(reminder == null || (event.reminderOffsetMinutes == null &&
            event.reminderLeadDays == null && event.recurrenceFrequency == null))

        return handleLocalResponse {
            database.withTransaction {
                val existing = checkNotNull(
                    eventDao.getEventByLocalId(event.eventLocalId, event.userLocalId)
                ) { "Event ${event.eventLocalId} was not found" }
                check(existing.deletedAt == null) { "A deleted Event cannot be updated" }
                existing.parentSeriesSyncId?.let {
                    val parent = getSeriesEvent(existing)
                    check(parent?.deletedAt == null && parent?.recurrenceFrequency != null) {
                        "The event series has changed. Reopen the occurrence to continue."
                    }
                }

                if (occurrenceDate != null && existing.recurrenceFrequency != null) {
                    existing.occurrenceDraft(occurrenceDate)
                    check(eventDao.getOccurrenceOverride(existing.syncId, occurrenceDate.toEpochDay(), event.userLocalId) == null) {
                        "This occurrence has already been edited. Reopen it to continue."
                    }
                    excludeOccurrence(existing, occurrenceDate, Instant.now())
                    val result = createEventWithReminder(reminder, event.copy(
                        eventLocalId = 0L, syncId = UUID.randomUUID(), serverVersion = null,
                        parentSeriesSyncId = existing.syncId, originalOccurrenceDate = occurrenceDate,
                        recurrenceFrequency = null, recurrenceInterval = null,
                        recurrenceWeekdays = null, recurrenceMonthlyMode = null,
                        recurrenceUntilDate = null, excludedOccurrenceDates = "[]",
                    ))
                    return@withTransaction checkNotNull((result as? Result.Success)?.data) {
                        "Could not save the event occurrence"
                    }
                }

                val now = Instant.now()
                val reminderToDelete = if (reminder == null) existing.reminderLocalId else null
                val storedReminder = when {
                    existing.reminderLocalId == null && reminder != null -> {
                        reminderWriter.create(reminder, event.userLocalId, now)
                    }
                    existing.reminderLocalId != null && reminder != null -> {
                        reminderWriter.update(
                            reminder.copy(reminderLocalId = existing.reminderLocalId),
                            event.userLocalId,
                            now
                        )
                    }
                    existing.reminderLocalId != null -> null
                    else -> null
                }

                val updated = existing.copy(
                    categoryLocalId = event.categoryLocalId,
                    reminderLocalId = storedReminder?.reminderLocalId,
                    title = event.title.trim(),
                    description = event.description,
                    startDate = event.startDate,
                    endDate = event.endDate,
                    startTime = event.startTime,
                    endTime = event.endTime,
                    recurrenceFrequency = event.recurrenceFrequency,
                    recurrenceInterval = event.recurrenceInterval,
                    recurrenceWeekdays = event.recurrenceWeekdays,
                    recurrenceMonthlyMode = event.recurrenceMonthlyMode,
                    recurrenceUntilDate = event.recurrenceUntilDate,
                    excludedOccurrenceDates = if (event.recurrenceFrequency == null) "[]" else existing.excludedOccurrenceDates,
                    reminderOffsetMinutes = event.reminderOffsetMinutes,
                    reminderLeadDays = event.reminderLeadDays,
                    reminderMinuteOfDay = event.reminderMinuteOfDay,
                    relativeReminderMessage = event.relativeReminderMessage,
                    zoneId = event.zoneId,
                    priority = event.priority,
                    location = event.location,
                    updatedAt = now
                )
                check(eventDao.updateEvent(updated) == 1) {
                    "Event update affected an unexpected number of rows"
                }
                enqueue(updated, TideOperation.UPDATE, existing.serverVersion, now)
                if (occurrenceDate == null && existing.recurrenceFrequency != null) {
                    for (override in eventDao.getSeriesOverrides(existing.syncId, event.userLocalId)) {
                        if (updated.recurrenceFrequency == null) {
                            deleteStoredEvent(override, now)
                        } else {
                            val result = updateEventWithReminder(null, override.withSeriesSettings(updated),
                                override.originalOccurrenceDate)
                            check(result is Result.Success) { "Could not update an edited occurrence" }
                        }
                    }
                }
                reminderToDelete?.let {
                    reminderWriter.delete(it, event.userLocalId, now)
                }
                ReminderPlanWriter(database).event(updated, storedReminder)
                StoredEventWithReminder(event = updated, reminder = storedReminder)
            }
        }
    }

    suspend fun softDeleteEventWithReminder(
        eventLocalId: Long,
        userLocalId: Long,
        occurrenceDate: LocalDate? = null,
    ): Result<Unit> {
        return handleLocalResponse {
            database.withTransaction {
                val existing = checkNotNull(
                    eventDao.getEventByLocalId(eventLocalId, userLocalId)
                ) { "Event $eventLocalId was not found" }

                if (existing.deletedAt == null) {
                    val now = Instant.now()
                    if (occurrenceDate != null && existing.recurrenceFrequency != null) {
                        existing.occurrenceDraft(occurrenceDate)
                        excludeOccurrence(existing, occurrenceDate, now)
                    } else {
                        val series = if (occurrenceDate == null && existing.parentSeriesSyncId != null)
                            checkNotNull(eventDao.getEventBySyncId(existing.parentSeriesSyncId, userLocalId))
                        else existing
                        for (override in eventDao.getSeriesOverrides(series.syncId, userLocalId)) {
                            deleteStoredEvent(override, now)
                        }
                        deleteStoredEvent(series, now)
                    }
                }
                Unit
            }
        }
    }

    suspend fun getEvent(eventLocalId: Long, userLocalId: Long): EventEntity? {
        return eventDao.getEventByLocalId(eventLocalId, userLocalId)
    }

    suspend fun getSeriesEvent(event: EventEntity): EventEntity? = event.parentSeriesSyncId?.let {
        eventDao.getEventBySyncId(it, event.userLocalId)
    } ?: event.takeIf { it.parentSeriesSyncId == null }

    private suspend fun excludeOccurrence(event: EventEntity, date: LocalDate, now: Instant) {
        val updated = event.copy(excludedOccurrenceDates = encodeExcludedDates(event.excludedDates() + date), updatedAt = now)
        check(eventDao.updateEvent(updated) == 1)
        enqueue(updated, TideOperation.UPDATE, event.serverVersion, now)
        ReminderPlanWriter(database).event(updated, null)
    }

    private suspend fun deleteStoredEvent(event: EventEntity, now: Instant) {
        if (event.deletedAt != null) return
        val tombstone = event.copy(updatedAt = now, deletedAt = now)
        check(eventDao.updateEvent(tombstone) == 1)
        val intents = outboxDao.entityIntents(event.userLocalId, TideEntityType.EVENT, event.syncId)
        if (event.parentSeriesSyncId != null && event.serverVersion == null && intents.isNotEmpty() &&
            intents.all { it.attemptCount == 0 && it.status == com.example.maiplan.utils.common.OutboxStatus.PENDING }) {
            outboxDao.deleteNeverAttemptedIntents(event.userLocalId, TideEntityType.EVENT, event.syncId)
        } else {
            enqueue(tombstone, TideOperation.DELETE, event.serverVersion, now)
        }
        ReminderPlanWriter(database).event(tombstone, null)
        event.reminderLocalId?.let { reminderWriter.delete(it, event.userLocalId, now) }
    }

    suspend fun getEventsForMonth(
        userLocalId: Long,
        monthStart: java.time.LocalDate,
        monthEndExclusive: java.time.LocalDate,
    ): List<EventEntity> {
        val lowerDay = monthStart.toEpochDay()
        val upperDay = monthEndExclusive.toEpochDay()
        return (
            eventDao.getTimedOneOffOverlapping(userLocalId, lowerDay, upperDay) +
            eventDao.getDateOnlyOneOffOverlapping(
                userLocalId, lowerDay, upperDay
            ) +
            eventDao.getActiveSeries(userLocalId)
        ).distinctBy { it.eventLocalId }.filter { event ->
            val series = getSeriesEvent(event)
            event.parentSeriesSyncId == null || (series?.deletedAt == null && series?.recurrenceFrequency != null)
        }
    }

    private suspend fun enqueue(
        event: EventEntity,
        operation: String,
        baseVersion: Long?,
        now: Instant
    ) {
        val payload = if (operation == TideOperation.DELETE) null else {
            val categorySyncId = event.categoryLocalId?.let { localId ->
                checkNotNull(categoryDao.getCategoryByLocalId(localId, event.userLocalId)) {
                    "Category $localId was not found for Event ${event.eventLocalId}"
                }.syncId
            }
            val reminderSyncId = event.reminderLocalId?.let { localId ->
                checkNotNull(reminderDao.getReminderByLocalId(localId, event.userLocalId)) {
                    "Reminder $localId was not found for Event ${event.eventLocalId}"
                }.syncId
            }
            gson.toJson(
                EventMutationPayload(
                    categorySyncId = categorySyncId,
                    reminderSyncId = reminderSyncId,
                    title = event.title,
                    description = event.description,
                    startDate = event.startDate.toEpochDay(),
                    endDate = event.endDate.toEpochDay(),
                    startTime = eventTimeToEpochMillis(
                        event.startDate, event.startTime, event.zoneId
                    ),
                    endTime = eventTimeToEpochMillis(
                        event.endDate, event.endTime, event.zoneId
                    ),
                    recurrenceFrequency = event.recurrenceFrequency,
                    recurrenceInterval = event.recurrenceInterval,
                    recurrenceWeekdays = event.recurrenceWeekdays,
                    recurrenceMonthlyMode = event.recurrenceMonthlyMode,
                    recurrenceUntilDate = event.recurrenceUntilDate?.toEpochDay(),
                    reminderOffsetMinutes = event.reminderOffsetMinutes,
                    reminderLeadDays = event.reminderLeadDays,
                    reminderMinuteOfDay = event.reminderMinuteOfDay,
                    relativeReminderMessage = event.relativeReminderMessage,
                    zoneId = event.zoneId,
                    priority = event.priority,
                    location = event.location,
                    excludedOccurrenceDates = event.excludedDates().map(LocalDate::toEpochDay).sorted(),
                    parentSeriesSyncId = event.parentSeriesSyncId,
                    originalOccurrenceDate = event.originalOccurrenceDate?.toEpochDay(),
                )
            )
        }

        outboxDao.insertMutation(
            OutboxEntity(
                mutationId = UUID.randomUUID(),
                userLocalId = event.userLocalId,
                entityType = TideEntityType.EVENT,
                entitySyncId = event.syncId,
                operation = operation,
                baseVersion = baseVersion,
                payloadJson = payload,
                createdAt = now
            )
        )
    }

    private companion object {
        const val EMPTY_EVENT_TITLE_ERROR = 1
    }
}
