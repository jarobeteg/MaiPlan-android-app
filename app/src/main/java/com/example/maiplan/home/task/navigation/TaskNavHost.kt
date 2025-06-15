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

/**
 * [Composable] that sets up the navigation host for the `Task` screens.
 *
 * This host defines the entry point and connects the navigation graph for:
 * - Viewing and managing tasks ([TaskScreen])
 *
 * Navigation transitions are set to instantly fade between screens for a seamless and subtle effect:
 * - `enterTransition`, `popEnterTransition`: Fade in with no delay.
 * - `exitTransition`, `popExitTransition`: Fade out with no delay.
 *
 * @param rootNavController Navigation controller for switching between root-level screens (e.g. `Home` tabs).
 * @param localNavController Navigation controller scoped to `Task`-related screens.
 *
 * @see TaskRoutes
 * @see taskNavGraph
 */
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

/**
 * Defines the navigation graph for the `Task` screens.
 *
 * This graph includes:
 * - [TaskScreen]: Displays all `Tasks` for the `User`.
 *
 * Navigation is handled via [localNavController] for `Task` screens,
 * and [rootNavController] for navigating between root-level `Home` tabs.
 *
 * @param localNavController The controller that handles navigation between `Task` screens.
 * @param rootNavController The controller that handles navigation between `Home` screens.
 *
 * @see TaskRoutes
 * @see TaskScreen
 */
fun NavGraphBuilder.taskNavGraph(
    localNavController: NavController,
    rootNavController: NavHostController
) {
    // --- Main Task Screen ---
    composable(TaskRoutes.TaskMain.route) {
        TaskScreen(rootNavController)
    }
}