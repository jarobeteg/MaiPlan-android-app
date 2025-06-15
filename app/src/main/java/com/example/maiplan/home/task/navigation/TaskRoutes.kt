package com.example.maiplan.home.task.navigation

import com.example.maiplan.home.task.screens.*

/**
 * Represents the different navigation routes for the `Task` screens.
 *
 * Each object corresponds to a specific `Task` screen.
 *
 * @property route Task object holds a route string value to differentiate route endpoints.
 */
sealed class TaskRoutes(val route: String) {

    /**
     * Route for the [TaskScreen], which is the `Main Task Screen`, it allows navigation between other task screen as well as home tabs.
     */
    data object TaskMain : TaskRoutes("task-main-screen")
}