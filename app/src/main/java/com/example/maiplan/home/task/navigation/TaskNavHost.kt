package com.example.maiplan.home.task.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.maiplan.home.task.screens.*

@Composable
fun TaskNavHost(rootNavController: NavHostController, localNavController: NavHostController) {
    NavHost(
        navController = localNavController,
        startDestination = TaskRoutes.TaskMain.route,
        enterTransition = { fadeIn(animationSpec = tween(0)) },
        exitTransition = { fadeOut(animationSpec = tween(0)) },
        popEnterTransition = { fadeIn(animationSpec = tween(0)) },
        popExitTransition = { fadeOut(animationSpec = tween(0)) }
    ) {
        taskNavGraph(localNavController, rootNavController)
    }
}

fun NavGraphBuilder.taskNavGraph(
    localNavController: NavController,
    rootNavController: NavHostController
) {
    // --- Main Task Screen ---
    composable(TaskRoutes.TaskMain.route) {
        TaskScreen(rootNavController)
    }
}