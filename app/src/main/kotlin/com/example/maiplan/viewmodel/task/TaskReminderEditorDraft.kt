package com.example.maiplan.viewmodel.task

import com.example.maiplan.database.entities.ReminderEntity
import com.example.maiplan.repository.task.TaskReminderDraft
import java.time.*

data class TaskReminderEditorDraft(val enabled: Boolean = false, val date: String = "", val time: String = "",
    val zoneId: String = ZoneId.systemDefault().id, val message: String = "", val preferredOffsetSeconds: Int? = null) {
    fun reminder(): TaskReminderDraft {
        require(zoneId in ZoneId.getAvailableZoneIds()) { "Choose a valid IANA time zone, such as Europe/Budapest" }
        val local = try { LocalDateTime.of(LocalDate.parse(date), LocalTime.parse(time)) }
            catch (_: DateTimeException) { throw IllegalArgumentException("Enter a reminder date as YYYY-MM-DD and time as HH:MM") }
        val zone = ZoneId.of(zoneId)
        require(zone.rules.getValidOffsets(local).isNotEmpty()) { "This time does not exist in that time zone. Choose another time." }
        require(message.length <= 512) { "Reminder message cannot exceed 512 characters" }
        val preferredOffset = preferredOffsetSeconds?.let(ZoneOffset::ofTotalSeconds)
        return TaskReminderDraft(ZonedDateTime.ofLocal(local, zone, preferredOffset).toInstant().toEpochMilli(), zoneId, message.takeIf { it.isNotBlank() })
    }
    fun enable(): TaskReminderEditorDraft {
        if (date.isNotBlank() && time.isNotBlank()) return copy(enabled = true)
        val future = ZonedDateTime.now(runCatching { ZoneId.of(zoneId) }.getOrDefault(ZoneId.systemDefault())).plusHours(1).withSecond(0).withNano(0)
        return copy(enabled = true, date = future.toLocalDate().toString(), time = future.toLocalTime().toString())
    }
    companion object {
        fun from(row: ReminderEntity?): TaskReminderEditorDraft {
            if (row == null) return TaskReminderEditorDraft()
            val local = Instant.ofEpochMilli(row.reminderTime).atZone(runCatching { ZoneId.of(row.zoneId) }.getOrDefault(ZoneOffset.UTC))
            return TaskReminderEditorDraft(row.status == 1 && row.frequency == 0, local.toLocalDate().toString(),
                local.toLocalTime().toString(), row.zoneId, row.message.orEmpty(), local.offset.totalSeconds)
        }
    }
}
