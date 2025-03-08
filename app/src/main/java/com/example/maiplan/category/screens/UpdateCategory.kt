package com.example.maiplan.category.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.example.maiplan.components.CategoryTopBar
import com.example.maiplan.components.ColorPickerRow
import com.example.maiplan.components.ErrorMessageComponent
import com.example.maiplan.components.IconPickerRow
import com.example.maiplan.components.SubmitButtonComponent
import com.example.maiplan.network.CategoryResponse
import com.example.maiplan.repository.Result
import com.example.maiplan.utils.IconData
import com.example.maiplan.viewmodel.CategoryViewModel

@Composable
fun UpdateCategoryScreen(
    viewModel: CategoryViewModel,
    category: CategoryResponse,
    onSaveClick: (String, String, String, String) -> Unit,
    onBackClick: () -> Unit
) {
    val saveResult by viewModel.updateCategoryResult.observeAsState()
    var name by remember { mutableStateOf(category.name) }
    var description by remember { mutableStateOf(category.description) }
    var selectedColor by remember { mutableStateOf(Color(category.color.toULong())) }
    var selectedIcon by remember { mutableStateOf(IconData.getIconByKey(category.icon)) }
    var selectedIconString by remember { mutableStateOf(category.icon) }

    Scaffold ( topBar = { CategoryTopBar(
        text = stringResource(R.string.category_modify),
        onBackClick = onBackClick
    ) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AdjustableTextFieldLengthComponent(name, stringResource(R.string.category_name), 255) { name = it }

            AdjustableTextFieldLengthComponent(description, stringResource(R.string.category_description), 512) { description = it }

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

            SubmitButtonComponent(stringResource(R.string.category_update)) { onSaveClick(name, description, selectedColor.value.toString(), selectedIconString) }
        }
    }
}
