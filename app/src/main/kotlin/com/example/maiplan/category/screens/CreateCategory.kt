package com.example.maiplan.category.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import com.example.maiplan.R
import com.example.maiplan.components.rememberEditorBackAction
import com.example.maiplan.repository.Result
import com.example.maiplan.viewmodel.category.CategoryViewModel
import com.example.maiplan.utils.common.IconData

@Composable
fun CreateCategoryScreen(
    viewModel: CategoryViewModel,
    onSaveClick: (String, String, String, String) -> Unit,
    onBackClick: () -> Unit,
) {
    val saveResult by viewModel.createCategoryResult.observeAsState()
    val initialColor = rememberSaveable { CategoryPrimary.value.toString() }
    var name by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var selectedColorString by rememberSaveable { mutableStateOf(initialColor) }
    val selectedColor = Color(selectedColorString.toULong())
    var selectedIconString by rememberSaveable { mutableStateOf("search") }
    val selectedIcon = IconData.getIconByKey(selectedIconString)
    val back = rememberEditorBackAction(
        hasChanges = name.isNotEmpty() || description.isNotEmpty() ||
            selectedColorString != initialColor || selectedIconString != "search",
        saving = saveResult is Result.Loading,
        onBack = onBackClick,
    )

    val errorMessage = (saveResult as? Result.Failure)?.let { error ->
        stringResource(
            when (error.errorCode) {
                1 -> R.string.category_error_1
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
        onColorSelected = { selectedColorString = it.value.toString() },
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
        onBackClick = back,
    )
}
