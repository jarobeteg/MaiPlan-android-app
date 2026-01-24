package com.example.maiplan.utils

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

fun Long.toLocalDateTime(
    zoneId: ZoneId = ZoneId.systemDefault()
): LocalDateTime {
    return Instant.ofEpochMilli(this).atZone(zoneId).toLocalDateTime()
}

fun LocalDateTime.toEpochMillis(
    zoneId: ZoneId = ZoneId.systemDefault()
): Long {
    return this
        .atZone(zoneId)
        .toInstant()
        .toEpochMilli()
}

fun LocalDate.toEpochMillis(
    zoneId: ZoneId = ZoneId.systemDefault()
): Long {
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