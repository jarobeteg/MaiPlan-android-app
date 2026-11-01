package com.example.maiplan.home.task.navigation

import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.maiplan.home.task.screens.*
import com.example.maiplan.home.task.TaskScreenManager
import com.example.maiplan.viewmodel.task.*

fun NavGraphBuilder.taskNavGraph(navController: NavHostController) {
    val open: (Long) -> Unit = { navController.navigate(TaskRoutes.Detail.withArgs(it)) { launchSingleTop = true } }
    composable(TaskRoutes.TaskMain.route) { entry ->
        TaskScreenManager(navController, entry) { _, _, model ->
            TaskListScreen(navController, model,
                { navController.navigate(TaskRoutes.Create.route) { launchSingleTop = true } }, open)
        }
    }
    composable(TaskRoutes.Detail.route, arguments = listOf(navArgument("taskLocalId") { type = NavType.LongType })) { entry ->
        val id = entry.arguments?.getLong("taskLocalId") ?: -1L
        TaskScreenManager(navController, entry) { _, _, model ->
            TaskDetailScreen(id, model, { navController.popBackStack() },
                { navController.navigate(TaskRoutes.Edit.withArgs(id)) { launchSingleTop = true } }, open)
        }
    }
    composable(TaskRoutes.Create.route) { entry ->
        TaskScreenManager(navController, entry) { userId, repository, model ->
            val factory = remember(userId, repository) { taskViewModelFactory { TaskEditorViewModel(userId, null, repository, it) } }
            val editor: TaskEditorViewModel = viewModel(viewModelStoreOwner = entry, key = "create-$userId", factory = factory)
            TaskEditorScreen(false, editor, model, { navController.popBackStack() }) { id ->
                navController.popBackStack(); open(id)
            }
        }
    }
    composable(TaskRoutes.Edit.route, arguments = listOf(navArgument("taskLocalId") { type = NavType.LongType })) { entry ->
        val id = entry.arguments?.getLong("taskLocalId") ?: -1L
        TaskScreenManager(navController, entry) { userId, repository, model ->
            val factory = remember(userId, id, repository) { taskViewModelFactory { TaskEditorViewModel(userId, id, repository, it) } }
            val editor: TaskEditorViewModel = viewModel(viewModelStoreOwner = entry, key = "edit-$userId-$id", factory = factory)
            TaskEditorScreen(true, editor, model, { navController.popBackStack() }) { savedId ->
                navController.popBackStack()
                if (savedId != id) { navController.popBackStack(); open(savedId) }
            }
        }
    }
}
