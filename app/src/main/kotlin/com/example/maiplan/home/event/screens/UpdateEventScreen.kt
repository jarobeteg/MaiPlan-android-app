package com.example.maiplan.home.event.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.maiplan.database.entities.EventEntity
import com.example.maiplan.database.entities.ReminderEntity
import com.example.maiplan.repository.event.EventEditSnapshot
import com.example.maiplan.theme.LocalAppDarkTheme
import com.example.maiplan.utils.common.UserSession
import com.example.maiplan.viewmodel.category.CategoryViewModel
import com.example.maiplan.viewmodel.event.EventViewModel
import java.time.LocalDate

@Composable
fun UpdateEventScreen(
    eventLocalId: Long,
    eventViewModel: EventViewModel,
    categoryViewModel: CategoryViewModel,
    isSaving: Boolean = false,
    onUpdateClick: (ReminderEntity?, EventEntity) -> Unit,
    onBackClick: () -> Unit,
    occurrenceDate: LocalDate? = null,
) {
    val userId = UserSession.userLocalId ?: return
    val categories by categoryViewModel.categoryList.observeAsState(emptyList())
    var snapshot by remember(eventLocalId, userId, occurrenceDate) { mutableStateOf<EventEditSnapshot?>(null) }
    var loaded by remember(eventLocalId, userId, occurrenceDate) { mutableStateOf(false) }
    LaunchedEffect(eventLocalId, userId, occurrenceDate) {
        snapshot = runCatching { eventViewModel.getEventForEdit(eventLocalId, userId, occurrenceDate) }.getOrNull()
        loaded = true
    }
    val current = snapshot
    when {
        !loaded || current == null -> EventScreenBackground {
            Scaffold(
                topBar = { EventEditorTopBar("Edit event", onBackClick) },
                containerColor = Color.Transparent,
            ) { padding ->
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    if (!loaded) CircularProgressIndicator()
                    else Text(
                        "Event not found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (LocalAppDarkTheme.current) Color(0xFFF5F7FB) else EventInk,
                    )
                }
            }
        }
        else -> EventFullEditor(
            heading = if (occurrenceDate != null) "Edit current occurrence"
                else if (current.event.recurrenceFrequency != null) "Edit all occurrences" else "Edit event",
            initial = current.event,
            initialReminder = current.reminder,
            categories = categories,
            isSaving = isSaving,
            onBack = onBackClick,
            onSave = onUpdateClick,
            allowRepeat = occurrenceDate == null,
        )
    }
}
