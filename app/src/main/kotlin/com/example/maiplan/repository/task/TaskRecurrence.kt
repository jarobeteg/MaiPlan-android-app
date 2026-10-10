package com.example.maiplan.repository.task

import java.time.LocalDate
import java.time.YearMonth

enum class TaskRepeatUnit(val code: Int, val maxInterval: Int) {
    DAILY(1, 365), WEEKLY(2, 52), MONTHLY(3, 24);

    companion object {
        fun fromCode(code: Int): TaskRepeatUnit = entries.firstOrNull { it.code == code }
            ?: throw IllegalArgumentException("Unknown Task repeat unit: $code")
    }
}

data class TaskRepeatRule(
    val unit: TaskRepeatUnit,
    val interval: Int,
    val anchorDate: LocalDate,
    val weekdays: Int? = null,
    val endDate: LocalDate? = null,
) {
    init {
        require(interval in 1..unit.maxInterval) { "Repeat interval is outside its unit's supported range" }
        validateTaskDate(anchorDate)
        validateTaskDate(endDate)
        require(endDate == null || !endDate.isBefore(anchorDate)) { "Repeat end precedes anchor" }
        if (unit == TaskRepeatUnit.WEEKLY) {
            require(weekdays != null && weekdays in 1..127) { "Weekly repetition requires a weekday mask in 1..127" }
            require((weekdays and (1 shl (anchorDate.dayOfWeek.value - 1))) != 0) {
                "The weekly mask must include the anchor weekday"
            }
        } else require(weekdays == null) { "Only weekly repetition has a weekday mask" }
    }
}

fun TaskRepeatRule.datesBetween(fromDate: LocalDate, throughDate: LocalDate): Sequence<LocalDate> = sequence {
    validateTaskDate(fromDate)
    validateTaskDate(throughDate)
    require(!fromDate.isAfter(throughDate)) { "Calendar window is reversed" }
    val start = maxOf(fromDate, anchorDate)
    val end = minOf(throughDate, endDate ?: TaskContract.MAX_DATE)
    if (start.isAfter(end)) return@sequence
    when (unit) {
        TaskRepeatUnit.DAILY -> {
            val offset = start.toEpochDay() - anchorDate.toEpochDay()
            var step = (offset + interval - 1) / interval
            while (true) {
                val day = anchorDate.toEpochDay() + step * interval
                if (day > end.toEpochDay()) break
                yield(LocalDate.ofEpochDay(day))
                step++
            }
        }
        TaskRepeatUnit.WEEKLY -> {
            val anchorMonday = anchorDate.toEpochDay() - (anchorDate.dayOfWeek.value - 1)
            val startMonday = start.toEpochDay() - (start.dayOfWeek.value - 1)
            val weeks = (startMonday - anchorMonday) / 7
            var step = (weeks + interval - 1) / interval
            while (true) {
                val monday = anchorMonday + step * interval * 7
                if (monday > end.toEpochDay()) break
                for (weekday in 0..6) {
                    val day = monday + weekday
                    if ((requireNotNull(weekdays) and (1 shl weekday)) != 0 &&
                        day in start.toEpochDay()..end.toEpochDay()) yield(LocalDate.ofEpochDay(day))
                }
                step++
            }
        }
        TaskRepeatUnit.MONTHLY -> {
            val anchorMonth = (anchorDate.year - 1) * 12 + anchorDate.monthValue - 1
            val startMonth = (start.year - 1) * 12 + start.monthValue - 1
            var step = (startMonth - anchorMonth) / interval
            while (true) {
                val index = anchorMonth + step * interval
                val year = index / 12 + 1
                val month = index % 12 + 1
                if (year > end.year || (year == end.year && month > end.monthValue)) break
                val yearMonth = YearMonth.of(year, month)
                val candidate = yearMonth.atDay(minOf(anchorDate.dayOfMonth, yearMonth.lengthOfMonth()))
                if (candidate in start..end) yield(candidate)
                step++
            }
        }
    }
}
