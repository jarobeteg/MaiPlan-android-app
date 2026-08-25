package com.example.maiplan.home.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Home
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.maiplan.R

sealed class HomeNavRoutes(
    val route: String,
    val labelResId: Int,
    val icon: ImageVector
) {
    data object Events : HomeNavRoutes("events", R.string.event, Icons.Rounded.CalendarMonth)
    data object Tasks : HomeNavRoutes("tasks", R.string.task, Icons.Rounded.CheckCircle)
    data object Notes : HomeNavRoutes("notes", R.string.notes, Icons.AutoMirrored.Rounded.Notes)
    data object Home : HomeNavRoutes("home", R.string.home, Icons.Rounded.Home)
    data object More : HomeNavRoutes("more", R.string.more, Icons.Rounded.GridView)
}
