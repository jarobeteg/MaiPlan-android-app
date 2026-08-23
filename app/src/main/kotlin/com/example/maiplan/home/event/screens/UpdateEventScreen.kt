package com.example.maiplan.home.event.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.maiplan.R
import com.example.maiplan.database.entities.CategoryEntity
import com.example.maiplan.database.entities.EventEntity
import com.example.maiplan.database.entities.ReminderEntity
import com.example.maiplan.utils.common.UserSession
import com.example.maiplan.utils.notifications.AlarmScheduler
import com.example.maiplan.utils.notifications.ReminderData
import com.example.maiplan.utils.toEpochMillis
import com.example.maiplan.utils.toLocalDateTime
import com.example.maiplan.viewmodel.category.CategoryViewModel
import com.example.maiplan.viewmodel.event.EventViewModel
import com.example.maiplan.viewmodel.reminder.ReminderViewModel
import java.time.LocalDate

@Composable
fun UpdateEventScreen(
    eventId: Int,
    eventViewModel: EventViewModel,
    categoryViewModel: CategoryViewModel,
    reminderViewModel: ReminderViewModel,
    onUpdateClick: (ReminderEntity?, EventEntity) -> Unit,
    onBackClick: () -> Unit,
) {
    val event by eventViewModel.getEventById(eventId).collectAsState()
    val safeEvent = event ?: return
    val context = LocalContext.current
    val userId = UserSession.userId ?: return
    val categories by categoryViewModel.categoryList.observeAsState(emptyList())

    var errorMessage by remember(safeEvent.eventId) { mutableStateOf<String?>(null) }
    var selectedCategory by remember(safeEvent.eventId) { mutableStateOf<CategoryEntity?>(null) }
    var title by remember(safeEvent.eventId) { mutableStateOf(safeEvent.title) }
    var description by remember(safeEvent.eventId) { mutableStateOf(safeEvent.description) }
    var date by remember(safeEvent.eventId) { mutableStateOf(safeEvent.date) }
    var startTime by remember(safeEvent.eventId) { mutableStateOf(safeEvent.startTime) }
    var endTime by remember(safeEvent.eventId) { mutableStateOf(safeEvent.endTime) }
    var reminderDateTime by remember(safeEvent.eventId) {
        mutableStateOf(safeEvent.reminderTime.takeIf { it != 0L }?.toLocalDateTime())
    }
    var reminderMessage by remember(safeEvent.eventId) { mutableStateOf(safeEvent.reminderMessage) }

    val blankTitleMessage = stringResource(R.string.blank_event_title)
    val dateInPastMessage = stringResource(R.string.event_date_in_past)
    val invalidTimeRangeMessage = stringResource(R.string.event_end_time_before_start_time)
    val blankCategoryMessage = stringResource(R.string.blank_event_category)

    LaunchedEffect(userId) {
        categoryViewModel.getAllCategories(userId)
    }
    LaunchedEffect(categories, safeEvent.categoryId) {
        selectedCategory = categories.find { it.categoryId == safeEvent.categoryId }
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
        onCategoryChange = { selectedCategory = it },
        onReminderDateTimeChange = { reminderDateTime = it },
        onReminderMessageChange = { reminderMessage = it },
        onBackClick = onBackClick,
        onSubmit = {
            val validationMessage = when {
                title.isBlank() -> blankTitleMessage
                date.isBefore(LocalDate.now()) -> dateInPastMessage
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
                        reminderId = safeEvent.reminderId.takeIf { id -> id != 0 } ?: 0,
                        userId = userId,
                        reminderTime = it.withSecond(0).withNano(0).toEpochMillis(),
                        message = reminderMessage,
                        syncState = 2,
                    )
                }
                val updatedEvent = EventEntity(
                    eventId = safeEvent.eventId,
                    userId = userId,
                    title = title.trim(),
                    categoryId = selectedCategory!!.categoryId,
                    reminderId = safeEvent.reminderId.takeIf { it != 0 },
                    description = description.trim(),
                    date = date.toEpochMillis(),
                    startTime = startTime.toEpochMillis(date),
                    endTime = endTime.toEpochMillis(date),
                    priority = 1,
                    location = "",
                    syncState = 2,
                )

                onUpdateClick(reminder, updatedEvent)
                reminder?.let {
                    AlarmScheduler.scheduleAlarm(
                        context,
                        ReminderData(
                            reminderId = it.reminderId,
                            reminderTime = it.reminderTime,
                            reminderTitle = updatedEvent.title,
                            reminderMessage = it.message.orEmpty(),
                        ),
                    )
                }
            }
        },
    )
}
