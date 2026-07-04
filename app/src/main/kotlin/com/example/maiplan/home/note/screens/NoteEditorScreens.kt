package com.example.maiplan.home.note.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.example.maiplan.R
import com.example.maiplan.components.AdjustableSpacer
import com.example.maiplan.components.AdjustableTextFieldLengthComponent
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AdjustableTextFieldLengthComponent(
                value = title,
                label = stringResource(R.string.title),
                icon = Icons.Filled.Title,
                length = 255,
                onValueChange = { title = it }
            )

            NoteCategoryDropdown(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )

            OutlinedTextField(
                value = content,
                onValueChange = { if (it.length <= 4000) content = it },
                label = {
                    Text(
                        text = stringResource(R.string.note_content),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 220.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onBackground,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    focusedLabelColor = MaterialTheme.colorScheme.onBackground,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
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
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.background,
                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.onBackground,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                focusedLabelColor = MaterialTheme.colorScheme.onBackground,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.primary)
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
