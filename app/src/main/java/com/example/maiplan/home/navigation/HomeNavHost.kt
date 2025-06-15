package com.example.maiplan.home.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.maiplan.home.event.EventScreenManager
import com.example.maiplan.home.task.TaskScreenManager
import com.example.maiplan.home.more.MoreScreenManager
import com.example.maiplan.home.file.FileScreenManager

/**
 * [Composable] that sets up the navigation host for the root-level `Home` tabs.
 *
 * This host manages the navigation between top-level sections:
 * - `Events` via [EventScreenManager]
 * - `Tasks` via [TaskScreenManager]
 * - `Files` via [FileScreenManager]
 * - `More` via [MoreScreenManager]
 *
 * Navigation transitions use instant fade effects to create smooth and subtle screen changes:
 * - `enterTransition`, `popEnterTransition`: Fade in with no delay.
 * - `exitTransition`, `popExitTransition`: Fade out with no delay.
 *
 * @param rootNavController The central [NavHostController] shared across all major screens (used by the bottom navigation bar).
 *
 * @see HomeNavRoutes
 * @see homeNavGraph
 */
@Composable
fun HomeNavHost(rootNavController: NavHostController) {
    NavHost(
        navController = rootNavController,
        startDestination = HomeNavRoutes.Events.route,
        enterTransition = { fadeIn(animationSpec = tween(0)) },
        exitTransition = { fadeOut(animationSpec = tween(0)) },
        popEnterTransition = { fadeIn(animationSpec = tween(0)) },
        popExitTransition = { fadeOut(animationSpec = tween(0)) }
    ) {
        homeNavGraph(rootNavController)
    }
}

/**
 * Defines the navigation graph for the top-level `Home` tabs.
 *
 * Each destination corresponds to a screen section managed by a dedicated screen manager:
 * - [EventScreenManager] for `Event`-related views.
 * - [TaskScreenManager] for `Task`-related views.
 * - [FileScreenManager] for `File`-related views.
 * - [MoreScreenManager] for user options, settings, or additional features.
 *
 * Navigation within each section is handled internally via their own local nav controllers.
 *
 * @param rootNavController The root-level [NavHostController] used for main screen navigation.
 *
 * @see HomeNavRoutes
 * @see EventScreenManager
 * @see TaskScreenManager
 * @see FileScreenManager
 * @see MoreScreenManager
 */
fun NavGraphBuilder.homeNavGraph(
    rootNavController: NavHostController
) {
    composable(HomeNavRoutes.Events.route) {
        EventScreenManager(rootNavController)
    }
    composable(HomeNavRoutes.Tasks.route) {
        TaskScreenManager(rootNavController)
    }
    composable(HomeNavRoutes.Files.route) {
        FileScreenManager(rootNavController)
    }
    composable(HomeNavRoutes.More.route) {
        MoreScreenManager(rootNavController)
    }
}