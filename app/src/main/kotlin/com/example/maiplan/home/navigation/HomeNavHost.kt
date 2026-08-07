package com.example.maiplan.home.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.maiplan.home.event.EventScreenManager
import com.example.maiplan.home.task.TaskScreenManager
import com.example.maiplan.home.more.MoreScreenManager
import com.example.maiplan.home.note.NoteScreenManager
import com.example.maiplan.utils.LocalAdaptiveLayout

@Composable
fun HomeNavHost(rootNavController: NavHostController) {
    val adaptiveLayout = LocalAdaptiveLayout.current
    val context = LocalContext.current

    Row(modifier = Modifier.fillMaxSize()) {
        if (adaptiveLayout.useNavigationRail) {
            HomeNavigationRail(rootNavController, context)
        }

        NavHost(
            navController = rootNavController,
            startDestination = HomeNavRoutes.Events.route,
            modifier = Modifier.weight(1f),
            enterTransition = { fadeIn(animationSpec = tween(0)) },
            exitTransition = { fadeOut(animationSpec = tween(0)) },
            popEnterTransition = { fadeIn(animationSpec = tween(0)) },
            popExitTransition = { fadeOut(animationSpec = tween(0)) }
        ) {
            homeNavGraph(rootNavController)
        }
    }
}

fun NavGraphBuilder.homeNavGraph(
    rootNavController: NavHostController
) {
    composable(HomeNavRoutes.Events.route) {
        EventScreenManager(rootNavController)
    }
    composable(HomeNavRoutes.Tasks.route) {
        TaskScreenManager(rootNavController)
    }
    composable(HomeNavRoutes.Notes.route) {
        NoteScreenManager(rootNavController)
    }
    composable(HomeNavRoutes.More.route) {
        MoreScreenManager(rootNavController)
    }
}
