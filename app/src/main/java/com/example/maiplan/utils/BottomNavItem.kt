package com.example.maiplan.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Task
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.maiplan.R

sealed class BottomNavItem(
    val labelResId: Int,
    val icon: ImageVector,
    val route: String
) {
    data object Events : BottomNavItem(R.string.event, Icons.Filled.Event, "event")
    data object Tasks : BottomNavItem(R.string.task, Icons.Filled.Task, "task")
    data object Files : BottomNavItem(R.string.file, Icons.Filled.Folder, "file")
    data object More : BottomNavItem(R.string.more, Icons.Filled.Menu, "more")
}