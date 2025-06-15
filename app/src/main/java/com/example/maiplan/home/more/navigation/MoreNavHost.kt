package com.example.maiplan.home.more.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.maiplan.home.more.screens.*

/**
 * [Composable] that sets up the navigation host for the `More` screens.
 *
 * This host defines the entry point and connects the navigation graph for:
 * - Viewing more options ([MoreScreen])
 *
 * Navigation transitions are set to instantly fade between screens for a seamless and subtle effect:
 * - `enterTransition`, `popEnterTransition`: Fade in with no delay.
 * - `exitTransition`, `popExitTransition`: Fade out with no delay.
 *
 * @param rootNavController Navigation controller for switching between root-level screens (e.g. `Home` tabs).
 * @param localNavController Navigation controller scoped to `More`-related screens.
 *
 * @see MoreRoutes
 * @see moreNavGraph
 */
@Composable
fun MoreNavHost(rootNavController: NavHostController, localNavController: NavHostController) {
    NavHost(
        navController = localNavController,
        startDestination = MoreRoutes.MoreMain.route,
        enterTransition = { fadeIn(animationSpec = tween(0)) },
        exitTransition = { fadeOut(animationSpec = tween(0)) },
        popEnterTransition = { fadeIn(animationSpec = tween(0)) },
        popExitTransition = { fadeOut(animationSpec = tween(0)) }
    ) {
        moreNavGraph(localNavController, rootNavController)
    }
}

/**
 * Defines the navigation graph for the `More` screens.
 *
 * This graph includes:
 * - [MoreScreen]: Displays all `More` options for the `User`.
 *
 * Navigation is handled via [localNavController] for `More` screens,
 * and [rootNavController] for navigating between root-level `Home` tabs.
 *
 * @param localNavController The controller that handles navigation between `More` screens.
 * @param rootNavController The controller that handles navigation between `Home` screens.
 *
 * @see MoreRoutes
 * @see MoreScreen
 */
fun NavGraphBuilder.moreNavGraph(
    localNavController: NavController,
    rootNavController: NavHostController
) {
    // --- Main More Screen ---
    composable(MoreRoutes.MoreMain.route) {
        MoreScreen(rootNavController)
    }
}