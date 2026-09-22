package com.example.maiplan.home.event.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.navArgument
import com.example.maiplan.home.event.screens.*
import com.example.maiplan.repository.Result
import com.example.maiplan.utils.common.UserSession
import com.example.maiplan.utils.notifications.AlarmScheduler
import com.example.maiplan.utils.notifications.ReminderData
import com.example.maiplan.viewmodel.category.CategoryViewModel
import com.example.maiplan.viewmodel.event.EventViewModel

@Composable
fun EventNavHost(
    rootNavController: NavHostController,
    localNavController: NavHostController,
    eventViewModel: EventViewModel,
    categoryViewModel: CategoryViewModel) {
    NavHost(
        navController = localNavController,
        startDestination = EventRoutes.EventMain.route,
        enterTransition = { fadeIn(animationSpec = tween(0)) },
        exitTransition = { fadeOut(animationSpec = tween(0)) },
        popEnterTransition = { fadeIn(animationSpec = tween(0)) },
        popExitTransition = { fadeOut(animationSpec = tween(0)) }
    ) {
        eventNavGraph(localNavController, rootNavController, eventViewModel, categoryViewModel)
    }
}

fun NavGraphBuilder.eventNavGraph(
    localNavController: NavHostController,
    rootNavController: NavHostController,
    eventViewModel: EventViewModel,
    categoryViewModel: CategoryViewModel
) {

    val userLocalId = UserSession.userLocalId!!
    // --- Main Event Screen ---
    composable(EventRoutes.EventMain.route) {
        EventScreen(
            eventViewModel = eventViewModel,
            rootNavController = rootNavController,
            localNavController = localNavController,
            onCreateEventClick = { localNavController.navigate(EventRoutes.Create.route) },
            onUpdateEventClick = { eventLocalId -> localNavController.navigate(EventRoutes.Update.withArgs(eventLocalId)) },
            onDeleteClick = { eventLocalId, selectedDate->
                eventViewModel.softDeleteEventWithReminder(eventLocalId, userLocalId, selectedDate)
            },
        )
    }

    // --- Create Event Screen ---
    composable(EventRoutes.Create.route) {
        val context = LocalContext.current
        val saveResult by eventViewModel.saveEventResult.observeAsState()
        CreateEventScreen(
            categoryViewModel = categoryViewModel,
            onSaveClick = { reminder, event ->
                eventViewModel.createEventWithReminder(reminder, event)
            },
            onBackClick = { localNavController.popBackStack() }
        )
        LaunchedEffect(saveResult) {
            val result = saveResult
            if (result is Result.Success) {
                result.data.reminder?.let { reminder ->
                    val reminderData = ReminderData(
                        reminderLocalId = reminder.reminderLocalId,
                        reminderTime = reminder.reminderTime,
                        reminderTitle = result.data.event.title,
                        reminderMessage = reminder.message.orEmpty()
                    )
                    if (!AlarmScheduler.attemptSchedule(context, reminderData)) {
                        AlarmScheduler.requestExactAlarmPermission(context)
                    }
                }
                eventViewModel.clearSaveResult()
                localNavController.popBackStack()
            }
        }
    }

    // --- Update Event Screen ---
    composable(
        route = EventRoutes.Update.route,
        arguments = listOf(
            navArgument("eventLocalId") { type = NavType.LongType }
        )
    ) { backstackEntry ->
        val context = LocalContext.current
        val saveResult by eventViewModel.saveEventResult.observeAsState()
        val eventLocalId = backstackEntry
            .arguments
            ?.getLong("eventLocalId")
            ?: return@composable
        UpdateEventScreen(
            eventLocalId = eventLocalId,
            eventViewModel = eventViewModel,
            categoryViewModel = categoryViewModel,
            onUpdateClick = { reminder, event ->
                eventViewModel.updateEventWithReminder(reminder, event)
            },
            onBackClick = { localNavController.popBackStack() }
        )
        LaunchedEffect(saveResult) {
            val result = saveResult
            if (result is Result.Success) {
                result.data.removedReminderLocalId?.let {
                    AlarmScheduler.cancelAlarm(context, it)
                }
                result.data.reminder?.let { reminder ->
                    AlarmScheduler.attemptSchedule(
                        context,
                        ReminderData(
                            reminderLocalId = reminder.reminderLocalId,
                            reminderTime = reminder.reminderTime,
                            reminderTitle = result.data.event.title,
                            reminderMessage = reminder.message.orEmpty()
                        )
                    )
                }
                eventViewModel.clearSaveResult()
                localNavController.popBackStack()
            }
        }
    }
}
