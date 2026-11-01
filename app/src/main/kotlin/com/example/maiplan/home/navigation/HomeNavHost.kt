package com.example.maiplan.home.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.example.maiplan.navigation.AppNavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.maiplan.home.event.navigation.EventRoutes
import com.example.maiplan.home.event.navigation.eventNavGraph
import com.example.maiplan.home.screens.HomeScreen
import com.example.maiplan.home.task.navigation.TaskRoutes
import com.example.maiplan.home.task.navigation.taskNavGraph
import com.example.maiplan.home.more.MoreScreenManager
import com.example.maiplan.home.note.navigation.NoteRoutes
import com.example.maiplan.home.note.navigation.noteNavGraph
import com.example.maiplan.utils.LocalAdaptiveLayout

@Composable
fun HomeNavHost(rootNavController: NavHostController, notificationTaskId: Long? = null, onNotificationHandled: () -> Unit = {}) {
    val adaptiveLayout = LocalAdaptiveLayout.current
    val context = LocalContext.current
    val useRail = adaptiveLayout.useHomeNavigationRail
    val safeInsets = if (useRail) {
        WindowInsets.safeDrawing
    } else {
        WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(safeInsets),
    ) {
        if (useRail) {
            HomeNavigationRail(rootNavController, context)
        }

        AppNavHost(
            navController = rootNavController,
            startDestination = HomeNavRoutes.Events.route,
            modifier = Modifier.weight(1f),
        ) {
            navigation(startDestination = EventRoutes.EventMain.route, route = HomeNavRoutes.Events.route) {
                eventNavGraph(rootNavController)
            }
            navigation(startDestination = TaskRoutes.TaskMain.route, route = HomeNavRoutes.Tasks.route) {
                taskNavGraph(rootNavController)
            }
            navigation(startDestination = NoteRoutes.NoteMain.route, route = HomeNavRoutes.Notes.route) {
                noteNavGraph(rootNavController)
            }
            composable(HomeNavRoutes.Home.route) {
                HomeScreen(rootNavController,
                    onOpenTask = {
                        rootNavController.openHomeTask(it)
                    },
                    onOpenEvent = { id, occurrence ->
                        rootNavController.openHomeEvent(id, occurrence)
                    },
                    onOpenNote = { rootNavController.openHomeNote(it) },
                    onAllEvents = { rootNavController.navigateHome(HomeNavRoutes.Events) },
                    onAllTasks = { rootNavController.navigateHome(HomeNavRoutes.Tasks) },
                    onAllNotes = { rootNavController.navigateHome(HomeNavRoutes.Notes) },
                )
            }
            composable(HomeNavRoutes.More.route) { MoreScreenManager(rootNavController) }
        }
    }
    LaunchedEffect(notificationTaskId) {
        if (notificationTaskId != null) {
            rootNavController.openHomeTask(notificationTaskId)
            onNotificationHandled()
        }
    }
}
