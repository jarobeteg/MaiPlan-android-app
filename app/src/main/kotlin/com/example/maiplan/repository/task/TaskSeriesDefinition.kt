package com.example.maiplan.repository.task

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.UUID

const val TASK_SERIES_PAGE_SIZE = 32
private val seriesGson = GsonBuilder().serializeNulls().create()
private val uuidUrlNamespace = UUID.fromString("6ba7b811-9dad-11d1-80b4-00c04fd430c8")

fun taskUuid5(namespace: UUID, name: String): UUID {
    val bytes = ByteBuffer.allocate(16).putLong(namespace.mostSignificantBits).putLong(namespace.leastSignificantBits).array()
    val digest = MessageDigest.getInstance("SHA-1").digest(bytes + name.toByteArray(Charsets.UTF_8))
    digest[6] = ((digest[6].toInt() and 0x0f) or 0x50).toByte()
    digest[8] = ((digest[8].toInt() and 0x3f) or 0x80).toByte()
    val buffer = ByteBuffer.wrap(digest)
    return UUID(buffer.long, buffer.long)
}
fun taskOccurrenceUuid(series: UUID, date: LocalDate) = taskUuid5(uuidUrlNamespace, "maiplan/task/$series/$date")
fun taskTemplateStepUuid(task: UUID, key: UUID) = taskUuid5(task, key.toString())
fun taskExclusionUuid(series: UUID, date: LocalDate) = taskUuid5(series, "excluded:$date")

data class TaskTemplateStep(val key: String, val title: String, val estimated_time: Long? = null) {
    fun validate() { require(UUID.fromString(key).toString() == key); require(normalizeTaskTitle(title) == title); validateTaskEstimate(estimated_time) }
}
data class TaskRelativeReminder(val lead_days: Int, val minute_of_day: Int, val zone_id: String, val message: String? = null) {
    fun validate() {
        require(lead_days in 0..7 && minute_of_day in 0..1439)
        require(zone_id == "UTC" || '/' in zone_id)
        require(runCatching { ZoneId.of(zone_id) }.isSuccess) { "Enter an IANA time zone" }
        require(message == null || message.codePointCount(0, message.length) <= 512)
    }
    fun trigger(date: LocalDate): Long? = runCatching {
        date.minusDays(lead_days.toLong()).atTime(LocalTime.of(minute_of_day / 60, minute_of_day % 60))
            .atZone(ZoneId.of(zone_id)).toInstant().toEpochMilli()
    }.getOrNull()
    fun json(): String = seriesGson.toJson(this)
    companion object { fun decode(json: String) = JsonParser.parseString(json).objectValue().relativeReminder() }
}
data class TaskSeriesRevision(
    val revision: Int, val effective_from: Long, val title: String, val description: String? = null,
    val estimated_time: Long? = null, val category_sync_id: String? = null,
    val repeat_unit: Int, val repeat_interval: Int, val repeat_weekdays: Int? = null,
    val repeat_anchor_date: Long, val repeat_end_date: Long? = null,
    val steps: List<TaskTemplateStep> = emptyList(), val reminder: TaskRelativeReminder? = null,
) {
    fun rule() = TaskRepeatRule(TaskRepeatUnit.fromCode(repeat_unit), repeat_interval,
        checkNotNull(taskDateFromEpochDay(repeat_anchor_date)), repeat_weekdays, taskDateFromEpochDay(repeat_end_date))
    fun validate() {
        require(revision > 0); taskDateFromEpochDay(effective_from); rule()
        require(normalizeTaskTitle(title) == title && normalizeTaskDescription(description) == description)
        validateTaskEstimate(estimated_time); category_sync_id?.let { require(UUID.fromString(it).toString() == it) }
        require(steps.size <= 100 && steps.map { it.key }.distinct().size == steps.size)
        steps.forEach { it.validate() }; reminder?.validate()
    }
}
data class TaskSeriesSlot(val date: LocalDate, val revision: TaskSeriesRevision, val number: Int)
data class TaskSeriesDefinition(val revisions: List<TaskSeriesRevision>) {
    fun validate() {
        require(revisions.size in 1..1000)
        val retired = mutableSetOf<String>()
        var previous = emptySet<String>()
        revisions.forEachIndexed { index, revision ->
            revision.validate(); require(revision.revision == index + 1)
            val keys = revision.steps.map { it.key }.toSet()
            require(keys.intersect(retired).isEmpty()) { "Removed template step keys cannot be recycled" }
            retired.addAll(previous - keys); previous = keys
        }
        require(revisions.first().effective_from == revisions.first().repeat_anchor_date)
    }
    fun slots(from: LocalDate, through: LocalDate): Sequence<TaskSeriesSlot> = sequence {
        validateTaskDate(from); validateTaskDate(through)
        revisions.forEachIndexed { index, revision ->
            val lower = maxOf(from, checkNotNull(taskDateFromEpochDay(revision.effective_from)))
            val next = revisions.drop(index + 1).minOfOrNull { it.effective_from }?.let { taskDateFromEpochDay(it) }
            if (next != null && next <= lower) return@forEachIndexed
            val upper = minOf(through, next?.minusDays(1) ?: through)
            if (lower > upper) return@forEachIndexed
            val rule = revision.rule()
            rule.datesBetween(lower, upper).forEach { yield(TaskSeriesSlot(it, revision, rule.number(it))) }
        }
    }
    fun json(): String = seriesGson.toJson(this)
    companion object {
        fun decode(json: String): TaskSeriesDefinition {
            val tree = JsonParser.parseString(json).objectValue()
            tree.fields(setOf("revisions"))
            val revisions = checkNotNull(tree["revisions"]) { "revisions is required" }
            require(revisions.isJsonArray) { "revisions must be an array" }
            return TaskSeriesDefinition(revisions.asJsonArray.map { it.objectValue().revision() }).also { it.validate() }
        }
    }
}

private fun com.google.gson.JsonElement.objectValue(): JsonObject {
    require(isJsonObject) { "Expected a Task series object" }; return asJsonObject
}
private fun JsonObject.fields(allowed: Set<String>) {
    require(keySet().all { it in allowed }) { "Unsupported Task series field" }
}
private fun JsonObject.textValue(name: String, required: Boolean = false): String? {
    val value = get(name)?.takeUnless { it.isJsonNull }
    require(!required || value != null) { "$name is required" }
    if (value == null) return null
    require(value.isJsonPrimitive && value.asJsonPrimitive.isString) { "$name must be text" }
    return value.asString
}
private fun JsonObject.numberValue(name: String, required: Boolean = false): Long? {
    val value = get(name)?.takeUnless { it.isJsonNull }
    require(!required || value != null) { "$name is required" }
    if (value == null) return null
    require(value.isJsonPrimitive && value.asJsonPrimitive.isNumber && Regex("-?(0|[1-9][0-9]*)").matches(value.asString)) { "$name must be an integer" }
    return value.asString.toLong()
}
private fun JsonObject.intValue(name: String, required: Boolean = false): Int? = numberValue(name, required)?.let {
    require(it in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()); it.toInt()
}
private fun JsonObject.relativeReminder(): TaskRelativeReminder {
    fields(setOf("lead_days", "minute_of_day", "zone_id", "message"))
    return TaskRelativeReminder(checkNotNull(intValue("lead_days", true)), checkNotNull(intValue("minute_of_day", true)),
        checkNotNull(textValue("zone_id", true)), textValue("message")).also { it.validate() }
}
private fun JsonObject.revision(): TaskSeriesRevision {
    fields(setOf("revision", "effective_from", "title", "description", "estimated_time", "category_sync_id",
        "repeat_unit", "repeat_interval", "repeat_weekdays", "repeat_anchor_date", "repeat_end_date", "steps", "reminder"))
    val steps = get("steps")?.let { value ->
        require(value.isJsonArray) { "steps must be an array" }
        value.asJsonArray.map { element -> element.objectValue().let { step ->
            step.fields(setOf("key", "title", "estimated_time"))
            TaskTemplateStep(checkNotNull(step.textValue("key", true)), checkNotNull(step.textValue("title", true)), step.numberValue("estimated_time"))
        } }
    } ?: emptyList()
    return TaskSeriesRevision(checkNotNull(intValue("revision", true)), checkNotNull(numberValue("effective_from", true)),
        checkNotNull(textValue("title", true)), textValue("description"), numberValue("estimated_time"), textValue("category_sync_id"),
        checkNotNull(intValue("repeat_unit", true)), checkNotNull(intValue("repeat_interval", true)), intValue("repeat_weekdays"),
        checkNotNull(numberValue("repeat_anchor_date", true)), numberValue("repeat_end_date"), steps,
        get("reminder")?.takeUnless { it.isJsonNull }?.objectValue()?.relativeReminder())
}
fun TaskRepeatRule.number(slot: LocalDate): Int = when (unit) {
    TaskRepeatUnit.DAILY -> ((slot.toEpochDay() - anchorDate.toEpochDay()) / interval).toInt()
    TaskRepeatUnit.MONTHLY -> ((slot.year - anchorDate.year) * 12 + slot.monthValue - anchorDate.monthValue) / interval
    TaskRepeatUnit.WEEKLY -> {
        val mask = checkNotNull(weekdays)
        val weeks = ((slot.toEpochDay() - slot.dayOfWeek.value + 1) -
            (anchorDate.toEpochDay() - anchorDate.dayOfWeek.value + 1)) / 7 / interval
        fun count(from: Int, until: Int) = (from until until).count { mask and (1 shl it) != 0 }
        val preceding = count(0, slot.dayOfWeek.value - 1)
        if (weeks == 0L) preceding - count(0, anchorDate.dayOfWeek.value - 1)
        else count(anchorDate.dayOfWeek.value - 1, 7) + (weeks.toInt() - 1) * Integer.bitCount(mask) + preceding
    }
}
