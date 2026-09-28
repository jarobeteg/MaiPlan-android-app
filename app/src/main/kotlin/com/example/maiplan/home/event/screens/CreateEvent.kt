package com.example.maiplan.home.event.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.example.maiplan.R
import com.example.maiplan.database.entities.CategoryEntity
import com.example.maiplan.database.entities.EventEntity
import com.example.maiplan.database.entities.ReminderEntity
import com.example.maiplan.utils.common.UserSession
import com.example.maiplan.utils.toEpochMillis
import com.example.maiplan.viewmodel.category.CategoryViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

@Composable
fun CreateEventScreen(
    categoryViewModel: CategoryViewModel,
    onSaveClick: (ReminderEntity?, EventEntity) -> Unit,
    onBackClick: () -> Unit,
) {
    val userLocalId = UserSession.userLocalId ?: return
    val categories by categoryViewModel.categoryList.observeAsState(emptyList())
    val supportedZones = remember {
        ZoneId.getAvailableZoneIds()
            .filter { it == "UTC" || "/" in it }
            .toSet()
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf<LocalDate?>(null) }
    var startTime by remember { mutableStateOf<LocalTime?>(null) }
    var endTime by remember { mutableStateOf<LocalTime?>(null) }
    var zoneId by remember { mutableStateOf(
        ZoneId.systemDefault().id.takeIf { it in supportedZones } ?: "UTC"
    ) }
    var reminderDateTime by remember { mutableStateOf<LocalDateTime?>(null) }
    var reminderMessage by remember { mutableStateOf("") }

    val zone = ZoneId.of(zoneId)

    val blankTitleMessage = stringResource(R.string.blank_event_title)
    val blankDateMessage = stringResource(R.string.blank_event_date)
    val dateInPastMessage = stringResource(R.string.event_date_in_past)
    val blankStartTimeMessage = stringResource(R.string.blank_event_start_time)
    val blankEndTimeMessage = stringResource(R.string.blank_event_end_time)
    val invalidTimeRangeMessage = stringResource(R.string.event_end_time_before_start_time)
    val nonexistentStartTimeMessage = stringResource(R.string.event_start_time_nonexistent)
    val nonexistentEndTimeMessage = stringResource(R.string.event_end_time_nonexistent)
    val nonexistentReminderTimeMessage = stringResource(R.string.event_reminder_time_nonexistent)
    val blankCategoryMessage = stringResource(R.string.blank_event_category)

    EventEditorLayout(
        topBarTitle = stringResource(R.string.event_new),
        heading = stringResource(R.string.event_create_heading),
        subtitle = stringResource(R.string.event_create_subtitle),
        submitLabel = stringResource(R.string.event_save),
        state = EventEditorState(
            title = title,
            description = description,
            date = date,
            startTime = startTime,
            endTime = endTime,
            zoneId = zoneId,
            selectedCategory = selectedCategory,
            categories = categories,
            reminderDateTime = reminderDateTime,
            reminderMessage = reminderMessage,
            errorMessage = errorMessage,
        ),
        onTitleChange = { title = it },
        onDescriptionChange = { description = it },
        onDateChange = { date = it },
        onStartTimeChange = { startTime = it },
        onEndTimeChange = { endTime = it },
        onZoneChange = { zoneId = it },
        onCategoryChange = { selectedCategory = it },
        onReminderDateTimeChange = { reminderDateTime = it },
        onReminderMessageChange = { reminderMessage = it },
        onBackClick = onBackClick,
        onSubmit = {
            val validationMessage = when {
                title.isBlank() -> blankTitleMessage
                date == null -> blankDateMessage
                date!!.isBefore(LocalDate.now(zone)) -> dateInPastMessage
                startTime == null -> blankStartTimeMessage
                endTime == null -> blankEndTimeMessage
                !endTime!!.isAfter(startTime) -> invalidTimeRangeMessage
                isNonexistentLocalTime(date!!.atTime(startTime!!), zone) -> nonexistentStartTimeMessage
                isNonexistentLocalTime(date!!.atTime(endTime!!), zone) -> nonexistentEndTimeMessage
                reminderDateTime?.let { isNonexistentLocalTime(it, zone) } == true -> nonexistentReminderTimeMessage
                selectedCategory == null -> blankCategoryMessage
                else -> null
            }

            if (validationMessage != null) {
                errorMessage = validationMessage
            } else {
                errorMessage = null
                val reminder = reminderDateTime?.let {
                    ReminderEntity(
                        userLocalId = userLocalId,
                        reminderTime = it.withSecond(0).withNano(0).toEpochMillis(zone),
                        zoneId = zone.id,
                        message = reminderMessage,
                    )
                }
                val event = EventEntity(
                    userLocalId = userLocalId,
                    title = title.trim(),
                    categoryLocalId = selectedCategory!!.categoryLocalId,
                    description = description.trim(),
                    date = date!!.toEpochMillis(zone),
                    startTime = startTime!!.toEpochMillis(date!!, zone),
                    endTime = endTime!!.toEpochMillis(date!!, zone),
                    zoneId = zone.id,
                    priority = 1,
                    location = "",
                )

                onSaveClick(reminder, event)
            }
        },
    )
}
