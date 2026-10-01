package com.example.maiplan.repository.event

import com.example.maiplan.database.entities.EventEntity
import java.time.*
import java.time.temporal.ChronoUnit
import java.util.UUID

data class EventOccurrence(
    val seriesLocalId: Long,
    val seriesSyncId: UUID,
    val originalStartDate: LocalDate,
    val localEndDate: LocalDate,
    val zone: ZoneId,
    val start: ZonedDateTime?,
    val end: ZonedDateTime?,
) {
    init { require((start == null) == (end == null)) }
    val isTimed: Boolean get() = start != null
    val key: String get() = "$seriesSyncId:$originalStartDate"
}

fun EventEntity.occurrencesIntersecting(
    windowStart: LocalDate,
    windowEndExclusive: LocalDate,
): List<EventOccurrence> {
    require(windowStart.isBefore(windowEndExclusive))
    val zone = ZoneId.of(zoneId)
    val anchor = startDate
    val originalEndDate = endDate
    require((startTime == null) == (endTime == null))
    val startClock = startTime
    val endClock = endTime
    val daySpan = ChronoUnit.DAYS.between(anchor, originalEndDate)
    require(daySpan >= 0)
    val originalDuration = if (startClock != null && endClock != null) {
        Duration.between(
            resolveLocal(anchor.atTime(startClock), zone).toInstant(),
            resolveLocal(originalEndDate.atTime(endClock), zone).toInstant(),
        )
    } else null

    val firstCandidate = maxOf(anchor, windowStart.minusDays(daySpan + 1))
    return ruleDatesOnOrAfter(firstCandidate)
        .takeWhile { it.isBefore(windowEndExclusive) }
        .map { occurrenceDate ->
            val occurrenceEndDate = occurrenceDate.plusDays(daySpan)
            val resolvedStart = startClock?.let {
                resolveLocal(occurrenceDate.atTime(it), zone)
            }
            val proposedEnd = endClock?.let {
                resolveLocal(occurrenceEndDate.atTime(it), zone)
            }

            val resolvedEnd = if (resolvedStart != null && proposedEnd != null &&
                !proposedEnd.toInstant().isAfter(resolvedStart.toInstant())
            ) {
                resolvedStart.plus(requireNotNull(originalDuration))
            } else {
                proposedEnd
            }
            EventOccurrence(
                seriesLocalId = eventLocalId,
                seriesSyncId = syncId,
                originalStartDate = occurrenceDate,
                localEndDate = resolvedEnd?.toLocalDate() ?: occurrenceEndDate,
                zone = zone,
                start = resolvedStart,
                end = resolvedEnd,
            )
        }
        .filter { occurrence ->
            val start = occurrence.start
            if (start == null) {
                occurrence.originalStartDate.isBefore(windowEndExclusive) &&
                    !occurrence.localEndDate.isBefore(windowStart)
            } else {
                val end = requireNotNull(occurrence.end)
                val visibleStart = windowStart.atStartOfDay(zone).toInstant()
                val visibleEnd = windowEndExclusive.atStartOfDay(zone).toInstant()
                end.toInstant().isAfter(start.toInstant()) &&
                    start.toInstant().isBefore(visibleEnd) &&
                    end.toInstant().isAfter(visibleStart)
            }
        }
        .toList()
}

fun resolveLocal(local: LocalDateTime, zone: ZoneId): ZonedDateTime {
    val offsets = zone.rules.getValidOffsets(local)
    return when (offsets.size) {
        1 -> ZonedDateTime.ofLocal(local, zone, offsets[0])
        2 -> ZonedDateTime.ofLocal(local, zone, offsets[0])
        else -> {
            val gap = requireNotNull(zone.rules.getTransition(local))
            ZonedDateTime.ofLocal(
                local.plusSeconds(gap.duration.seconds),
                zone,
                gap.offsetAfter,
            )
        }
    }
}

data class EventDayEntry(
    val occurrence: EventOccurrence,
    val visibleDate: LocalDate,
    val isFirstDay: Boolean,
    val isLastDay: Boolean,
)

fun EventOccurrence.visibleDays(
    monthStart: LocalDate,
    monthEndExclusive: LocalDate,
): List<EventDayEntry> = generateSequence(monthStart) { it.plusDays(1) }
    .takeWhile { it.isBefore(monthEndExclusive) }
    .filter { day ->
        val startInstant = start?.toInstant()
        if (startInstant == null) {
            !day.isBefore(originalStartDate) && !day.isAfter(localEndDate)
        } else {
            val nextDay = day.plusDays(1).atStartOfDay(zone).toInstant()
            startInstant.isBefore(nextDay) &&
                requireNotNull(end).toInstant().isAfter(day.atStartOfDay(zone).toInstant())
        }
    }
    .map { day ->
        val isLast = if (end == null) day == localEndDate else
            !end.toInstant().isAfter(day.plusDays(1).atStartOfDay(zone).toInstant())
        EventDayEntry(this, day, day == originalStartDate, isLast)
    }
    .toList()
