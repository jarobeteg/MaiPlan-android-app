package com.example.maiplan.home.event.navigation

import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.compose.composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.maiplan.home.event.screens.*
import com.example.maiplan.repository.Result
import com.example.maiplan.home.event.EventScreenManager
import java.time.LocalDate

fun NavGraphBuilder.eventNavGraph(navController: NavHostController) {
    // --- Main Event Screen ---
    composable(EventRoutes.EventMain.route) { entry ->
        EventScreenManager(navController, entry) { userLocalId, eventViewModel, _ ->
            val context = LocalContext.current
            val deleteResult by eventViewModel.deleteEventResult.observeAsState()
            EventScreen(
                eventViewModel = eventViewModel,
                rootNavController = navController,
                localNavController = navController,
                onCreateEventClick = { navController.navigate(EventRoutes.Create.route) },
                onUpdateEventClick = { eventLocalId, occurrenceDate ->
                    navController.navigate(EventRoutes.Update.withArgs(eventLocalId, occurrenceDate))
                },
                onDeleteClick = { eventLocalId, selectedDate, occurrenceDate ->
                    eventViewModel.softDeleteEventWithReminder(eventLocalId, userLocalId, selectedDate, occurrenceDate)
                },
                isDeleting = deleteResult is Result.Loading,
            )
            LaunchedEffect(deleteResult) {
                if (deleteResult is Result.Error || deleteResult is Result.Failure) {
                    Toast.makeText(context, "Could not delete the event. Please try again.", Toast.LENGTH_LONG).show()
                    eventViewModel.clearDeleteResult()
                }
            }
        }
    }

    // --- Create Event Screen ---
    composable(EventRoutes.Create.route) { entry ->
        EventScreenManager(navController, entry) { _, eventViewModel, categoryViewModel ->
            val context = LocalContext.current
            val saveResult by eventViewModel.saveEventResult.observeAsState()
            CreateEventScreen(
                categoryViewModel = categoryViewModel,
                isSaving = saveResult is Result.Loading,
                onSaveClick = { reminder, event ->
                    eventViewModel.createEventWithReminder(reminder, event)
                },
                onBackClick = { navController.popBackStack() }
            )
            LaunchedEffect(saveResult) {
                val result = saveResult
                if (result is Result.Error || result is Result.Failure) {
                    Toast.makeText(context, (result as? Result.Error)?.exception?.message
                        ?: "Could not save the event. Please try again.", Toast.LENGTH_LONG).show()
                    eventViewModel.clearSaveResult()
                }
                if (result is Result.Success) {
                    result.data.reminderWarning?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
                    eventViewModel.clearSaveResult()
                    navController.popBackStack()
                }
            }
        }
    }

    // --- Update Event Screen ---
    composable(
        route = EventRoutes.Update.route,
        arguments = listOf(
            navArgument("eventLocalId") { type = NavType.LongType },
            navArgument("occurrenceDay") { type = NavType.StringType; nullable = true; defaultValue = null },
        )
    ) { backstackEntry ->
        val eventLocalId = backstackEntry
            .arguments
            ?.getLong("eventLocalId")
            ?: return@composable
        val occurrenceDate = backstackEntry.arguments?.getString("occurrenceDay")
            ?.toLongOrNull()?.let(LocalDate::ofEpochDay)
        EventScreenManager(navController, backstackEntry) { _, eventViewModel, categoryViewModel ->
            val context = LocalContext.current
            val saveResult by eventViewModel.saveEventResult.observeAsState()
            UpdateEventScreen(
                eventLocalId = eventLocalId,
                eventViewModel = eventViewModel,
                categoryViewModel = categoryViewModel,
                isSaving = saveResult is Result.Loading,
                occurrenceDate = occurrenceDate,
                onUpdateClick = { reminder, event ->
                    eventViewModel.updateEventWithReminder(reminder, event, occurrenceDate)
                },
                onBackClick = { navController.popBackStack() }
            )
            LaunchedEffect(saveResult) {
                val result = saveResult
                if (result is Result.Error || result is Result.Failure) {
                    Toast.makeText(context, (result as? Result.Error)?.exception?.message
                        ?: "Could not save the event. Please try again.", Toast.LENGTH_LONG).show()
                    eventViewModel.clearSaveResult()
                }
                if (result is Result.Success) {
                    result.data.reminderWarning?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
                    eventViewModel.clearSaveResult()
                    navController.popBackStack()
                }
            }
        }
    }
}
