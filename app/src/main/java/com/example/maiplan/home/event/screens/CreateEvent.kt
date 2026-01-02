package com.example.maiplan.home.event.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.maiplan.components.SimpleTopBar
import com.example.maiplan.R
import com.example.maiplan.components.AdjustableTextFieldLengthComponent
import com.example.maiplan.components.CategoryDropdownComponent
import com.example.maiplan.components.DateInputComponent
import com.example.maiplan.components.LocalDateTimeInputField
import com.example.maiplan.components.PriorityDropdown
import com.example.maiplan.components.SectionTitle
import com.example.maiplan.components.SubmitButtonComponent
import com.example.maiplan.components.TimeInputComponent
import com.example.maiplan.database.entities.EventEntity
import com.example.maiplan.database.entities.ReminderEntity
import com.example.maiplan.network.api.CategoryResponse
import com.example.maiplan.utils.common.UserSession
import com.example.maiplan.utils.toEpochMillis
import com.example.maiplan.viewmodel.category.CategoryViewModel
import com.example.maiplan.viewmodel.event.EventViewModel
import com.example.maiplan.viewmodel.reminder.ReminderViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun CreateEventScreen(
    eventViewModel: EventViewModel,
    categoryViewModel: CategoryViewModel,
    reminderViewModel: ReminderViewModel,
    onSaveClick: (ReminderEntity?, EventEntity) -> Unit,
    onBackClick: () -> Unit
) {
    categoryViewModel.getAllCategories(UserSession.userId!!)
    val categories by categoryViewModel.categoryList.observeAsState(emptyList())
    var selectedCategory by remember { mutableStateOf<CategoryResponse?>(null) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf<LocalDate?>(null) }
    var startTime by remember { mutableStateOf<LocalTime?>(null) }
    var endTime by remember { mutableStateOf<LocalTime?>(null) }
    var priority by remember { mutableIntStateOf(1) }
    var location by remember { mutableStateOf("") }
    var dateTime by remember { mutableStateOf<LocalDateTime?>(null) }
    var message by remember { mutableStateOf("") }

    Scaffold (
        topBar = {
            SimpleTopBar(
                text = stringResource(R.string.event_new),
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AdjustableTextFieldLengthComponent(title, stringResource(R.string.title), Icons.Filled.Title, 255) { title = it }

            AdjustableTextFieldLengthComponent(description, stringResource(R.string.description), Icons.Filled.Description, 512) { description = it }

            DateInputComponent(stringResource(R.string.date), date)  { date = it }

            TimeInputComponent(stringResource(R.string.start_time), startTime) { startTime = it }

            TimeInputComponent(stringResource(R.string.end_time), endTime) { endTime = it }

            PriorityDropdown(priority) { priority = it }

            AdjustableTextFieldLengthComponent(location, stringResource(R.string.location), Icons.Filled.LocationOn, 255) { location = it }

            SectionTitle(stringResource(R.string.category))

            CategoryDropdownComponent(categories, selectedCategory) { selectedCategory = it }

            SectionTitle(stringResource(R.string.reminder))

            LocalDateTimeInputField(stringResource(R.string.date_time), dateTime) { dateTime = it }

            AdjustableTextFieldLengthComponent(message, stringResource(R.string.message), Icons.AutoMirrored.Filled.Message, 512) { message = it }

            SubmitButtonComponent(stringResource(R.string.event_save), onButtonClicked = {
                var reminder: ReminderEntity? = null
                dateTime?.let {
                    reminder = ReminderEntity(
                        userId = UserSession.userId!!,
                        reminderTime = it.toEpochMillis(),
                        message = message,
                        syncState = 4
                    )
                }

                val event = EventEntity(
                    userId = UserSession.userId!!,
                    title = title,
                    description = description,
                    date = date?.toEpochDay() ?: LocalDate.now().toEpochDay(),
                    startTime = startTime?.toNanoOfDay(),
                    endTime = endTime?.toNanoOfDay(),
                    priority = priority,
                    location = location,
                    syncState = 4
                    )
                onSaveClick(reminder, event)
            })
        }
    }
}