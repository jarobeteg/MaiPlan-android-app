package com.example.maiplan.home.note.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.maiplan.R
import com.example.maiplan.components.AdjustableSpacer
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
    val ui = LocalUiScale.current
    val categories by viewModel.categoryList.observeAsState(emptyList())
    val isLoading = saveResult is Result.Loading
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val bodyMinHeight = if (isLandscape) 132.dp else 220.dp
    val bodyMinLines = if (isLandscape) 4 else 8
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
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = notePanelColor()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { if (it.length <= 255) title = it },
                        placeholder = {
                            Text(
                                text = stringResource(R.string.note_title_placeholder),
                                fontSize = ui.fonts.generalTextSize
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Title,
                                contentDescription = null,
                                modifier = Modifier.size(ui.components.generalIconSize),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        textStyle = TextStyle(
                            fontSize = ui.fonts.generalTextSize,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        colors = noteTextFieldColors()
                    )

                    NoteCategoryDropdown(
                        categories = categories,
                        selectedCategory = selectedCategory,
                        onCategorySelected = { selectedCategory = it }
                    )
                }
            }

            OutlinedTextField(
                value = content,
                onValueChange = { if (it.length <= 4000) content = it },
                placeholder = {
                    Text(
                        text = stringResource(R.string.note_body_placeholder),
                        fontSize = ui.fonts.generalTextSize,
                        style = ui.typographies.generalTextStyle
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Description,
                        contentDescription = null,
                        modifier = Modifier.size(ui.components.generalIconSize)
                    )
                },
                textStyle = TextStyle(fontSize = ui.fonts.generalTextSize),
                shape = RoundedCornerShape(8.dp),
                minLines = bodyMinLines,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = bodyMinHeight),
                supportingText = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "${content.length}/4000",
                            fontSize = ui.fonts.passwordStrengthTextSize,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
                        )
                    }
                },
                colors = noteTextFieldColors()
            )

            AdjustableSpacer(ui.dimensions.mediumSpacer)

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
private fun noteTextFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = noteInputColor(),
    unfocusedContainerColor = noteInputColor(),
    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
    unfocusedIndicatorColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.22f),
    focusedTextColor = MaterialTheme.colorScheme.onBackground,
    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
    focusedLabelColor = MaterialTheme.colorScheme.onBackground,
    unfocusedLabelColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
    focusedPlaceholderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
    cursorColor = MaterialTheme.colorScheme.primary
)

@Composable
private fun notePanelColor(): Color {
    return if (isSystemInDarkTheme()) {
        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f)
    } else {
        MaterialTheme.colorScheme.surface
    }
}

@Composable
private fun noteInputColor(): Color {
    return if (isSystemInDarkTheme()) {
        MaterialTheme.colorScheme.background.copy(alpha = 0.65f)
    } else {
        MaterialTheme.colorScheme.background
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteCategoryDropdown(
    categories: List<CategoryEntity>,
    selectedCategory: CategoryEntity?,
    onCategorySelected: (CategoryEntity?) -> Unit
) {
    val ui = LocalUiScale.current
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedCategory?.name ?: stringResource(R.string.uncategorized),
            onValueChange = {},
            readOnly = true,
            label = {
                Text(
                    text = stringResource(R.string.category),
                    fontSize = ui.fonts.generalTextSize,
                    style = ui.typographies.generalTextStyle
                )
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded,
                    modifier = Modifier.size(ui.components.generalIconSize)
                )
            },
            textStyle = TextStyle(fontSize = ui.fonts.generalTextSize),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
            colors = noteTextFieldColors()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(notePanelColor())
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(R.string.uncategorized),
                        fontSize = ui.fonts.generalTextSize,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                onClick = {
                    onCategorySelected(null)
                    expanded = false
                }
            )

            categories.forEach { category ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = category.name,
                            fontSize = ui.fonts.generalTextSize,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}
