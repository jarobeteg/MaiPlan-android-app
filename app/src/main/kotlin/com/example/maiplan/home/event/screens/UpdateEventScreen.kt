package com.example.maiplan.home.event.screens

import android.annotation.SuppressLint
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.maiplan.R
import com.example.maiplan.components.SimpleTopBar
import com.example.maiplan.viewmodel.event.EventViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun UpdateEventScreen(
    eventViewModel: EventViewModel,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold (
        topBar = {
            SimpleTopBar(
                text = stringResource(R.string.event_update),
                onBackClick = onBackClick
            )
        }
    ) {}
}