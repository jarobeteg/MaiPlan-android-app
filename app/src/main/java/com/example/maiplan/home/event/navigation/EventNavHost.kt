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
            onCreateEventClick = { localNavController.navigate(EventRoutes.Create.route) }
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
            },
            onBackClick = { localNavController.popBackStack() }
        )
    }

    // --- Update Event Screen ---
    composable(EventRoutes.Update.route) {
        UpdateEventScreen(
            eventViewModel = eventViewModel,
            onSaveClick = {},
            onBackClick = { localNavController.popBackStack() }
        )
    }
}