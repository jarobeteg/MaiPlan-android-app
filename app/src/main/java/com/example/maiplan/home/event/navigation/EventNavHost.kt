package com.example.maiplan.home.event.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.compose.composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.example.maiplan.home.event.screens.*
import com.example.maiplan.viewmodel.event.EventViewModel

/**
 * [Composable] that sets up the navigation host for the `Event` screens.
 *
 * This host defines the entry point and connects the navigation graph for:
 * - Viewing and managing events ([EventScreen])
 * - Creating a new event ([CreateEventScreen])
 * - Updating an existing event ([UpdateEventScreen])
 *
 * Navigation transitions are set to instantly fade between screens for a seamless and subtle effect:
 * - `enterTransition`, `popEnterTransition`: Fade in with no delay.
 * - `exitTransition`, `popExitTransition`: Fade out with no delay.
 *
 * @param rootNavController Navigation controller for switching between root-level screens (e.g. `Home` tabs).
 * @param localNavController Navigation controller scoped to `Event`-related screens.
 * @param eventViewModel Shared `ViewModel` for performing `CRUD` operations on events.
 *
 * @see EventViewModel
 * @see EventRoutes
 * @see eventNavGraph
 */
@Composable
fun EventNavHost(rootNavController: NavHostController, localNavController: NavHostController, eventViewModel: EventViewModel) {
    NavHost(
        navController = localNavController,
        startDestination = EventRoutes.EventMain.route,
        enterTransition = { fadeIn(animationSpec = tween(0)) },
        exitTransition = { fadeOut(animationSpec = tween(0)) },
        popEnterTransition = { fadeIn(animationSpec = tween(0)) },
        popExitTransition = { fadeOut(animationSpec = tween(0)) }
    ) {
        eventNavGraph(localNavController, rootNavController, eventViewModel)
    }
}

/**
 * Defines the navigation graph for the `Event` screens.
 *
 * This graph includes:
 * - [EventScreen]: Displays all `Events` for the selected view ([MonthlyView], [WeeklyView], [WeeklyView]).
 * - [CreateEventScreen]: Allows `User` to create a new `Event`.
 * - [UpdateEventScreen]: Enables editing of an existing `Event` by its Id.
 *
 * Navigation is handled via [localNavController] for `Event` screens,
 * and [rootNavController] for navigating between root-level `Home` tabs.
 * The [eventViewModel] is passed to all screens to ensure shared state.
 *
 * @param localNavController The controller that handles navigation between `Event` screens.
 * @param rootNavController The controller that handles navigation between `Home` screens.
 * @param eventViewModel The `ViewModel` providing data and logic for the `Event` screens.
 *
 * @see EventViewModel
 * @see EventRoutes
 * @see EventScreen
 * @see CreateEventScreen
 * @see UpdateEventScreen
 */
fun NavGraphBuilder.eventNavGraph(
    localNavController: NavHostController,
    rootNavController: NavHostController,
    eventViewModel: EventViewModel
) {
    // --- Main Event Screen ---
    composable(EventRoutes.EventMain.route) {
        EventScreen(
            viewModel = eventViewModel,
            rootNavController = rootNavController,
            localNavController = localNavController,
            onCreateEventClick = { localNavController.navigate(EventRoutes.Create.route) }
        )
    }

    // --- Create Event Screen ---
    composable(EventRoutes.Create.route) {
        CreateEventScreen(
            viewModel = eventViewModel,
            onSaveClick = {},
            onBackClick = { localNavController.popBackStack() }
        )
    }

    // --- Update Event Screen ---
    composable(EventRoutes.Update.route) {
        UpdateEventScreen(
            viewModel = eventViewModel,
            onSaveClick = {},
            onBackClick = { localNavController.popBackStack() }
        )
    }
}