package com.example.maiplan.home.event.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import com.example.maiplan.database.entities.EventEntity
import com.example.maiplan.database.entities.ReminderEntity
import com.example.maiplan.viewmodel.category.CategoryViewModel

@Composable
fun CreateEventScreen(
    categoryViewModel: CategoryViewModel,
    isSaving: Boolean = false,
    onSaveClick: (ReminderEntity?, EventEntity) -> Unit,
    onBackClick: () -> Unit,
) {
    val categories by categoryViewModel.categoryList.observeAsState(emptyList())
    EventFullEditor(
        heading = "New event",
        initial = null,
        initialReminder = null,
        categories = categories,
        isSaving = isSaving,
        onBack = onBackClick,
        onSave = onSaveClick,
    )
}
