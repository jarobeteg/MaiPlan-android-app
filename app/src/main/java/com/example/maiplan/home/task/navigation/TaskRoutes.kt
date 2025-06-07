package com.example.maiplan.home.task.navigation

sealed class TaskRoutes(val route: String) {
    data object TaskMain : TaskRoutes("task-main-screen")
}