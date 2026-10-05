package com.example.maiplan.home.note.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.maiplan.R
import com.example.maiplan.database.entities.CategoryEntity
import com.example.maiplan.database.entities.NoteEntity
import com.example.maiplan.database.entities.ReminderEntity
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.note.NoteSaveOutcome
import com.example.maiplan.home.event.screens.isNonexistentLocalTime
import com.example.maiplan.utils.toEpochMillis
import com.example.maiplan.utils.toLocalDateTime
import com.example.maiplan.viewmodel.note.NoteViewModel
import java.time.LocalDateTime
import java.time.ZoneId

private val ReminderDateTimeSaver = Saver<LocalDateTime, String>(
    save = { it.toString() },
    restore = { LocalDateTime.parse(it) },
)

@Composable
fun CreateNoteScreen(
    viewModel: NoteViewModel,
    onSaveClick: (String, String, CategoryEntity?, LocalDateTime?, String) -> Unit,
    onBackClick: () -> Unit,
) {
    NoteEditorScreen(
        viewModel = viewModel,
        topBarTitle = stringResource(R.string.note_new),
        heading = stringResource(R.string.note_create_heading),
        subtitle = stringResource(R.string.note_create_subtitle),
        buttonText = stringResource(R.string.note_save),
        saveResult = viewModel.createNoteResult.observeAsState().value,
        initialTitle = "",
        initialContent = "",
        initialCategoryLocalId = null,
        initialReminderDateTime = null,
        initialReminderMessage = "",
        onSaveClick = onSaveClick,
        onBackClick = onBackClick,
    )
}

@Composable
fun UpdateNoteScreen(
    viewModel: NoteViewModel,
    note: NoteEntity,
    onSaveClick: (String, String, CategoryEntity?, LocalDateTime?, String) -> Unit,
    onBackClick: () -> Unit,
) {
    var reminderResult by remember(note.noteLocalId, note.reminderLocalId) {
        mutableStateOf<Result<ReminderEntity?>>(Result.Loading)
    }
    var loadAttempt by remember { mutableStateOf(0) }
    LaunchedEffect(note.noteLocalId, note.reminderLocalId, loadAttempt) {
        reminderResult = Result.Loading
        reminderResult = viewModel.getNoteReminder(note.reminderLocalId, note.userLocalId)
    }
    val loadedReminder = reminderResult as? Result.Success<ReminderEntity?>
    if (loadedReminder == null) {
        NoteScreenBackground {
            Scaffold(
                topBar = { NoteEditorTopBar(stringResource(R.string.note_update), onBackClick) },
                containerColor = Color.Transparent,
            ) { padding ->
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    if (reminderResult is Result.Loading) {
                        CircularProgressIndicator()
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stringResource(R.string.note_reminder_load_error))
                            TextButton(onClick = { loadAttempt++ }) {
                                Text(stringResource(R.string.note_reminder_retry))
                            }
                        }
                    }
                }
            }
        }
        return
    }
    val reminder = loadedReminder.data
    NoteEditorScreen(
        viewModel = viewModel,
        topBarTitle = stringResource(R.string.note_update),
        heading = stringResource(R.string.note_update_heading),
        subtitle = stringResource(R.string.note_update_subtitle),
        buttonText = stringResource(R.string.update),
        saveResult = viewModel.updateNoteResult.observeAsState().value,
        initialTitle = note.title,
        initialContent = note.content.orEmpty(),
        initialCategoryLocalId = note.categoryLocalId,
        initialReminderDateTime = reminder?.reminderTime?.toLocalDateTime(),
        initialReminderMessage = reminder?.message.orEmpty(),
        onSaveClick = onSaveClick,
        onBackClick = onBackClick,
    )
}

@Composable
private fun NoteEditorScreen(
    viewModel: NoteViewModel,
    topBarTitle: String,
    heading: String,
    subtitle: String,
    buttonText: String,
    saveResult: Result<NoteSaveOutcome>?,
    initialTitle: String,
    initialContent: String,
    initialCategoryLocalId: Long?,
    initialReminderDateTime: LocalDateTime?,
    initialReminderMessage: String,
    onSaveClick: (String, String, CategoryEntity?, LocalDateTime?, String) -> Unit,
    onBackClick: () -> Unit,
) {
    val categories by viewModel.categoryList.observeAsState(emptyList())
    var title by rememberSaveable(initialTitle) { mutableStateOf(initialTitle) }
    var content by rememberSaveable(initialContent) { mutableStateOf(initialContent) }
    var selectedCategoryId by rememberSaveable(initialCategoryLocalId) { mutableStateOf(initialCategoryLocalId) }
    val selectedCategory = categories.find { it.categoryLocalId == selectedCategoryId }
    var reminderDateTime by rememberSaveable(initialReminderDateTime, stateSaver = ReminderDateTimeSaver) {
        mutableStateOf(initialReminderDateTime ?: LocalDateTime.now().plusHours(1).withMinute(0).withSecond(0).withNano(0))
    }
    var reminderEnabled by rememberSaveable(initialReminderDateTime) { mutableStateOf(initialReminderDateTime != null) }
    var reminderMessage by rememberSaveable(initialReminderMessage) {
        mutableStateOf(initialReminderMessage)
    }
    var localError by rememberSaveable { mutableStateOf<String?>(null) }
    val titleRequiredMessage = stringResource(R.string.note_error_1)
    val reminderPastMessage = stringResource(R.string.note_reminder_past)
    val reminderNonexistentMessage = stringResource(R.string.event_reminder_time_nonexistent)

    val resultError = when (saveResult) {
        is Result.Failure -> when (saveResult.errorCode) {
            1 -> stringResource(R.string.note_error_1)
            else -> stringResource(R.string.unknown_error)
        }
        is Result.Error -> stringResource(R.string.note_error_save)
        else -> null
    }

    NoteEditorLayout(
        topBarTitle = topBarTitle,
        heading = heading,
        subtitle = subtitle,
        submitLabel = buttonText,
        state = NoteEditorState(
            title = title,
            content = content,
            selectedCategory = selectedCategory,
            categories = categories,
            reminderDateTime = if (reminderEnabled) reminderDateTime else null,
            reminderMessage = reminderMessage,
            errorMessage = localError ?: resultError,
            isLoading = saveResult is Result.Loading,
        ),
        onTitleChange = {
            title = it
            if (localError != null) localError = null
        },
        onContentChange = { content = it },
        onCategoryChange = { selectedCategoryId = it?.categoryLocalId },
        onReminderDateTimeChange = { reminderDateTime = it },
        onReminderMessageChange = { reminderMessage = it },
        onReminderEnabledChange = { reminderEnabled = it },
        onBackClick = onBackClick,
        onSubmit = {
            when {
                title.isBlank() -> localError = titleRequiredMessage
                reminderEnabled && isNonexistentLocalTime(reminderDateTime, ZoneId.systemDefault()) -> {
                    localError = reminderNonexistentMessage
                }
                reminderEnabled && reminderDateTime.withSecond(0).withNano(0).toEpochMillis() <= System.currentTimeMillis() -> {
                    localError = reminderPastMessage
                }
                else -> {
                    localError = null
                    onSaveClick(
                        title.trim(),
                        content,
                        selectedCategory,
                        if (reminderEnabled) reminderDateTime else null,
                        reminderMessage,
                    )
                }
            }
        },
    )
}
