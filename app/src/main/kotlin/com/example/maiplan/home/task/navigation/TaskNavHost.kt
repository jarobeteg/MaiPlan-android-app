package com.example.maiplan.home.task.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.maiplan.home.task.screens.*
import com.example.maiplan.repository.task.TaskRepository
import com.example.maiplan.viewmodel.task.*

@Composable
fun TaskNavHost(rootNavController: NavHostController, localNavController: NavHostController,
    userId: Long, repository: TaskRepository, taskViewModel: TaskViewModel,
    notificationTaskId: Long? = null, onNotificationHandled: () -> Unit = {}) {
    val open: (Long) -> Unit = { localNavController.navigate(TaskRoutes.Detail.withArgs(it)) { launchSingleTop = true } }
    TaskFeatureTheme {
        NavHost(localNavController, startDestination = TaskRoutes.TaskMain.route) {
            composable(TaskRoutes.TaskMain.route) {
                TaskListScreen(rootNavController, taskViewModel, { localNavController.navigate(TaskRoutes.Create.route) { launchSingleTop = true } }, open)
            }
            composable(TaskRoutes.Detail.route, arguments = listOf(navArgument("taskLocalId") { type = NavType.LongType })) { entry ->
                val id = entry.arguments?.getLong("taskLocalId") ?: -1L
                TaskDetailScreen(id, taskViewModel, { localNavController.popBackStack() },
                    { localNavController.navigate(TaskRoutes.Edit.withArgs(id)) { launchSingleTop = true } }, open)
            }
            composable(TaskRoutes.Create.route) { entry ->
                val factory = remember(userId, repository) { taskViewModelFactory { TaskEditorViewModel(userId, null, repository, it) } }
                val editor: TaskEditorViewModel = viewModel(viewModelStoreOwner = entry, key = "create-$userId", factory = factory)
                TaskEditorScreen(false, editor, taskViewModel, { localNavController.popBackStack() }) { id ->
                    localNavController.popBackStack(); open(id)
                }
            }
            composable(TaskRoutes.Edit.route, arguments = listOf(navArgument("taskLocalId") { type = NavType.LongType })) { entry ->
                val id = entry.arguments?.getLong("taskLocalId") ?: -1L
                val factory = remember(userId, id, repository) { taskViewModelFactory { TaskEditorViewModel(userId, id, repository, it) } }
                val editor: TaskEditorViewModel = viewModel(viewModelStoreOwner = entry, key = "edit-$userId-$id", factory = factory)
                TaskEditorScreen(true, editor, taskViewModel, { localNavController.popBackStack() }) { savedId ->
                    localNavController.popBackStack()
                    if (savedId != id) { localNavController.popBackStack(); open(savedId) }
                }
            }
        }
    }
    LaunchedEffect(notificationTaskId) {
        if (notificationTaskId != null) {
            localNavController.popBackStack(TaskRoutes.TaskMain.route, false)
            open(notificationTaskId)
            onNotificationHandled()
        }
    }
}
