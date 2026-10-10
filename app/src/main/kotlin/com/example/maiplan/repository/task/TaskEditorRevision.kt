package com.example.maiplan.repository.task

import com.example.maiplan.database.entities.TaskEntity
import com.example.maiplan.database.entities.SubtaskEntity
import com.google.gson.JsonArray
import java.security.MessageDigest

class TaskEditorChangedException : IllegalStateException("This Task changed while you were editing it")

fun taskReminderRevision(row: com.example.maiplan.database.entities.ReminderEntity?): String {
    val fields = row?.let { listOf(it.syncId.toString(), it.reminderTime.toString(), it.zoneId,
        it.message, it.status.toString(), it.frequency.toString(), it.serverVersion?.toString(),
        it.updatedAt.toString(), it.deletedAt?.toString()) } ?: emptyList()
    val encoded = JsonArray().apply { fields.forEach { add(it) } }.toString()
    return MessageDigest.getInstance("SHA-256").digest(encoded.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
}

fun taskEditorRevision(task: TaskEntity): String {
    val fields = listOf(task.taskLocalId.toString(), task.userLocalId.toString(), task.title, task.description,
        task.status.toString(), task.scheduledDate?.toString(), task.estimatedMilliseconds?.toString(),
        task.completedDate?.toString(), task.categoryLocalId?.toString(), task.reminderLocalId?.toString(),
        task.seriesId, task.occurrenceNumber?.toString(), task.repeatUnit?.toString(), task.repeatInterval?.toString(),
        task.slotDate?.toString(), task.generationRevision?.toString(), task.relativeReminderJson, task.occurrenceOverride.toString(),
        task.repeatWeekdays?.toString(), task.repeatEndDate?.toString(), task.repeatAnchorDate?.toString(),
        task.syncId.toString(), task.serverVersion?.toString(), task.createdAt.toString(), task.updatedAt.toString(), task.deletedAt?.toString())
    val encoded = JsonArray().apply { fields.forEach { add(it) } }.toString()
    return MessageDigest.getInstance("SHA-256").digest(encoded.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
}

fun TaskSnapshot.editableContent() = TaskContent(task.title, task.description, task.scheduledDate,
    task.estimatedMilliseconds, effectiveCategoryLocalId)

fun checklistEditorRevision(steps: List<SubtaskEntity>): String {
    val fields = JsonArray().apply {
        steps.sortedBy { it.syncId.toString() }.forEach { row ->
            add(JsonArray().apply {
                listOf(row.syncId.toString(), row.title, row.estimatedMilliseconds?.toString(),
                    row.sortOrder.toString(), row.status.toString(), row.completedDate?.toString(),
                    row.serverVersion?.toString(), row.updatedAt.toString(), row.deletedAt?.toString()).forEach { add(it) }
            })
        }
    }
    return MessageDigest.getInstance("SHA-256").digest(fields.toString().toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }
}
