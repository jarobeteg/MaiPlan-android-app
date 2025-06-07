package com.example.maiplan.home.event.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.example.maiplan.home.event.screens.CreateEventScreen
import com.example.maiplan.home.event.screens.EventScreen
import com.example.maiplan.viewmodel.EventViewModel

/**
 * Composable that sets up the navigation host for the Event screens.
 *
 * It defines the entry point and connects the navigation graph for managing events,
 * viewing, creating, and updating events.
 *
 * @param eventViewModel The ViewModel shared across event-related screens,
 * used for performing CRUD operations on events.
 *
 * @see EventRoutes
 * @see EventViewModel
 */
@Composable
fun EventNavHost(rootNavController: NavHostController, localNavController: NavHostController, eventViewModel: EventViewModel) {
    NavHost(
        navController = localNavController,
        startDestination = EventRoutes.EventMain.route
    ) {
        eventNavGraph(localNavController, rootNavController, eventViewModel)
    }
}

/**
 * Defines the navigation graph for the Event screens.
 *
 * This graph includes:
 * - Event Screen With Nav: Displays all events for the selected view (Monthly, Weekly, Daily).
 * - Create Event Screen: Allows user to create a new event.
 *
 * The navigation between screens are handled by the [navController].
 * The [eventViewModel] is passed to all screens to ensure shared state.
 *
 * @param navController The controller that handles navigation between screens.
 * @param eventViewModel The ViewModel providing data and logic for the event screens.
 *
 * @see EventViewModel
 * @see EventRoutes
 * @see EventScreen
 * @see CreateEventScreen
 */
fun NavGraphBuilder.eventNavGraph(
    localNavController: NavController,
    rootNavController: NavHostController,
    eventViewModel: EventViewModel
) {
    // --- Main Event Screen ---
    composable(EventRoutes.EventMain.route) {
        /**
         * Displays all events for the selected view (Monthly, Weekly, Daily).
         *
         * @param viewModel Used to fetch and manage event data.
         * @param onCreateEventClick Called when the user clicks the add new event button.
         */
        EventScreen(
            viewModel = eventViewModel,
            rootNavController = rootNavController,
            onCreateEventClick = { localNavController.navigate(EventRoutes.Create.route) }
        )
    }

    // --- Create Event Screen ---
    composable(EventRoutes.Create.route) {
        /**
         * Screen for creating a new event.
         *
         * @param viewModel Supplies the creation logic.
         * @param onSaveClick Called when the user submits the event creation form.
         * @param onBackClick Cancels creating and pops back without saving.
         */
        CreateEventScreen(
            viewModel = eventViewModel,
            onSaveClick = {},
            onBackClick = { localNavController.popBackStack() }
        )
    }
}