package com.example.maiplan.category.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.example.maiplan.R
import com.example.maiplan.database.entities.CategoryEntity
import com.example.maiplan.repository.Result
import com.example.maiplan.utils.common.IconData
import com.example.maiplan.viewmodel.category.CategoryViewModel

@Composable
fun UpdateCategoryScreen(
    viewModel: CategoryViewModel,
    category: CategoryEntity,
    onSaveClick: (String, String, String, String) -> Unit,
    onBackClick: () -> Unit,
) {
    val saveResult by viewModel.updateCategoryResult.observeAsState()
    var name by remember(category.categoryId) { mutableStateOf(category.name) }
    var description by remember(category.categoryId) { mutableStateOf(category.description) }
    var selectedColor by remember(category.categoryId) {
        mutableStateOf(Color(category.color.toULong()))
    }
    var selectedIcon by remember(category.categoryId) {
        mutableStateOf(IconData.getIconByKey(category.icon))
    }
    var selectedIconString by remember(category.categoryId) { mutableStateOf(category.icon) }

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
        title = stringResource(R.string.category_modify),
        heading = stringResource(R.string.category_update_heading),
        subtitle = stringResource(R.string.category_update_subtitle),
        name = name,
        description = description,
        selectedColor = selectedColor,
        selectedIcon = selectedIcon,
        onNameChange = { name = it },
        onDescriptionChange = { description = it },
        onColorSelected = { selectedColor = it },
        onIconSelected = { selectedIcon = it },
        onIconSelectedString = { selectedIconString = it },
        submitLabel = stringResource(R.string.category_update),
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
