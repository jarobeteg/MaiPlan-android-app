package com.example.maiplan.repository.event

import com.example.maiplan.database.entities.EventEntity
import com.google.gson.Gson
import com.google.gson.JsonParser
import java.time.LocalDate
import java.time.temporal.ChronoUnit

fun EventEntity.excludedDates(): Set<LocalDate> =
    JsonParser.parseString(excludedOccurrenceDates).asJsonArray
        .map { LocalDate.ofEpochDay(it.asLong) }.toSet()

fun encodeExcludedDates(dates: Collection<LocalDate>): String =
    Gson().toJson(dates.map(LocalDate::toEpochDay).distinct().sorted())

fun EventEntity.occurrenceDraft(date: LocalDate): EventEntity {
    require(recurrenceFrequency != null && date !in excludedDates())
    val occurrence = occurrencesIntersecting(date, date.plusDays(1))
        .firstOrNull { it.originalStartDate == date }
        ?: error("This event occurrence no longer exists")
    return copy(
        startDate = date,
        endDate = occurrence.localEndDate,
        startTime = occurrence.start?.toLocalTime(),
        endTime = occurrence.end?.toLocalTime(),
        recurrenceFrequency = null,
        recurrenceInterval = null,
        recurrenceWeekdays = null,
        recurrenceMonthlyMode = null,
        recurrenceUntilDate = null,
        excludedOccurrenceDates = "[]",
        parentSeriesSyncId = syncId,
        originalOccurrenceDate = date,
        syncId = java.util.UUID.randomUUID(),
        serverVersion = null,
    )
}

fun EventEntity.withSeriesSettings(series: EventEntity): EventEntity = copy(
    title = series.title,
    description = series.description,
    categoryLocalId = series.categoryLocalId,
    startTime = series.startTime,
    endTime = series.endTime,
    endDate = startDate.plusDays(ChronoUnit.DAYS.between(series.startDate, series.endDate)),
    zoneId = series.zoneId,
    priority = series.priority,
    location = series.location,
    reminderOffsetMinutes = series.reminderOffsetMinutes,
    reminderLeadDays = series.reminderLeadDays,
    reminderMinuteOfDay = series.reminderMinuteOfDay,
    relativeReminderMessage = series.relativeReminderMessage,
    reminderLocalId = null,
)
