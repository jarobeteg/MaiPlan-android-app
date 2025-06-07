package com.example.maiplan.home.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Task
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.maiplan.R

/**
 * Represents the different navigation routes for the Home component screens.
 *
 * Each object corresponds to a specific Home component screen.
 *
 * @property route Every object holds a route string value to differentiate route endpoints.
 * @property labelResId Every object holds an Id value for localized string resource to differentiate route endpoints.
 * @property icon Every object holds an [ImageVector] to show an icon with the label.
 */
sealed class HomeNavRoutes(
    val route: String,
    val labelResId: Int,
    val icon: ImageVector
) {
    /**
     * Route for the Event Main Screen, which opens the EventScreenWithNav screen with Monthly view selected by default.
     */
    data object Events : HomeNavRoutes("events", R.string.event, Icons.Filled.Event)

    /**
     * Route for the Task Main Screen, which opens the TaskScreenWithNav screen.
     */
    data object Tasks : HomeNavRoutes("tasks", R.string.task, Icons.Filled.Task)

    /**
     * Route for the File Main Screen, which opens the FileScreenWithNav screen.
     */
    data object Files : HomeNavRoutes("files", R.string.file, Icons.Filled.Folder)

    /**
     * Route for the More Main Screen, which opens the MoreScreenWithNav screen.
     */
    data object More : HomeNavRoutes("more", R.string.more, Icons.Filled.Menu)
}