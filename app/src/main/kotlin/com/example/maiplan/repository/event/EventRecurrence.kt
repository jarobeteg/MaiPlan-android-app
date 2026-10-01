package com.example.maiplan.repository.event

import com.example.maiplan.database.entities.EventEntity
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

fun EventEntity.ruleDatesOnOrAfter(fromInclusive: LocalDate): Sequence<LocalDate> = sequence {
    val anchor = startDate
    val from = maxOf(anchor, fromInclusive)
    val until = recurrenceUntilDate
    if (until != null && from.isAfter(until)) return@sequence
    when (recurrenceFrequency) {
        null -> if (!anchor.isBefore(from)) yield(anchor)
        "DAILY" -> {
            val interval = requireNotNull(recurrenceInterval).toLong()
            val days = ChronoUnit.DAYS.between(anchor, from)
            var step = (days + interval - 1) / interval
            while (true) {
                val next = runCatching { anchor.plusDays(step * interval) }.getOrNull()
                    ?: break
                if (next.year > 9999 || (until != null && next.isAfter(until))) break
                yield(next)
                step++
            }
        }
        "WEEKLY" -> {
            val interval = requireNotNull(recurrenceInterval).toLong()
            val weekdays = requireNotNull(recurrenceWeekdays)
            fun mondayOf(day: LocalDate) =
                day.minusDays((day.dayOfWeek.value - 1).toLong())
            val anchorMonday = mondayOf(anchor)
            val weeks = ChronoUnit.WEEKS.between(anchorMonday, mondayOf(from))
            var step = (weeks + interval - 1) / interval
            while (true) {
                val monday = runCatching {
                    anchorMonday.plusWeeks(step * interval)
                }.getOrNull() ?: break
                if (monday.year > 9999 || (until != null && monday.isAfter(until))) break
                for (weekday in 1..7) {
                    if (weekdays and (1 shl (weekday - 1)) == 0) continue
                    val next = monday.plusDays((weekday - 1).toLong())
                    if (next.year <= 9999 && !next.isBefore(from) &&
                        !next.isBefore(anchor) && (until == null || !next.isAfter(until))
                    ) yield(next)
                }
                step++
            }
        }
        "MONTHLY" -> {
            val interval = requireNotNull(recurrenceInterval).toLong()
            val mode = requireNotNull(recurrenceMonthlyMode)
            val anchorMonth = YearMonth.from(anchor)
            val months = ChronoUnit.MONTHS.between(anchorMonth, YearMonth.from(from))
            var step = (months + interval - 1) / interval
            while (true) {
                val month = runCatching {
                    anchorMonth.plusMonths(step * interval)
                }.getOrNull() ?: break
                if (month.year > 9999 ||
                    (until != null && month.atDay(1).isAfter(until))) break
                val next = monthlyDate(month, anchor, mode)
                if (next != null && !next.isBefore(from) &&
                    !next.isBefore(anchor) && (until == null || !next.isAfter(until))
                ) yield(next)
                step++
            }
        }
        else -> error("Unsupported recurrence frequency: $recurrenceFrequency")
    }
}

private fun monthlyDate(month: YearMonth, anchor: LocalDate, mode: String): LocalDate? =
    when (mode) {
        "DAY_OF_MONTH" ->
            if (anchor.dayOfMonth <= month.lengthOfMonth()) month.atDay(anchor.dayOfMonth)
            else null // Skip months without the selected date.
        "LAST_DAY" -> month.atEndOfMonth()
        "NTH_WEEKDAY" -> {
            val ordinal = (anchor.dayOfMonth - 1) / 7 + 1
            val offset = (anchor.dayOfWeek.value -
                month.atDay(1).dayOfWeek.value + 7) % 7
            val day = 1 + offset + (ordinal - 1) * 7
            if (day <= month.lengthOfMonth()) month.atDay(day) else null
        }
        "LAST_WEEKDAY" -> {
            val last = month.atEndOfMonth()
            val offset = (last.dayOfWeek.value - anchor.dayOfWeek.value + 7) % 7
            last.minusDays(offset.toLong())
        }
        else -> error("Unsupported monthly recurrence mode: $mode")
    }
