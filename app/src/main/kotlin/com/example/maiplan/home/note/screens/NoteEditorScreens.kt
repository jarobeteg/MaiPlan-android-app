package com.example.maiplan.home.note.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.example.maiplan.R
import com.example.maiplan.database.entities.CategoryEntity
import com.example.maiplan.database.entities.NoteEntity
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.note.NoteSaveOutcome
import com.example.maiplan.utils.toLocalDateTime
import com.example.maiplan.viewmodel.note.NoteViewModel
import java.time.LocalDateTime

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
    val reminder by viewModel.selectedReminder.observeAsState()
    LaunchedEffect(note.reminderLocalId) {
        viewModel.loadNoteReminder(note.reminderLocalId)
    }
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
        initialReminderDateTime = reminder
            ?.takeIf { it.reminderLocalId == note.reminderLocalId }
            ?.reminderTime
            ?.toLocalDateTime(),
        initialReminderMessage = reminder
            ?.takeIf { it.reminderLocalId == note.reminderLocalId }
            ?.message
            .orEmpty(),
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
    var title by remember(initialTitle) { mutableStateOf(initialTitle) }
    var content by remember(initialContent) { mutableStateOf(initialContent) }
    var selectedCategory by remember(initialCategoryLocalId) { mutableStateOf<CategoryEntity?>(null) }
    var reminderDateTime by remember(initialReminderDateTime) {
        mutableStateOf(initialReminderDateTime)
    }
    var reminderMessage by remember(initialReminderMessage) {
        mutableStateOf(initialReminderMessage)
    }
    var localError by remember { mutableStateOf<String?>(null) }
    val titleRequiredMessage = stringResource(R.string.note_error_1)
    val reminderPastMessage = stringResource(R.string.note_reminder_past)

    LaunchedEffect(categories, initialCategoryLocalId) {
        if (initialCategoryLocalId != null && selectedCategory == null) {
            selectedCategory = categories.find { it.categoryLocalId == initialCategoryLocalId }
        }
    }

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
            reminderDateTime = reminderDateTime,
            reminderMessage = reminderMessage,
            errorMessage = localError ?: resultError,
            isLoading = saveResult is Result.Loading,
        ),
        onTitleChange = {
            title = it
            if (localError != null) localError = null
        },
        onContentChange = { content = it },
        onCategoryChange = { selectedCategory = it },
        onReminderDateTimeChange = { reminderDateTime = it },
        onReminderMessageChange = { reminderMessage = it },
        onReminderClear = {
            reminderDateTime = null
            reminderMessage = ""
        },
        onBackClick = onBackClick,
        onSubmit = {
            when {
                title.isBlank() -> localError = titleRequiredMessage
                reminderDateTime != null && !reminderDateTime!!.isAfter(LocalDateTime.now()) -> {
                    localError = reminderPastMessage
                }
                else -> {
                    localError = null
                    onSaveClick(
                        title.trim(),
                        content,
                        selectedCategory,
                        reminderDateTime,
                        reminderMessage,
                    )
                }
            }
        },
    )
}
