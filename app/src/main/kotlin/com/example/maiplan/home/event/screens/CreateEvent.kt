package com.example.maiplan.home.event.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.maiplan.viewmodel.category.CategoryViewModel
import com.example.maiplan.viewmodel.event.EventViewModel
import com.example.maiplan.viewmodel.reminder.ReminderViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Composable
fun CreateEventScreen(
    eventViewModel: EventViewModel,
    categoryViewModel: CategoryViewModel,
    reminderViewModel: ReminderViewModel,
    onSaveClick: (ReminderEntity?, EventEntity) -> Unit,
    onBackClick: () -> Unit,
) {
    val context = LocalContext.current
    val userId = UserSession.userId ?: return
    val categories by categoryViewModel.categoryList.observeAsState(emptyList())

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf<LocalDate?>(null) }
    var startTime by remember { mutableStateOf<LocalTime?>(null) }
    var endTime by remember { mutableStateOf<LocalTime?>(null) }
    var reminderDateTime by remember { mutableStateOf<LocalDateTime?>(null) }
    var reminderMessage by remember { mutableStateOf("") }

    val blankTitleMessage = stringResource(R.string.blank_event_title)
    val blankDateMessage = stringResource(R.string.blank_event_date)
    val dateInPastMessage = stringResource(R.string.event_date_in_past)
    val blankStartTimeMessage = stringResource(R.string.blank_event_start_time)
    val blankEndTimeMessage = stringResource(R.string.blank_event_end_time)
    val invalidTimeRangeMessage = stringResource(R.string.event_end_time_before_start_time)
    val blankCategoryMessage = stringResource(R.string.blank_event_category)

    LaunchedEffect(userId) {
        categoryViewModel.getAllCategories(userId)
    }

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
                date == null -> blankDateMessage
                date!!.isBefore(LocalDate.now()) -> dateInPastMessage
                startTime == null -> blankStartTimeMessage
                endTime == null -> blankEndTimeMessage
                endTime!!.isBefore(startTime) -> invalidTimeRangeMessage
                selectedCategory == null -> blankCategoryMessage
                else -> null
            }

            if (validationMessage != null) {
                errorMessage = validationMessage
            } else {
                errorMessage = null
                val reminder = reminderDateTime?.let {
                    ReminderEntity(
                        userId = userId,
                        reminderTime = it.withSecond(0).withNano(0).toEpochMillis(),
                        message = reminderMessage,
                        syncState = 4,
                    )
                }
                val event = EventEntity(
                    userId = userId,
                    title = title.trim(),
                    categoryId = selectedCategory!!.categoryId,
                    description = description.trim(),
                    date = date!!.toEpochMillis(),
                    startTime = startTime!!.toEpochMillis(date!!),
                    endTime = endTime!!.toEpochMillis(date!!),
                    priority = 1,
                    location = "",
                    syncState = 4,
                )

                onSaveClick(reminder, event)
                reminder?.let {
                    val reminderData = ReminderData(
                        reminderId = it.reminderId,
                        reminderTime = it.reminderTime,
                        reminderTitle = event.title,
                        reminderMessage = it.message.orEmpty(),
                    )
                    if (!AlarmScheduler.attemptSchedule(context, reminderData)) {
                        AlarmScheduler.requestExactAlarmPermission(context)
                    }
                }
            }
        },
    )
}
