package com.example.maiplan.category.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.maiplan.R
import com.example.maiplan.components.AdjustableTextFieldLengthComponent
import com.example.maiplan.components.SimpleTopBar
import com.example.maiplan.components.ColorPickerRow
import com.example.maiplan.components.ErrorMessageComponent
import com.example.maiplan.components.IconPickerRow
import com.example.maiplan.components.SubmitButtonComponent
import com.example.maiplan.viewmodel.CategoryViewModel
import com.example.maiplan.repository.Result

/**
 * Composable screen for creating a new category.
 *
 * This screen includes input fields for a category's name and description,
 * a color picker, and an icon picker. When the user submits the form, the
 * [onSaveClick] callback is triggered with the entered values.
 *
 * The screen also observes [viewmodel.createCategoryResult] to display
 * any error messages if category creation fails.
 *
 * @param viewModel Provides category creation logic and category result state.
 * @param onSaveClick Callback invoked when the user submits the creation form.
 * @param onBackClick Callback invoked when the back button in the top app bar is clicked.
 *
 * @see AdjustableTextFieldLengthComponent
 * @see SimpleTopBar
 * @see ColorPickerRow
 * @see ErrorMessageComponent
 * @see IconPickerRow
 * @see SubmitButtonComponent
 * @see CategoryViewModel
 * @see Result
 */
@Composable
fun CreateCategoryScreen(
    viewModel: CategoryViewModel,
    onSaveClick: (String, String, String, String) -> Unit,
    onBackClick: () -> Unit
) {
    val saveResult by viewModel.createCategoryResult.observeAsState()
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(Color(0xFF4A6583)) }
    var selectedIcon by remember { mutableStateOf(Icons.Filled.Search) }
    var selectedIconString by remember { mutableStateOf("search") }

    Scaffold ( topBar = { SimpleTopBar(
        text = stringResource(R.string.category_new),
        onBackClick = onBackClick
    ) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AdjustableTextFieldLengthComponent(name, stringResource(R.string.name), 255) { name = it }

            AdjustableTextFieldLengthComponent(description, stringResource(R.string.description), 512) { description = it }

            ColorPickerRow(
                selectedColor = selectedColor,
                onColorSelected = { selectedColor = it }
            )

            IconPickerRow(
                selectedIcon = selectedIcon,
                onIconSelected = { selectedIcon = it },
                onIconSelectedString = { selectedIconString = it }
            )

            if (saveResult is Result.Failure) {
                val error = saveResult as Result.Failure
                val code = error.errorCode

                val errorMessageId = when (code) {
                    1 -> R.string.category_error_1
                    2 -> R.string.category_error_2
                    else -> R.string.unknown_error
                }

                val errorMessage = stringResource(errorMessageId)

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    ErrorMessageComponent(errorMessage)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            SubmitButtonComponent(stringResource(R.string.category_save)) { onSaveClick(name, description, selectedColor.value.toString(), selectedIconString) }
        }
    }
}