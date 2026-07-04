package com.example.maiplan.home.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Task
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.maiplan.R

sealed class HomeNavRoutes(
    val route: String,
    val labelResId: Int,
    val icon: ImageVector
) {
    data object Events : HomeNavRoutes("events", R.string.event, Icons.Filled.Event)
    data object Tasks : HomeNavRoutes("tasks", R.string.task, Icons.Filled.Task)
    data object Notes : HomeNavRoutes("notes", R.string.notes, Icons.Filled.Notes)
    data object More : HomeNavRoutes("more", R.string.more, Icons.Filled.Menu)
}
