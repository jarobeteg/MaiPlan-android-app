package com.example.maiplan.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Task
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.maiplan.R

sealed class BottomNavRoutes(
    val labelResId: Int,
    val icon: ImageVector
) {
    data object Events : BottomNavRoutes(R.string.event, Icons.Filled.Event)
    data object Tasks : BottomNavRoutes(R.string.task, Icons.Filled.Task)
    data object Files : BottomNavRoutes(R.string.file, Icons.Filled.Folder)
    data object More : BottomNavRoutes(R.string.more, Icons.Filled.Menu)
}