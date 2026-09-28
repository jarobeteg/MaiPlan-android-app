package com.example.maiplan.home.event.screens

import com.example.maiplan.utils.toEpochMillis
import java.time.LocalDateTime
import java.time.ZoneId

internal fun isNonexistentLocalTime(value: LocalDateTime, zone: ZoneId): Boolean =
    zone.rules.getValidOffsets(value).isEmpty()

internal fun reminderEpochMillisForUpdate(
    localDateTime: LocalDateTime,
    selectedZone: ZoneId,
    originalMillis: Long?,
    originalZoneId: String,
    reminderEdited: Boolean,
): Long =
    if (!reminderEdited && selectedZone.id == originalZoneId && originalMillis != null) {
        originalMillis
    } else {
        localDateTime.withSecond(0).withNano(0).toEpochMillis(selectedZone)
    }
