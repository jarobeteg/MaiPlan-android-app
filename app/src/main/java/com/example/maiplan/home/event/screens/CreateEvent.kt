package com.example.maiplan.home.event.screens

import android.annotation.SuppressLint
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.maiplan.components.SimpleTopBar
import com.example.maiplan.R
import com.example.maiplan.viewmodel.category.CategoryViewModel
import com.example.maiplan.viewmodel.event.EventViewModel
import com.example.maiplan.viewmodel.reminder.ReminderViewModel

/**
 * Still under work.
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun CreateEventScreen(
    eventViewModel: EventViewModel,
    categoryViewModel: CategoryViewModel,
    reminderViewModel: ReminderViewModel,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold (
        topBar = {
            SimpleTopBar(
                text = stringResource(R.string.event_new),
                onBackClick = onBackClick
            )
        }
    ) {}
}