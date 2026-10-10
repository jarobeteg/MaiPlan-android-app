package com.example.maiplan.repository.task

import java.time.Duration
import java.time.LocalDate

fun taskDateFromEpochDay(epochDay: Long?): LocalDate? = epochDay?.let {
    require(it in TaskContract.MIN_DATE.toEpochDay()..TaskContract.MAX_DATE.toEpochDay()) {
        "Task date is outside years 1 through 9999"
    }
    LocalDate.ofEpochDay(it)
}

fun taskDateToEpochDay(date: LocalDate?): Long? {
    validateTaskDate(date)
    return date?.toEpochDay()
}

fun taskDurationFromMilliseconds(milliseconds: Long?): Duration? {
    validateTaskEstimate(milliseconds)
    return milliseconds?.let(Duration::ofMillis)
}

fun taskDurationToMilliseconds(duration: Duration?): Long? = duration?.let {
    require(!it.isNegative && it <= Duration.ofMillis(TaskContract.MAX_ESTIMATED_MILLISECONDS)) {
        "Estimated duration must be between zero and 365 fixed days"
    }
    require(it.nano % 1_000_000 == 0) { "Task duration must contain whole milliseconds" }
    it.toMillis()
}
