package com.example.maiplan.home.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Task
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.maiplan.R
import com.example.maiplan.home.event.screens.*
import com.example.maiplan.home.task.screens.TaskScreen
import com.example.maiplan.home.file.screens.FileScreen
import com.example.maiplan.home.more.screens.MoreScreen

/**
 * Represents the different navigation routes for the `Home` component screens.
 *
 * Each object corresponds to a specific `Home` component screen.
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
     * Route for the Event Main Screen, which opens the [EventScreen] screen with [MonthlyView]selected by default.
     */
    data object Events : HomeNavRoutes("events", R.string.event, Icons.Filled.Event)

    /**
     * Route for the Task Main Screen, which opens the [TaskScreen].
     */
    data object Tasks : HomeNavRoutes("tasks", R.string.task, Icons.Filled.Task)

    /**
     * Route for the File Main Screen, which opens the [FileScreen].
     */
    data object Files : HomeNavRoutes("files", R.string.file, Icons.Filled.Folder)

    /**
     * Route for the More Main Screen, which opens the [MoreScreen].
     */
    data object More : HomeNavRoutes("more", R.string.more, Icons.Filled.Menu)
}