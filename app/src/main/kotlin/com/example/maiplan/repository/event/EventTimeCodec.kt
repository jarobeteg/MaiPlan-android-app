package com.example.maiplan.repository.event

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

fun eventTimeToEpochMillis(date: LocalDate, time: LocalTime?, zoneId: String): Long? =
    time?.let {
        resolveLocal(date.atTime(it), ZoneId.of(zoneId)).toInstant().toEpochMilli()
    }

fun eventTimeFromEpochMillis(
    epochMillis: Long?,
    date: LocalDate,
    zoneId: String,
): LocalTime? {
    if (epochMillis == null) return null
    val zone = ZoneId.of(zoneId)
    val local = Instant.ofEpochMilli(epochMillis).atZone(zone)
    require(local.toLocalDate() == date) { "Timestamp does not match event date" }
    require(resolveLocal(local.toLocalDateTime(), zone).toInstant().toEpochMilli() ==
        epochMillis) { "Timestamp uses a noncanonical daylight-saving offset" }
    return local.toLocalTime()
}
