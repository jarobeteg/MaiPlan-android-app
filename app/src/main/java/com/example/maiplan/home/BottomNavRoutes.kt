package com.example.maiplan.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Task
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.maiplan.R

/**
 * Represents the different navigation routes for the Home component Activity screens.
 *
 * Each object corresponds to a specific Home component Activity screen.
 *
 * @property labelResId Every object holds an Id value for localized string resource to differentiate route endpoints.
 * @property icon Every object holds an [ImageVector] to show an icon with the label.
 */
sealed class BottomNavRoutes(
    val labelResId: Int,
    val icon: ImageVector
) {
    /**
     * Route for the Event Activity, which opens the EventScreenWithNav screen with Monthly view selected by default.
     */
    data object Events : BottomNavRoutes(R.string.event, Icons.Filled.Event)

    /**
     * Route for the Task Activity, which opens the TaskScreenWithNav screen.
     */
    data object Tasks : BottomNavRoutes(R.string.task, Icons.Filled.Task)

    /**
     * Route for the File Activity, which opens the FileScreenWithNav screen.
     */
    data object Files : BottomNavRoutes(R.string.file, Icons.Filled.Folder)

    /**
     * Route for the More Activity, which opens the MoreScreenWithNav screen.
     */
    data object More : BottomNavRoutes(R.string.more, Icons.Filled.Menu)
}