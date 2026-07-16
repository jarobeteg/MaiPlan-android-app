package com.example.maiplan.home.note.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.maiplan.R
import com.example.maiplan.components.AdjustableTextFieldLengthComponent
import com.example.maiplan.components.CategoryDropdownComponent
import com.example.maiplan.components.ErrorMessageComponent
import com.example.maiplan.components.SimpleTopBar
import com.example.maiplan.components.SubmitButtonComponent
import com.example.maiplan.database.entities.CategoryEntity
import com.example.maiplan.database.entities.NoteEntity
import com.example.maiplan.repository.Result
import com.example.maiplan.utils.LocalUiScale
import com.example.maiplan.viewmodel.note.NoteViewModel

@Composable
fun CreateNoteScreen(
    viewModel: NoteViewModel,
    onSaveClick: (String, String, CategoryEntity?) -> Unit,
    onBackClick: () -> Unit
) {
    NoteEditorScreen(
        viewModel = viewModel,
        topBarTitle = stringResource(R.string.note_new),
        buttonText = stringResource(R.string.note_save),
        saveResult = viewModel.createNoteResult.observeAsState().value,
        initialTitle = "",
        initialContent = "",
        initialCategoryId = null,
        onSaveClick = onSaveClick,
        onBackClick = onBackClick
    )
}

@Composable
fun UpdateNoteScreen(
    viewModel: NoteViewModel,
    note: NoteEntity,
    onSaveClick: (String, String, CategoryEntity?) -> Unit,
    onBackClick: () -> Unit
) {
    NoteEditorScreen(
        viewModel = viewModel,
        topBarTitle = stringResource(R.string.note_update),
        buttonText = stringResource(R.string.update),
        saveResult = viewModel.updateNoteResult.observeAsState().value,
        initialTitle = note.title,
        initialContent = note.content.orEmpty(),
        initialCategoryId = note.categoryId,
        onSaveClick = onSaveClick,
        onBackClick = onBackClick
    )
}

@Composable
private fun NoteEditorScreen(
    viewModel: NoteViewModel,
    topBarTitle: String,
    buttonText: String,
    saveResult: Result<Unit>?,
    initialTitle: String,
    initialContent: String,
    initialCategoryId: Int?,
    onSaveClick: (String, String, CategoryEntity?) -> Unit,
    onBackClick: () -> Unit
) {
    val categories by viewModel.categoryList.observeAsState(emptyList())
    val isLoading = saveResult is Result.Loading
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val bodyMinLines = if (isLandscape) 2 else 3
    val bodyMaxLines = if (isLandscape) 5 else 9
    var title by remember { mutableStateOf(initialTitle) }
    var content by remember { mutableStateOf(initialContent) }
    var localError by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember(categories, initialCategoryId) {
        mutableStateOf(categories.find { it.categoryId == initialCategoryId })
    }

    Scaffold(
        topBar = {
            SimpleTopBar(
                text = topBarTitle,
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            AdjustableTextFieldLengthComponent(title, stringResource(R.string.title), Icons.Filled.Title, 255) { title = it }

            CategoryDropdownComponent(categories, selectedCategory) { selectedCategory = it }

            NoteBodyEditor(
                content = content,
                onContentChange = { content = it },
                minLines = bodyMinLines,
                maxLines = bodyMaxLines
            )

            SubmitButtonComponent(
                value = buttonText,
                onButtonClicked = {
                    if (title.isBlank()) {
                        localError = ""
                    } else {
                        localError = null
                        onSaveClick(title.trim(), content, selectedCategory)
                    }
                },
                isLoading = isLoading
            )

            val resultErrorMessage = when (saveResult) {
                is Result.Failure -> when (saveResult.errorCode) {
                    1 -> stringResource(R.string.note_error_1)
                    else -> stringResource(R.string.unknown_error)
                }
                is Result.Error -> stringResource(R.string.note_error_save)
                else -> null
            }

            val message = if (localError != null) {
                stringResource(R.string.note_error_1)
            } else {
                resultErrorMessage
            }

            message?.let {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    ErrorMessageComponent(value = it)
                }
            }
        }
    }
}

@Composable
private fun NoteBodyEditor(
    content: String,
    onContentChange: (String) -> Unit,
    minLines: Int,
    maxLines: Int
) {
    val ui = LocalUiScale.current
    val contentColor = MaterialTheme.colorScheme.onBackground

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    ) {
        Text(
            text = stringResource(R.string.note_content),
            fontSize = ui.fonts.passwordStrengthTextSize,
            fontWeight = FontWeight.Medium,
            color = contentColor.copy(alpha = 0.62f),
            modifier = Modifier.padding(horizontal = 2.dp)
        )

        HorizontalDivider(
            modifier = Modifier.padding(top = 8.dp),
            color = contentColor.copy(alpha = 0.12f)
        )

        BasicTextField(
            value = content,
            onValueChange = onContentChange,
            textStyle = TextStyle(
                fontSize = ui.fonts.generalTextSize,
                lineHeight = ui.fonts.generalTextSize * 1.45f,
                color = contentColor
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            minLines = minLines,
            maxLines = maxLines,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp, vertical = 12.dp),
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxSize()) {
                    if (content.isEmpty()) {
                        Text(
                            text = stringResource(R.string.note_body_placeholder),
                            fontSize = ui.fonts.generalTextSize,
                            lineHeight = ui.fonts.generalTextSize * 1.45f,
                            color = contentColor.copy(alpha = 0.45f)
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}
