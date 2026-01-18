package com.example.maiplan.utils

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

fun LocalDateTime.toEpochMillis(
    zoneId: ZoneId = ZoneId.systemDefault()
): Long {
    return this
        .atZone(zoneId)
        .toInstant()
        .toEpochMilli()
}

fun LocalDate.toEpochMillis(zoneId: ZoneId = ZoneId.systemDefault()): Long {
    return this
        .atStartOfDay()
        .atZone(zoneId)
        .toInstant()
        .toEpochMilli()
}

fun LocalTime.toEpochMillis(
    referenceDate: LocalDate,
    zoneId: ZoneId = ZoneId.systemDefault()
): Long {
    return this
        .let { referenceDate.atTime(it) }
        .atZone(zoneId)
        .toInstant()
        .toEpochMilli()
}