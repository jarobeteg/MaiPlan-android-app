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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.sp
import com.example.maiplan.R
import com.example.maiplan.components.AdjustableTextFieldLengthComponent
import com.example.maiplan.components.SimpleTopBar
import com.example.maiplan.components.ColorPickerRow
import com.example.maiplan.components.ErrorMessageComponent
import com.example.maiplan.components.IconPickerRow
import com.example.maiplan.components.SubmitButtonComponent
import com.example.maiplan.components.isTablet
import com.example.maiplan.database.entities.CategoryEntity
import com.example.maiplan.repository.Result
import com.example.maiplan.utils.common.IconData
import com.example.maiplan.viewmodel.category.CategoryViewModel

@Composable
fun UpdateCategoryScreen(
    viewModel: CategoryViewModel,
    category: CategoryEntity,
    onSaveClick: (String, String, String, String) -> Unit,
    onBackClick: () -> Unit
) {
    val saveResult by viewModel.updateCategoryResult.observeAsState()
    val isLoading = saveResult is Result.Loading
    var name by remember { mutableStateOf(category.name) }
    var description by remember { mutableStateOf(category.description) }
    var selectedColor by remember { mutableStateOf(Color(category.color.toULong())) }
    var selectedIcon by remember { mutableStateOf(IconData.getIconByKey(category.icon)) }
    var selectedIconString by remember { mutableStateOf(category.icon) }

    val isTablet = isTablet()
    val fieldHeight = if (isTablet) 96.dp else 64.dp

    Scaffold ( topBar = { SimpleTopBar(
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
            AdjustableTextFieldLengthComponent(name, stringResource(R.string.name), Icons.Filled.Title, 255) { name = it }

            AdjustableTextFieldLengthComponent(description, stringResource(R.string.description), Icons.Filled.Description, 512) { description = it }

            ColorPickerRow(
                selectedColor = selectedColor,
                onColorSelected = { selectedColor = it }
            )

            IconPickerRow(
                selectedIcon = selectedIcon,
                onIconSelected = { selectedIcon = it },
                onIconSelectedString = { selectedIconString = it }
            )

            Spacer(modifier = Modifier.height(8.dp))

            SubmitButtonComponent(
                value = stringResource(R.string.category_update),
                onButtonClicked = { onSaveClick(name, description, selectedColor.value.toString(), selectedIconString) },
                isLoading = isLoading,
                fontSize = if (isTablet) 24.sp * 1.75f else 18.sp,
                modifier = Modifier.fillMaxWidth().height(fieldHeight)
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
                    ErrorMessageComponent(
                        value = errorMessage,
                        fontSize = if (isTablet) 24.sp * 1.75f else 18.sp,
                        style = if (isTablet) MaterialTheme.typography.labelLarge else MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}