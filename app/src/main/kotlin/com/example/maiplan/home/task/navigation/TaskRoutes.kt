package com.example.maiplan.home.task.navigation

sealed class TaskRoutes(val route: String) {
    data object TaskMain : TaskRoutes("task-main-screen")
    data object Create : TaskRoutes("task-create")
    data object Detail : TaskRoutes("task-detail/{taskLocalId}") {
        fun withArgs(id: Long) = "task-detail/$id"
    }
    data object Edit : TaskRoutes("task-edit/{taskLocalId}") {
        fun withArgs(id: Long) = "task-edit/$id"
    }
}
