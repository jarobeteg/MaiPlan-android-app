package com.example.maiplan.repository.event

import com.example.maiplan.database.entities.EventEntity
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

fun validateEventDefinition(event: EventEntity) {
    val minDate = LocalDate.of(1, 1, 1)
    val maxDate = LocalDate.of(9999, 12, 31)
    listOfNotNull(event.startDate, event.endDate, event.recurrenceUntilDate).forEach {
        require(it in minDate..maxDate) { "Event date outside supported range" }
    }
    require(!event.endDate.isBefore(event.startDate)) { "End date precedes start date" }
    require((event.startTime == null) == (event.endTime == null)) {
        "Event times must be present together"
    }
    val timed = event.startTime != null
    val zone = ZoneId.of(event.zoneId)
    require(zone.id == "UTC" || '/' in zone.id) { "Event needs an IANA region time zone" }
    if (timed) {
        val startTime = requireNotNull(event.startTime)
        val endTime = requireNotNull(event.endTime)

        require(startTime.nano % 1_000_000 == 0 && endTime.nano % 1_000_000 == 0)
        val startLocal = event.startDate.atTime(startTime)
        val endLocal = event.endDate.atTime(endTime)
        require(zone.rules.getValidOffsets(startLocal).isNotEmpty()) {
            "Event start falls in a daylight-saving gap"
        }
        require(zone.rules.getValidOffsets(endLocal).isNotEmpty()) {
            "Event end falls in a daylight-saving gap"
        }
        require(resolveLocal(endLocal, zone).toInstant()
            .isAfter(resolveLocal(startLocal, zone).toInstant())) {
            "Event end must follow start"
        }
    }
    when (event.recurrenceFrequency) {
        null -> require(event.recurrenceInterval == null &&
            event.recurrenceWeekdays == null && event.recurrenceMonthlyMode == null &&
            event.recurrenceUntilDate == null) { "Incomplete recurrence rule" }
        "DAILY" -> {
            require(event.recurrenceInterval in 1..365)
            require(event.recurrenceWeekdays == null && event.recurrenceMonthlyMode == null)
        }
        "WEEKLY" -> {
            require(event.recurrenceInterval in 1..52)
            val mask = requireNotNull(event.recurrenceWeekdays)
            require(mask in 1..127)
            require(mask and (1 shl (event.startDate.dayOfWeek.value - 1)) != 0)
            require(event.recurrenceMonthlyMode == null)
        }
        "MONTHLY" -> {
            require(event.recurrenceInterval in 1..24)
            require(event.recurrenceWeekdays == null)
            require(event.recurrenceMonthlyMode in setOf(
                "DAY_OF_MONTH", "LAST_DAY", "NTH_WEEKDAY", "LAST_WEEKDAY"
            ))
            if (event.recurrenceMonthlyMode == "LAST_DAY") {
                require(event.startDate.dayOfMonth == event.startDate.lengthOfMonth()) {
                    "The first occurrence must be the month's last day"
                }
            }
            if (event.recurrenceMonthlyMode == "LAST_WEEKDAY") {
                require(event.startDate.dayOfMonth + 7 > event.startDate.lengthOfMonth()) {
                    "The first occurrence must be the month's last selected weekday"
                }
            }
        }
        else -> throw IllegalArgumentException("Unsupported recurrence frequency")
    }
    if (event.recurrenceFrequency != null) {
        require(event.recurrenceUntilDate == null ||
            !event.recurrenceUntilDate.isBefore(event.startDate))
        require(event.reminderLocalId == null)
        val daySpan = ChronoUnit.DAYS.between(event.startDate, event.endDate)
        val datesToCheck = when (event.recurrenceFrequency) {
            "DAILY" -> 2
            "WEEKLY" -> 16
            else -> 4801
        }
        val overlapping = daySpan > 0 && event.ruleDatesOnOrAfter(event.startDate)
            .take(datesToCheck)
            .zipWithNext()
            .any { (current, next) ->
                val currentEndDate = current.plusDays(daySpan)
                if (!timed) {
                    !currentEndDate.isBefore(next)
                } else {
                    resolveLocal(currentEndDate.atTime(requireNotNull(event.endTime)), zone)
                        .toInstant().isAfter(
                            resolveLocal(next.atTime(requireNotNull(event.startTime)), zone)
                                .toInstant()
                        )
                }
            }
        require(!overlapping) {
            "Event occurrences overlap. Shorten End date for each occurrence; use Repeat until for the last repeat."
        }
    }
    val hasDateLead = event.reminderLeadDays != null
    require(hasDateLead == (event.reminderMinuteOfDay != null))
    require(event.reminderOffsetMinutes == null ||
        (timed && event.reminderOffsetMinutes in 0..10080))
    require(!hasDateLead || (!timed && event.reminderLeadDays in 0..7 &&
        event.reminderMinuteOfDay in 0..1439))
    val modes = listOf(
        event.reminderLocalId != null,
        event.reminderOffsetMinutes != null,
        hasDateLead,
    ).count { it }
    require(modes <= 1) { "Only one reminder mode is allowed" }
    require(event.relativeReminderMessage == null ||
        event.reminderOffsetMinutes != null || hasDateLead)
}
