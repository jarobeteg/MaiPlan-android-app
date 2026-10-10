package com.example.maiplan.viewmodel.task

import com.example.maiplan.repository.task.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

data class TaskRepeatEditorDraft(val unit: Int = 0, val interval: String = "1", val weekdays: Int = 127,
    val endDate: String = "", val relative: Boolean = false, val leadDays: String = "0",
    val time: String = "09:00", val zone: String = ZoneId.systemDefault().id, val message: String = "") {
    fun reminder(): TaskRelativeReminder? = if (!relative) null else {
        val clock = LocalTime.parse(time)
        require(clock.second == 0 && clock.nano == 0) { "Use reminder time HH:MM" }
        TaskRelativeReminder(leadDays.toInt(), clock.hour * 60 + clock.minute, zone, message.takeIf { it.isNotBlank() }).also { it.validate() }
    }
    fun revision(content: TaskContent, steps: List<TaskStepDraft>, category: String?): TaskSeriesRevision {
        val anchor = checkNotNull(content.scheduledDate) { "Repeating Tasks need a planned anchor date" }
        val rule = TaskRepeatRule(TaskRepeatUnit.fromCode(unit), interval.toInt(), anchor,
            weekdays.takeIf { unit == 2 }, endDate.takeIf { it.isNotBlank() }?.let(LocalDate::parse))
        return TaskSeriesRevision(1, anchor.toEpochDay(), content.title, content.description, content.estimatedMilliseconds,
            category, rule.unit.code, rule.interval, rule.weekdays, anchor.toEpochDay(), rule.endDate?.toEpochDay(),
            steps.map { val step = it.input(); TaskTemplateStep(it.key, step.title, step.estimatedMilliseconds) }, reminder()).also { it.validate() }
    }
    companion object {
        fun from(revision: TaskSeriesRevision): TaskRepeatEditorDraft {
            val rule = revision.reminder
            return TaskRepeatEditorDraft(revision.repeat_unit, revision.repeat_interval.toString(), revision.repeat_weekdays ?: 127,
                taskDateFromEpochDay(revision.repeat_end_date)?.toString().orEmpty(), rule != null,
                rule?.lead_days?.toString() ?: "0", rule?.let { "%02d:%02d".format(it.minute_of_day / 60, it.minute_of_day % 60) } ?: "09:00",
                rule?.zone_id ?: ZoneId.systemDefault().id, rule?.message.orEmpty())
        }
    }
}
