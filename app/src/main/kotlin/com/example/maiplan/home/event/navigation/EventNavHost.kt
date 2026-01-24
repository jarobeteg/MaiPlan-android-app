package com.example.maiplan.home.event.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.compose.composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.navArgument
import com.example.maiplan.home.event.screens.*
import com.example.maiplan.viewmodel.category.CategoryViewModel
import com.example.maiplan.viewmodel.event.EventViewModel
import com.example.maiplan.viewmodel.reminder.ReminderViewModel

@Composable
fun EventNavHost(
    rootNavController: NavHostController,
    localNavController: NavHostController,
    eventViewModel: EventViewModel,
    categoryViewModel: CategoryViewModel,
    reminderViewModel: ReminderViewModel) {
    NavHost(
        navController = localNavController,
        startDestination = EventRoutes.EventMain.route,
        enterTransition = { fadeIn(animationSpec = tween(0)) },
        exitTransition = { fadeOut(animationSpec = tween(0)) },
        popEnterTransition = { fadeIn(animationSpec = tween(0)) },
        popExitTransition = { fadeOut(animationSpec = tween(0)) }
    ) {
        eventNavGraph(localNavController, rootNavController, eventViewModel, categoryViewModel, reminderViewModel)
    }
}

fun NavGraphBuilder.eventNavGraph(
    localNavController: NavHostController,
    rootNavController: NavHostController,
    eventViewModel: EventViewModel,
    categoryViewModel: CategoryViewModel,
    reminderViewModel: ReminderViewModel
) {
    // --- Main Event Screen ---
    composable(EventRoutes.EventMain.route) {
        EventScreen(
            eventViewModel = eventViewModel,
            rootNavController = rootNavController,
            localNavController = localNavController,
            onCreateEventClick = { localNavController.navigate(EventRoutes.Create.route) },
            onUpdateEventClick = { eventId -> localNavController.navigate(EventRoutes.Update.withArgs(eventId)) }
        )
    }

    // --- Create Event Screen ---
    composable(EventRoutes.Create.route) {
        CreateEventScreen(
            eventViewModel = eventViewModel,
            categoryViewModel = categoryViewModel,
            reminderViewModel = reminderViewModel,
            onSaveClick = { reminder, event ->
                eventViewModel.createEventWithReminder(reminder, event)
                localNavController.popBackStack()
            },
            onBackClick = { localNavController.popBackStack() }
        )
    }

    // --- Update Event Screen ---
    composable(
        route = EventRoutes.Update.route,
        arguments = listOf(
            navArgument("eventId") { type = NavType.IntType }
        )
    ) { backstackEntry ->
        val eventId = backstackEntry
            .arguments
            ?.getInt("eventId")
            ?: return@composable
        UpdateEventScreen(
            eventId = eventId,
            eventViewModel = eventViewModel,
            categoryViewModel = categoryViewModel,
            reminderViewModel = reminderViewModel,
            onUpdateClick = {},
            onDeleteClick = {},
            onBackClick = { localNavController.popBackStack() }
        )
    }
}