package com.example.maiplan.home.event.screens

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import com.example.maiplan.database.entities.EventEntity
import com.example.maiplan.database.entities.ReminderEntity
import com.example.maiplan.repository.event.EventEditSnapshot
import com.example.maiplan.utils.common.UserSession
import com.example.maiplan.viewmodel.category.CategoryViewModel
import com.example.maiplan.viewmodel.event.EventViewModel

@Composable
fun UpdateEventScreen(
    eventLocalId: Long,
    eventViewModel: EventViewModel,
    categoryViewModel: CategoryViewModel,
    onUpdateClick: (ReminderEntity?, EventEntity) -> Unit,
    onBackClick: () -> Unit,
) {
    val userId = UserSession.userLocalId ?: return
    val categories by categoryViewModel.categoryList.observeAsState(emptyList())
    var snapshot by remember(eventLocalId, userId) { mutableStateOf<EventEditSnapshot?>(null) }
    var loaded by remember(eventLocalId, userId) { mutableStateOf(false) }
    LaunchedEffect(eventLocalId, userId) {
        snapshot = eventViewModel.getEventForEdit(eventLocalId, userId)
        loaded = true
    }
    val current = snapshot
    when {
        !loaded -> CircularProgressIndicator()
        current == null -> Text("Event not found")
        else -> EventFullEditor(
            heading = "Edit event",
            initial = current.event,
            initialReminder = current.reminder,
            categories = categories,
            onBack = onBackClick,
            onSave = onUpdateClick,
        )
    }
}
