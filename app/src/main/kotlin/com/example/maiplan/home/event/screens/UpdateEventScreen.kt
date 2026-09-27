package com.example.maiplan.home.event.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.example.maiplan.utils.toLocalDateTime
import com.example.maiplan.viewmodel.category.CategoryViewModel
import com.example.maiplan.viewmodel.event.EventViewModel
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun UpdateEventScreen(
    eventLocalId: Long,
    eventViewModel: EventViewModel,
    categoryViewModel: CategoryViewModel,
    onUpdateClick: (ReminderEntity?, EventEntity) -> Unit,
    onBackClick: () -> Unit,
) {
    val event by eventViewModel.getEventById(eventLocalId).collectAsState()
    val safeEvent = event ?: return
    val userLocalId = UserSession.userLocalId ?: return
    val categories by categoryViewModel.categoryList.observeAsState(emptyList())

    var errorMessage by remember(safeEvent.eventLocalId) { mutableStateOf<String?>(null) }
    var selectedCategory by remember(safeEvent.eventLocalId) { mutableStateOf<CategoryEntity?>(null) }
    var title by remember(safeEvent.eventLocalId) { mutableStateOf(safeEvent.title) }
    var description by remember(safeEvent.eventLocalId) { mutableStateOf(safeEvent.description) }
    var date by remember(safeEvent.eventLocalId) { mutableStateOf(safeEvent.date) }
    var startTime by remember(safeEvent.eventLocalId) { mutableStateOf(safeEvent.startTime) }
    var endTime by remember(safeEvent.eventLocalId) { mutableStateOf(safeEvent.endTime) }
    var zoneId by remember { mutableStateOf(safeEvent.zoneId) }
    var reminderDateTime by remember(safeEvent.eventLocalId, safeEvent.zoneId) {
        mutableStateOf(safeEvent.reminderTime?.toLocalDateTime(ZoneId.of(safeEvent.zoneId)))
    }
    var reminderMessage by remember(safeEvent.eventLocalId) { mutableStateOf(safeEvent.reminderMessage) }

    val zone = ZoneId.of(zoneId)

    val blankTitleMessage = stringResource(R.string.blank_event_title)
    val dateInPastMessage = stringResource(R.string.event_date_in_past)
    val invalidTimeRangeMessage = stringResource(R.string.event_end_time_before_start_time)
    val blankCategoryMessage = stringResource(R.string.blank_event_category)

    LaunchedEffect(categories, safeEvent.categoryLocalId) {
        selectedCategory = categories.find { it.categoryLocalId == safeEvent.categoryLocalId }
    }

    EventEditorLayout(
        topBarTitle = stringResource(R.string.event_update),
        heading = stringResource(R.string.event_update_heading),
        subtitle = stringResource(R.string.event_update_subtitle),
        submitLabel = stringResource(R.string.update),
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
                date.isBefore(LocalDate.now(zone)) -> dateInPastMessage
                endTime.isBefore(startTime) -> invalidTimeRangeMessage
                selectedCategory == null -> blankCategoryMessage
                else -> null
            }

            if (validationMessage != null) {
                errorMessage = validationMessage
            } else {
                errorMessage = null
                val reminder = reminderDateTime?.let {
                    ReminderEntity(
                        reminderLocalId = safeEvent.reminderLocalId ?: 0L,
                        userLocalId = userLocalId,
                        reminderTime = it.withSecond(0).withNano(0).toEpochMillis(zone),
                        zoneId = zone.id,
                        message = reminderMessage,
                    )
                }
                val updatedEvent = EventEntity(
                    eventLocalId = safeEvent.eventLocalId,
                    userLocalId = userLocalId,
                    title = title.trim(),
                    categoryLocalId = selectedCategory!!.categoryLocalId,
                    reminderLocalId = safeEvent.reminderLocalId,
                    description = description.trim(),
                    date = date.toEpochMillis(zone),
                    startTime = startTime.toEpochMillis(date, zone),
                    endTime = endTime.toEpochMillis(date, zone),
                    zoneId = zone.id,
                    priority = 1,
                    location = "",
                )

                onUpdateClick(reminder, updatedEvent)
            }
        },
    )
}
