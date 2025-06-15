package com.example.maiplan.home.file.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.maiplan.home.file.screens.FileScreen

/**
 * [Composable] that sets up the navigation host for the `File` screens.
 *
 * This host defines the entry point and connects the navigation graph for:
 * - Viewing and managing files ([FileScreen])
 *
 * Navigation transitions are set to instantly fade between screens for a seamless and subtle effect:
 * - `enterTransition`, `popEnterTransition`: Fade in with no delay.
 * - `exitTransition`, `popExitTransition`: Fade out with no delay.
 *
 * @param rootNavController Navigation controller for switching between root-level screens (e.g. `Home` tabs).
 * @param localNavController Navigation controller scoped to `File`-related screens.
 *
 * @see FileRoutes
 * @see fileNavGraph
 */
@Composable
fun FileNavHost(rootNavController: NavHostController, localNavController: NavHostController) {
    NavHost(
        navController = localNavController,
        startDestination = FileRoutes.FileMain.route,
        enterTransition = { fadeIn(animationSpec = tween(0)) },
        exitTransition = { fadeOut(animationSpec = tween(0)) },
        popEnterTransition = { fadeIn(animationSpec = tween(0)) },
        popExitTransition = { fadeOut(animationSpec = tween(0)) }
    ) {
        fileNavGraph(localNavController, rootNavController)
    }
}

/**
 * Defines the navigation graph for the `File` screens.
 *
 * This graph includes:
 * - [FileScreen]: Displays all `Files` for the `User`.
 *
 * Navigation is handled via [localNavController] for `File` screens,
 * and [rootNavController] for navigating between root-level `Home` tabs.
 *
 * @param localNavController The controller that handles navigation between `File` screens.
 * @param rootNavController The controller that handles navigation between `Home` screens.
 *
 * @see FileRoutes
 * @see FileScreen
 */
fun NavGraphBuilder.fileNavGraph(
    localNavController: NavController,
    rootNavController: NavHostController
) {
    // --- Main File Screen ---
    composable(FileRoutes.FileMain.route) {
        FileScreen(rootNavController)
    }
}