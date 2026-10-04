package com.example.maiplan.utils.notifications

import com.example.maiplan.database.MaiPlanDatabase
import com.example.maiplan.database.entities.EventEntity
import com.example.maiplan.database.entities.NoteEntity
import com.example.maiplan.database.entities.ReminderEntity
import com.example.maiplan.database.entities.ScheduledReminderEntity
import java.time.Instant

class ReminderPlanWriter(private val database: MaiPlanDatabase) {
    suspend fun event(event: EventEntity, reminder: ReminderEntity?) {
        replace("event:${event.eventLocalId}", eventReminderPlan(event, reminder, event.updatedAt.minusNanos(1)))
    }

    suspend fun note(note: NoteEntity, reminder: ReminderEntity?) {
        replace("note:${note.noteLocalId}", noteReminderPlan(note, reminder))
    }

    private suspend fun replace(source: String, plan: ScheduledReminderEntity?) {
        database.scheduledReminderDAO().replacePending(source, plan?.alarmKey)
        plan?.let { enqueue(it) }
    }

    suspend fun enqueue(plan: ScheduledReminderEntity) {
        database.scheduledReminderDAO().insert(plan)
        database.scheduledReminderDAO().updateContent(plan.alarmKey, plan.title, plan.message)
    }
}

fun eventReminderPlan(event: EventEntity, reminder: ReminderEntity?, after: Instant): ScheduledReminderEntity? {
    if (event.deletedAt != null) return null
    val next = if (event.reminderOffsetMinutes != null || event.reminderLeadDays != null)
        nextEventReminderAfter(event, after) else null
    val trigger = next?.triggerAtMillis ?: reminder?.takeIf { it.deletedAt == null }?.reminderTime
        ?: return null
    val source = "event:${event.eventLocalId}"
    return ScheduledReminderEntity(
        alarmKey = "$source:$trigger", sourceKey = source, userLocalId = event.userLocalId,
        eventLocalId = event.eventLocalId, occurrenceDate = next?.occurrenceDate?.toString(),
        triggerAt = trigger, title = event.title,
        message = if (next != null) event.relativeReminderMessage.orEmpty() else reminder?.message.orEmpty(),
    )
}

fun noteReminderPlan(note: NoteEntity, reminder: ReminderEntity?): ScheduledReminderEntity? {
    if (note.deletedAt != null || reminder == null || reminder.deletedAt != null) return null
    val source = "note:${note.noteLocalId}"
    return ScheduledReminderEntity(
        alarmKey = "$source:${reminder.reminderTime}", sourceKey = source,
        userLocalId = note.userLocalId, noteLocalId = note.noteLocalId,
        triggerAt = reminder.reminderTime, title = note.title, message = reminder.message.orEmpty(),
    )
}
