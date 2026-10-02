package com.example.maiplan.home.event.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Message
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Title
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.maiplan.R
import com.example.maiplan.database.entities.CategoryEntity
import com.example.maiplan.database.entities.EventEntity
import com.example.maiplan.database.entities.ReminderEntity
import com.example.maiplan.repository.event.resolveLocal
import com.example.maiplan.repository.event.validateEventDefinition
import com.example.maiplan.theme.LocalAppDarkTheme
import com.example.maiplan.utils.common.UserSession
import java.time.*
import java.time.format.DateTimeFormatter

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
    var pickingZone by remember { mutableStateOf(false) }
    BackHandler(enabled = pickingDate != null || pickingTime != null || pickingZone) {
        pickingDate = null
        pickingTime = null
        pickingZone = false
    }
    val dateFormat = remember { DateTimeFormatter.ofPattern("EEE, MMM d, yyyy") }
    val previewState = EventEditorState(
        title = title,
        description = description,
        date = LocalDate.ofEpochDay(startDay),
        startTime = if (timed) runCatching { LocalTime.parse(startClock) }.getOrNull() else null,
        endTime = if (timed) runCatching { LocalTime.parse(endClock) }.getOrNull() else null,
        zoneId = zoneText,
        selectedCategory = categories.firstOrNull { it.categoryLocalId == categoryId },
        categories = categories,
        reminderDateTime = null,
        reminderMessage = reminderMessage,
        errorMessage = error,
    )

    EventScreenBackground {
        Scaffold(
            topBar = { EventEditorTopBar(heading, onBack) },
            containerColor = Color.Transparent,
        ) { padding ->
            Column(
                Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(
                    modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    EventEditorHeading(
                        heading = stringResource(if (initial == null) R.string.event_create_heading else R.string.event_update_heading),
                        subtitle = stringResource(if (initial == null) R.string.event_create_subtitle else R.string.event_update_subtitle),
                    )
                    EventPreview(previewState)

                    EventEditorSection(
                        title = stringResource(R.string.event_details_title),
                        subtitle = stringResource(R.string.event_details_subtitle),
                    ) {
                        EventEditorTextField(
                            value = title,
                            onValueChange = { title = it.take(255) },
                            label = stringResource(R.string.title),
                            icon = Icons.Rounded.Title,
                            singleLine = true,
                            imeAction = ImeAction.Next,
                        )
                        Spacer(Modifier.height(14.dp))
                        EventEditorTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = stringResource(R.string.description),
                            icon = Icons.Rounded.Description,
                            singleLine = false,
                            imeAction = ImeAction.Default,
                        )
                    }

                    EventEditorSection(
                        title = stringResource(R.string.event_schedule_title),
                        subtitle = stringResource(R.string.event_schedule_subtitle),
                    ) {
                        EventSelectionField(
                            label = "Start date",
                            value = LocalDate.ofEpochDay(startDay).format(dateFormat),
                            icon = Icons.Rounded.CalendarMonth,
                            onClick = { pickingDate = "start" },
                        )
                        Spacer(Modifier.height(12.dp))
                        EventSelectionField(
                            label = if (repeatMode == "NONE") "End date" else "End date of each occurrence",
                            value = endDay?.let { LocalDate.ofEpochDay(it).format(dateFormat) } ?: "Same day",
                            icon = Icons.Rounded.CalendarMonth,
                            onClick = { pickingDate = "end" },
                        )
                        if (repeatMode != "NONE") {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "This is when each occurrence finishes. Use Repeat until to choose the last repeat.",
                                style = MaterialTheme.typography.bodySmall,
                                color = eventEditorMuted(),
                            )
                            if (endDay?.let { it > startDay } == true) {
                                TextButton(onClick = {
                                    untilDay = endDay
                                    endDay = null
                                }) {
                                    Text("Use end date as Repeat until")
                                }
                            }
                        }
                        if (endDay != null) {
                            TextButton(onClick = { endDay = null }) { Text("Clear end date") }
                        } else Spacer(Modifier.height(12.dp))
                        EventSelectionField(
                            label = stringResource(R.string.time_zone),
                            value = zoneText,
                            icon = Icons.Rounded.Public,
                            onClick = { pickingZone = true },
                        )
                        Spacer(Modifier.height(12.dp))
                        EventEditorToggle("Include time", timed) { timed = it }
                        if (timed) {
                            Spacer(Modifier.height(12.dp))
                            BoxWithConstraints {
                                if (maxWidth >= 420.dp) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Box(Modifier.weight(1f)) {
                                            EventSelectionField("Start time", startClock, Icons.Rounded.AccessTime) { pickingTime = "start" }
                                        }
                                        Box(Modifier.weight(1f)) {
                                            EventSelectionField("End time", endClock, Icons.Rounded.AccessTime) { pickingTime = "end" }
                                        }
                                    }
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        EventSelectionField("Start time", startClock, Icons.Rounded.AccessTime) { pickingTime = "start" }
                                        EventSelectionField("End time", endClock, Icons.Rounded.AccessTime) { pickingTime = "end" }
                                    }
                                }
                            }
                        }
                    }

                    EventEditorSection(
                        title = stringResource(R.string.event_organization_title),
                        subtitle = stringResource(R.string.event_organization_subtitle),
                    ) {
                        EventCategoryDropdown(
                            categories = categories,
                            selectedCategory = previewState.selectedCategory,
                            onCategorySelected = { categoryId = it?.categoryLocalId },
                        )
                    }

                    EventEditorSection(title = "Repeat", subtitle = "Choose how often this event occurs.") {
                        EventEditorLabel("Frequency")
                        EventOptionRow(listOf("NONE", "DAILY", "WEEKLY", "MONTHLY"), repeatMode,
                            { it.lowercase().replaceFirstChar(Char::uppercase) }) { mode ->
                            repeatMode = mode
                            if (mode == "WEEKLY") weekdayMask = weekdayMask or
                                (1 shl (LocalDate.ofEpochDay(startDay).dayOfWeek.value - 1))
                        }
                        if (repeatMode != "NONE") {
                            Spacer(Modifier.height(14.dp))
                            val unit = when (repeatMode) {
                                "DAILY" -> "days"
                                "WEEKLY" -> "weeks"
                                else -> "months"
                            }
                            EventEditorTextField(
                                value = intervalText,
                                onValueChange = { intervalText = it.filter(Char::isDigit) },
                                label = "Every number of $unit",
                                icon = Icons.Rounded.Repeat,
                                singleLine = true,
                                imeAction = ImeAction.Done,
                                keyboardType = KeyboardType.Number,
                            )
                            if (repeatMode == "WEEKLY") {
                                Spacer(Modifier.height(14.dp))
                                EventEditorLabel("Repeat on")
                                EventOptionRow(DayOfWeek.values().toList(), null,
                                    { it.name.take(3).lowercase().replaceFirstChar(Char::uppercase) },
                                    isSelected = { day -> weekdayMask and (1 shl (day.value - 1)) != 0 },
                                ) { day ->
                                    weekdayMask = weekdayMask xor (1 shl (day.value - 1))
                                }
                            }
                            if (repeatMode == "MONTHLY") {
                                Spacer(Modifier.height(14.dp))
                                val anchorDate = LocalDate.ofEpochDay(startDay)
                                val weekday = anchorDate.dayOfWeek.name.lowercase()
                                    .replaceFirstChar(Char::uppercase)
                                val ordinal = (anchorDate.dayOfMonth - 1) / 7 + 1
                                EventEditorLabel("Monthly pattern")
                                EventOptionRow(
                                    listOf(
                                        "DAY_OF_MONTH" to "Day ${anchorDate.dayOfMonth}",
                                        "LAST_DAY" to "Last day",
                                        "NTH_WEEKDAY" to "$ordinal $weekday",
                                        "LAST_WEEKDAY" to "Last $weekday",
                                    ),
                                    null,
                                    { it.second },
                                    isSelected = { it.first == monthlyMode },
                                    isEnabled = { (mode, _) ->
                                        when (mode) {
                                            "LAST_DAY" -> anchorDate.dayOfMonth == anchorDate.lengthOfMonth()
                                            "LAST_WEEKDAY" -> anchorDate.dayOfMonth + 7 > anchorDate.lengthOfMonth()
                                            else -> true
                                        }
                                    },
                                ) { monthlyMode = it.first }
                                Text("A missing day or fifth weekday skips that month.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = eventEditorMuted())
                            }
                            Spacer(Modifier.height(14.dp))
                            EventSelectionField(
                                label = "Repeat until (last start date)",
                                value = untilDay?.let { LocalDate.ofEpochDay(it).format(dateFormat) } ?: "No end date",
                                icon = Icons.Rounded.CalendarMonth,
                                onClick = { pickingDate = "until" },
                            )
                            if (untilDay != null) TextButton(onClick = { untilDay = null }) {
                                Text("Clear repeat end")
                            }
                        }
                    }

                    EventEditorSection(
                        title = stringResource(R.string.event_reminder_title),
                        subtitle = stringResource(R.string.event_reminder_subtitle),
                    ) {
                        EventOptionRow(listOf("none", "relative", "absolute"), reminderMode,
                            { when (it) { "relative" -> "Before event"; "absolute" -> "At a date"; else -> "None" } },
                        ) { reminderMode = it }
                        if (reminderMode == "relative") {
                            Spacer(Modifier.height(14.dp))
                            if (timed) {
                                EventEditorTextField(offsetText,
                                    { offsetText = it.filter(Char::isDigit) },
                                    "Minutes before start", Icons.Rounded.Notifications, true,
                                    ImeAction.Done, KeyboardType.Number)
                            } else {
                                EventEditorTextField(leadText,
                                    { leadText = it.filter(Char::isDigit) },
                                    "Calendar days before", Icons.Rounded.Notifications, true,
                                    ImeAction.Done, KeyboardType.Number)
                                Spacer(Modifier.height(12.dp))
                                EventSelectionField("Reminder time", reminderClock,
                                    Icons.Rounded.AccessTime) { pickingTime = "relative" }
                            }
                        }
                        if (reminderMode == "absolute") {
                            Spacer(Modifier.height(14.dp))
                            EventSelectionField("Reminder date",
                                LocalDate.ofEpochDay(absoluteDay).format(dateFormat),
                                Icons.Rounded.CalendarMonth) { pickingDate = "absolute" }
                            Spacer(Modifier.height(12.dp))
                            EventSelectionField("Reminder time", absoluteClock,
                                Icons.Rounded.AccessTime) { pickingTime = "absolute" }
                        }
                        if (reminderMode != "none") {
                            Spacer(Modifier.height(14.dp))
                            EventEditorTextField(reminderMessage, { reminderMessage = it },
                                "Reminder message", Icons.AutoMirrored.Rounded.Message,
                                false, ImeAction.Default)
                        }
                    }

                    error?.let { EventEditorError(it) }
                    EventEditorButton(stringResource(R.string.event_save)) {
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
                            validateEventDefinition(event)
                            error = null
                            onSave(reminder, event)
                        } catch (failure: Exception) {
                            error = failure.message ?: "Check event fields"
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
        if (pickingDate != null) EventDatePickerOverlay(
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
        if (pickingTime != null) EventTimePickerOverlay(
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
        if (pickingZone) ZonePickerOverlay(
            selectedZone = zoneText,
            onZoneSelected = { zoneText = it; pickingZone = false },
            onDismiss = { pickingZone = false },
        )
    }
}

@Composable
private fun EventEditorLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = eventEditorMuted(),
    )
    Spacer(Modifier.height(6.dp))
}

@Composable
private fun eventEditorMuted(): Color =
    if (LocalAppDarkTheme.current) Color(0xFFAEB7C9) else EventMuted

@Composable
private fun EventEditorToggle(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val dark = LocalAppDarkTheme.current
    Surface(
        onClick = { onCheckedChange(!checked) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        color = if (dark) Color(0xFF20263A) else Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, if (dark) Color(0xFF3A435C) else EventBorder),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (dark) Color(0xFFF5F7FB) else EventInk,
            )
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun <T> EventOptionRow(
    options: List<T>,
    selected: T?,
    label: (T) -> String,
    isSelected: (T) -> Boolean = { it == selected },
    isEnabled: (T) -> Boolean = { true },
    onSelect: (T) -> Unit,
) {
    val dark = LocalAppDarkTheme.current
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { option ->
            val active = isSelected(option)
            val enabled = isEnabled(option)
            FilterChip(
                selected = active,
                enabled = enabled,
                onClick = { onSelect(option) },
                label = {
                    Text(
                        label(option),
                        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                    )
                },
                shape = RoundedCornerShape(13.dp),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = enabled,
                    selected = active,
                    borderColor = if (dark) Color(0xFF3A435C) else EventBorder,
                    selectedBorderColor = EventPrimary,
                ),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = if (dark) Color(0xFF20263A) else Color(0xFFF8FAFC),
                    labelColor = eventEditorMuted(),
                    selectedContainerColor = EventPrimary,
                    selectedLabelColor = Color.White,
                ),
            )
        }
    }
}
