package com.example.maiplan.category.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.example.maiplan.R
import com.example.maiplan.repository.Result
import com.example.maiplan.viewmodel.category.CategoryViewModel

@Composable
fun CreateCategoryScreen(
    viewModel: CategoryViewModel,
    onSaveClick: (String, String, String, String) -> Unit,
    onBackClick: () -> Unit,
) {
    val saveResult by viewModel.createCategoryResult.observeAsState()
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(CategoryPrimary) }
    var selectedIcon by remember { mutableStateOf(Icons.Rounded.Search) }
    var selectedIconString by remember { mutableStateOf("search") }

    val errorMessage = (saveResult as? Result.Failure)?.let { error ->
        stringResource(
            when (error.errorCode) {
                1 -> R.string.category_error_1
                2 -> R.string.category_error_2
                else -> R.string.unknown_error
            },
        )
    }

    CategoryEditorLayout(
        title = stringResource(R.string.category_new),
        heading = stringResource(R.string.category_create_heading),
        subtitle = stringResource(R.string.category_create_subtitle),
        name = name,
        description = description,
        selectedColor = selectedColor,
        selectedIcon = selectedIcon,
        onNameChange = { name = it },
        onDescriptionChange = { description = it },
        onColorSelected = { selectedColor = it },
        onIconSelected = { selectedIcon = it },
        onIconSelectedString = { selectedIconString = it },
        submitLabel = stringResource(R.string.category_save),
        isLoading = saveResult is Result.Loading,
        errorMessage = errorMessage,
        onSubmit = {
            onSaveClick(
                name,
                description,
                selectedColor.value.toString(),
                selectedIconString,
            )
        },
        onBackClick = onBackClick,
    )
}
