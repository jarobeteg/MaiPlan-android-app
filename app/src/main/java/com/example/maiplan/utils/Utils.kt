package com.example.maiplan.utils

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

fun LocalDateTime.toEpochMillis(
    zoneId: ZoneId = ZoneId.systemDefault()
): Long {
    return this
        .atZone(zoneId)
        .toInstant()
        .toEpochMilli()
}