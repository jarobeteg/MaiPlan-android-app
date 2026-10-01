package com.example.maiplan.home.event.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.maiplan.database.entities.CategoryEntity
import com.example.maiplan.database.entities.EventEntity
import com.example.maiplan.database.entities.ReminderEntity
import com.example.maiplan.repository.event.resolveLocal
import com.example.maiplan.utils.common.UserSession
import java.time.*

@Composable
internal fun EventFullEditor(
    heading: String,
    initial: EventEntity?,
    initialReminder: ReminderEntity?,
    categories: List<CategoryEntity>,
    onBack: () -> Unit,
    onSave: (ReminderEntity?, EventEntity) -> Unit,
) {
    val userId = UserSession.userLocalId ?: return
    var title by rememberSaveable(initial?.eventLocalId) { mutableStateOf(initial?.title.orEmpty()) }
    var description by rememberSaveable(initial?.eventLocalId) {
        mutableStateOf(initial?.description.orEmpty())
    }
    var startDay by rememberSaveable(initial?.eventLocalId) {
        mutableLongStateOf(initial?.startDate?.toEpochDay() ?: LocalDate.now().toEpochDay())
    }
    var endDay by rememberSaveable(initial?.eventLocalId) {
        mutableStateOf(initial?.endDate?.takeIf { it != initial.startDate }?.toEpochDay())
    }
    var timed by rememberSaveable(initial?.eventLocalId) {
        mutableStateOf(initial?.startTime != null)
    }
    val initialZone = remember(initial?.eventLocalId) {
        runCatching { ZoneId.of(initial?.zoneId ?: ZoneId.systemDefault().id) }
            .getOrDefault(ZoneOffset.UTC)
    }
    var zoneText by rememberSaveable(initial?.eventLocalId) { mutableStateOf(initialZone.id) }
    var startClock by rememberSaveable(initial?.eventLocalId) {
        mutableStateOf(initial?.startTime?.toString() ?: "09:00")
    }
    var endClock by rememberSaveable(initial?.eventLocalId) {
        mutableStateOf(initial?.endTime?.toString() ?: "10:00")
    }
    var categoryId by rememberSaveable(initial?.eventLocalId) {
        mutableStateOf(initial?.categoryLocalId)
    }
    var repeatMode by rememberSaveable(initial?.eventLocalId) {
        mutableStateOf(initial?.recurrenceFrequency ?: "NONE")
    }
    var weekdayMask by rememberSaveable(initial?.eventLocalId) {
        mutableIntStateOf(initial?.recurrenceWeekdays
            ?: (1 shl (LocalDate.ofEpochDay(startDay).dayOfWeek.value - 1)))
    }
    var intervalText by rememberSaveable(initial?.eventLocalId) {
        mutableStateOf((initial?.recurrenceInterval ?: 1).toString())
    }
    var monthlyMode by rememberSaveable(initial?.eventLocalId) {
        mutableStateOf(initial?.recurrenceMonthlyMode ?: "DAY_OF_MONTH")
    }
    var untilDay by rememberSaveable(initial?.eventLocalId) {
        mutableStateOf(initial?.recurrenceUntilDate?.toEpochDay())
    }
    var reminderMode by rememberSaveable(initial?.eventLocalId) {
        mutableStateOf(when {
            initialReminder != null -> "absolute"
            initial?.reminderOffsetMinutes != null ||
                initial?.reminderLeadDays != null -> "relative"
            else -> "none"
        })
    }
    var offsetText by rememberSaveable(initial?.eventLocalId) {
        mutableStateOf((initial?.reminderOffsetMinutes ?: 15).toString())
    }
    var leadText by rememberSaveable(initial?.eventLocalId) {
        mutableStateOf((initial?.reminderLeadDays ?: 0).toString())
    }
    var reminderClock by rememberSaveable(initial?.eventLocalId) {
        mutableStateOf(initial?.reminderMinuteOfDay?.let {
            LocalTime.of(it / 60, it % 60).toString()
        } ?: "09:00")
    }
    var absoluteDay by rememberSaveable(initial?.eventLocalId) {
        mutableLongStateOf(initialReminder?.reminderTime?.let {
            Instant.ofEpochMilli(it).atZone(initialZone).toLocalDate().toEpochDay()
        } ?: startDay)
    }
    var absoluteClock by rememberSaveable(initial?.eventLocalId) {
        mutableStateOf(initialReminder?.reminderTime?.let {
            Instant.ofEpochMilli(it).atZone(initialZone).toLocalTime().toString()
        } ?: "09:00")
    }
    var absoluteEdited by rememberSaveable(initial?.eventLocalId) { mutableStateOf(false) }
    var reminderMessage by rememberSaveable(initial?.eventLocalId) {
        mutableStateOf(initialReminder?.message ?: initial?.relativeReminderMessage.orEmpty())
    }
    var error by rememberSaveable(initial?.eventLocalId) { mutableStateOf<String?>(null) }
    var pickingDate by remember { mutableStateOf<String?>(null) }
    var pickingTime by remember { mutableStateOf<String?>(null) }

    EventScreenBackground {
        Scaffold(
            topBar = { EventEditorTopBar(heading, onBack) },
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
        ) { padding ->
            Column(
                Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(title, { title = it.take(255) }, label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(description, { description = it }, label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth())
                Button(onClick = { pickingDate = "start" }) {
                    Text("Starts: " + LocalDate.ofEpochDay(startDay))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { pickingDate = "end" }) {
                        Text("Ends: " + (endDay?.let(LocalDate::ofEpochDay) ?: "same day"))
                    }
                    if (endDay != null) TextButton(onClick = { endDay = null }) { Text("Clear") }
                }
                OutlinedTextField(zoneText, { zoneText = it }, label = { Text("Event time zone") },
                    modifier = Modifier.fillMaxWidth())
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Switch(checked = timed, onCheckedChange = { timed = it })
                    Text("Include time")
                }
                if (timed) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { pickingTime = "start" }) { Text("Start " + startClock) }
                        Button(onClick = { pickingTime = "end" }) { Text("End " + endClock) }
                    }
                }
                Text("Category")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = categoryId == null, onClick = { categoryId = null },
                        label = { Text("None") })
                }
                categories.forEach { category ->
                    FilterChip(selected = categoryId == category.categoryLocalId,
                        onClick = { categoryId = category.categoryLocalId },
                        label = { Text(category.name) })
                }
                Text("Repeat")
                Row(Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("NONE", "DAILY", "WEEKLY", "MONTHLY").forEach { mode ->
                        FilterChip(selected = repeatMode == mode, onClick = {
                            repeatMode = mode
                            if (mode == "WEEKLY") weekdayMask = weekdayMask or
                                (1 shl (LocalDate.ofEpochDay(startDay).dayOfWeek.value - 1))
                        }, label = { Text(mode.lowercase().replaceFirstChar(Char::uppercase)) })
                    }
                }
                if (repeatMode != "NONE") {
                    val unit = when (repeatMode) {
                        "DAILY" -> "days"
                        "WEEKLY" -> "weeks"
                        else -> "months"
                    }
                    OutlinedTextField(intervalText, { intervalText = it },
                        label = { Text("Every number of $unit") })
                    if (repeatMode == "WEEKLY") {
                        Text("Repeat on")
                        DayOfWeek.values().forEach { day ->
                            val bit = 1 shl (day.value - 1)
                            FilterChip(selected = weekdayMask and bit != 0,
                                onClick = { weekdayMask = weekdayMask xor bit },
                                label = { Text(day.name.lowercase().replaceFirstChar(Char::uppercase)) })
                        }
                    }
                    if (repeatMode == "MONTHLY") {
                        val anchorDate = LocalDate.ofEpochDay(startDay)
                        val weekday = anchorDate.dayOfWeek.name.lowercase()
                            .replaceFirstChar(Char::uppercase)
                        val ordinal = (anchorDate.dayOfMonth - 1) / 7 + 1
                        Text("Monthly pattern")
                        listOf(
                            "DAY_OF_MONTH" to "Day ${anchorDate.dayOfMonth}",
                            "LAST_DAY" to "Last day",
                            "NTH_WEEKDAY" to "$ordinal $weekday",
                            "LAST_WEEKDAY" to "Last $weekday",
                        ).forEach { (mode, label) ->
                            val allowed = when (mode) {
                                "LAST_DAY" ->
                                    anchorDate.dayOfMonth == anchorDate.lengthOfMonth()
                                "LAST_WEEKDAY" ->
                                    anchorDate.dayOfMonth + 7 > anchorDate.lengthOfMonth()
                                else -> true
                            }
                            FilterChip(selected = monthlyMode == mode, enabled = allowed,
                                onClick = { monthlyMode = mode }, label = { Text(label) })
                        }
                        Text("A missing day or fifth weekday skips that month.")
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { pickingDate = "until" }) {
                            Text("Repeat until: " +
                                (untilDay?.let(LocalDate::ofEpochDay) ?: "no end"))
                        }
                        if (untilDay != null) TextButton(onClick = { untilDay = null }) {
                            Text("Clear")
                        }
                    }
                }
                Text("Reminder")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("none", "relative", "absolute").forEach { mode ->
                        FilterChip(selected = reminderMode == mode,
                            onClick = { reminderMode = mode },
                            label = { Text(mode.replaceFirstChar(Char::uppercase)) })
                    }
                }
                if (reminderMode == "relative") {
                    if (timed) {
                        OutlinedTextField(offsetText, { offsetText = it },
                            label = { Text("Minutes before start") })
                    } else {
                        OutlinedTextField(leadText, { leadText = it },
                            label = { Text("Calendar days before") })
                        Button(onClick = { pickingTime = "relative" }) {
                            Text("At " + reminderClock)
                        }
                    }
                }
                if (reminderMode == "absolute") {
                    Button(onClick = { pickingDate = "absolute" }) {
                        Text("Reminder date " + LocalDate.ofEpochDay(absoluteDay))
                    }
                    Button(onClick = { pickingTime = "absolute" }) {
                        Text("Reminder time " + absoluteClock)
                    }
                }
                if (reminderMode != "none") {
                    OutlinedTextField(reminderMessage, { reminderMessage = it },
                        label = { Text("Reminder message") },
                        modifier = Modifier.fillMaxWidth())
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Button(onClick = {
                    try {
                        val zone = ZoneId.of(zoneText)
                        require(zone.id == "UTC" || '/' in zone.id) {
                            "Choose an IANA region time zone"
                        }
                        val startDate = LocalDate.ofEpochDay(startDay)
                        val endDate = LocalDate.ofEpochDay(endDay ?: startDay)
                        require(title.isNotBlank()) { "Enter a title" }
                        require(!endDate.isBefore(startDate)) { "End date precedes start date" }
                        val untilDate = if (repeatMode != "NONE")
                            untilDay?.let(LocalDate::ofEpochDay) else null
                        listOfNotNull(startDate, endDate, untilDate).forEach {
                            require(it.year in 1..9999) { "Date outside supported range" }
                        }
                        val interval = if (repeatMode != "NONE") intervalText.toInt() else null
                        val maxInterval = when (repeatMode) {
                            "DAILY" -> 365
                            "WEEKLY" -> 52
                            "MONTHLY" -> 24
                            else -> 1
                        }
                        require(interval == null || interval in 1..maxInterval) {
                            "Repeat interval must be 1–$maxInterval"
                        }
                        require(repeatMode != "WEEKLY" || weekdayMask in 1..127) {
                            "Select weekdays"
                        }
                        require(repeatMode != "WEEKLY" || weekdayMask and
                            (1 shl (startDate.dayOfWeek.value - 1)) != 0) {
                            "Start date must be selected weekday"
                        }
                        require(repeatMode != "MONTHLY" || monthlyMode != "LAST_DAY" ||
                            startDate.dayOfMonth == startDate.lengthOfMonth()) {
                            "Choose a last day as the first occurrence"
                        }
                        require(repeatMode != "MONTHLY" || monthlyMode != "LAST_WEEKDAY" ||
                            startDate.dayOfMonth + 7 > startDate.lengthOfMonth()) {
                            "Choose a last weekday as the first occurrence"
                        }
                        require(untilDate == null || !untilDate.isBefore(startDate)) {
                            "Repeat end precedes start"
                        }
                        require(repeatMode == "NONE" || reminderMode != "absolute") {
                            "Use a relative reminder for repeating events"
                        }
                        val startTime = if (timed) LocalTime.parse(startClock) else null
                        val endTime = if (timed) LocalTime.parse(endClock) else null
                        if (timed) {
                            require(!isNonexistentLocalTime(startDate.atTime(requireNotNull(startTime)), zone))
                            require(!isNonexistentLocalTime(endDate.atTime(requireNotNull(endTime)), zone))
                        }
                        val startInstant = startTime?.let {
                            resolveLocal(startDate.atTime(it), zone).toInstant()
                        }
                        val endInstant = endTime?.let {
                            resolveLocal(endDate.atTime(it), zone).toInstant()
                        }
                        require(startInstant == null || endInstant!!.isAfter(startInstant)) {
                            "End must follow start"
                        }
                        val offset = if (reminderMode == "relative" && timed)
                            offsetText.toInt() else null
                        val lead = if (reminderMode == "relative" && !timed)
                            leadText.toInt() else null
                        require(offset == null || offset in 0..10080)
                        require(lead == null || lead in 0..7)
                        val minute = if (lead != null) {
                            val clock = LocalTime.parse(reminderClock)
                            clock.hour * 60 + clock.minute
                        } else null
                        val event = (initial ?: EventEntity(
                            userLocalId = userId, title = title.trim(), startDate = startDate,
                        )).copy(
                            title = title.trim(), description = description.trim(),
                            categoryLocalId = categoryId, startDate = startDate, endDate = endDate,
                            startTime = startTime,
                            endTime = endTime, zoneId = zone.id,
                            recurrenceFrequency = repeatMode.takeUnless { it == "NONE" },
                            recurrenceInterval = interval,
                            recurrenceWeekdays = if (repeatMode == "WEEKLY")
                                weekdayMask else null,
                            recurrenceMonthlyMode = if (repeatMode == "MONTHLY")
                                monthlyMode else null,
                            recurrenceUntilDate = untilDate,
                            reminderOffsetMinutes = offset, reminderLeadDays = lead,
                            reminderMinuteOfDay = minute,
                            reminderLocalId = if (reminderMode == "absolute")
                                initial?.reminderLocalId else null,
                            relativeReminderMessage = if (offset != null || lead != null)
                                reminderMessage else null,
                            priority = initial?.priority ?: 1,
                        )
                        val reminder = if (reminderMode == "absolute") {
                            val selected = LocalDate.ofEpochDay(absoluteDay)
                                .atTime(LocalTime.parse(absoluteClock))
                            require(!isNonexistentLocalTime(selected, zone))
                            ReminderEntity(
                                reminderLocalId = initialReminder?.reminderLocalId ?: 0L,
                                userLocalId = userId,
                                reminderTime = reminderEpochMillisForUpdate(
                                    selected, zone, initialReminder?.reminderTime,
                                    initial?.zoneId ?: zone.id, absoluteEdited,
                                ),
                                zoneId = zone.id, message = reminderMessage,
                            )
                        } else null
                        error = null
                        onSave(reminder, event)
                    } catch (failure: Exception) {
                        error = failure.message ?: "Check event fields"
                    }
                }) { Text("Save event") }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
    if (pickingDate != null) EventDatePickerDialog(
        onDateSelected = { selected ->
            when (pickingDate) {
                "start" -> {
                    startDay = selected.toEpochDay()
                    if (repeatMode == "WEEKLY") weekdayMask = weekdayMask or
                        (1 shl (selected.dayOfWeek.value - 1))
                    if (monthlyMode == "LAST_DAY" &&
                        selected.dayOfMonth != selected.lengthOfMonth()) {
                        monthlyMode = "DAY_OF_MONTH"
                    }
                    if (monthlyMode == "LAST_WEEKDAY" &&
                        selected.dayOfMonth + 7 <= selected.lengthOfMonth()) {
                        monthlyMode = "DAY_OF_MONTH"
                    }
                }
                "end" -> endDay = selected.toEpochDay()
                "until" -> untilDay = selected.toEpochDay()
                "absolute" -> {
                    absoluteDay = selected.toEpochDay()
                    absoluteEdited = true
                }
            }
        },
        onDismiss = { pickingDate = null },
    )
    if (pickingTime != null) EventTimePickerDialog(
        onTimeSelected = { selected ->
            when (pickingTime) {
                "start" -> startClock = selected.toString()
                "end" -> endClock = selected.toString()
                "relative" -> reminderClock = selected.toString()
                "absolute" -> {
                    absoluteClock = selected.toString()
                    absoluteEdited = true
                }
            }
        },
        onDismiss = { pickingTime = null },
    )
}
