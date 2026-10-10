package com.example.maiplan.utils.notifications

import com.example.maiplan.database.MaiPlanDatabase
import com.example.maiplan.database.entities.EventEntity
import com.example.maiplan.database.entities.NoteEntity
import com.example.maiplan.database.entities.ReminderEntity
import com.example.maiplan.database.entities.ScheduledReminderEntity
import java.time.Instant
import com.example.maiplan.database.entities.TaskEntity
import com.example.maiplan.repository.task.TaskStatus

class ReminderPlanWriter(private val database: MaiPlanDatabase) {
    suspend fun task(task: TaskEntity, reminder: ReminderEntity?, now: Instant = Instant.now()) {
        if (taskSeriesSuppressesReminder(database, task)) {
            replace("task:${task.taskLocalId}", null); return
        }
        val candidate = taskReminderPlan(task, reminder)
        val pending = candidate?.let { database.scheduledReminderDAO().get(it.alarmKey) }?.takeIf { it.deliveredAt == null }
        val plan = candidate?.takeIf { it.triggerAt > now.toEpochMilli() || pending != null }
        replace("task:${task.taskLocalId}", plan)
    }
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

internal suspend fun taskSeriesSuppressesReminder(db: MaiPlanDatabase, task: TaskEntity): Boolean {
    val id = task.seriesId?.let(java.util.UUID::fromString) ?: return false
    val series = db.taskSeriesDAO().get(task.userLocalId, id) ?: return true
    if (series.deletedAt != null) return true
    val operation = series.pendingOperationJson?.let { com.google.gson.JsonParser.parseString(it).asJsonObject } ?: return false
    return operation["kind"]?.asString == "DELETE" || operation["cutoff"]?.asLong?.let { cutoff ->
        task.scheduledDate?.toEpochDay()?.let { it >= cutoff } == true
    } == true
}

fun taskReminderPlan(task: TaskEntity, reminder: ReminderEntity?): ScheduledReminderEntity? {
    if (task.deletedAt != null || TaskStatus.fromCode(task.status).isTerminal) return null
    task.relativeReminderJson?.let { json ->
        if (task.reminderLocalId != null) return null
        val rule = com.example.maiplan.repository.task.TaskRelativeReminder.decode(json)
        val trigger = task.scheduledDate?.let(rule::trigger) ?: return null
        val source = "task:${task.taskLocalId}"
        return ScheduledReminderEntity(alarmKey = "$source:$trigger", sourceKey = source,
            userLocalId = task.userLocalId, taskLocalId = task.taskLocalId, triggerAt = trigger,
            title = task.title, message = rule.message.orEmpty())
    }
    if (reminder == null ||
        reminder.deletedAt != null || reminder.userLocalId != task.userLocalId ||
        reminder.reminderLocalId != task.reminderLocalId || reminder.frequency != 0 || reminder.status != 1) return null
    val source = "task:${task.taskLocalId}"
    return ScheduledReminderEntity(alarmKey = "$source:${reminder.reminderTime}", sourceKey = source,
        userLocalId = task.userLocalId, taskLocalId = task.taskLocalId, triggerAt = reminder.reminderTime,
        title = task.title, message = reminder.message.orEmpty())
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
