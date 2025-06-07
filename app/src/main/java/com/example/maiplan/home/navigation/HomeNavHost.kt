package com.example.maiplan.home.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.maiplan.home.event.EventScreenManager
import com.example.maiplan.home.task.TaskScreenManager
import com.example.maiplan.home.more.MoreScreenManager
import com.example.maiplan.home.file.FileScreenManager

@Composable
fun HomeNavHost(rootNavController: NavHostController) {
    NavHost(
        navController = rootNavController,
        startDestination = HomeNavRoutes.Events.route,
        enterTransition = { fadeIn(animationSpec = tween(0)) },
        exitTransition = { fadeOut(animationSpec = tween(0)) },
        popEnterTransition = { fadeIn(animationSpec = tween(0)) },
        popExitTransition = { fadeOut(animationSpec = tween(0)) }
    ) {
        homeNavGraph(rootNavController)
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
    composable(HomeNavRoutes.Files.route) {
        FileScreenManager(rootNavController)
    }
    composable(HomeNavRoutes.More.route) {
        MoreScreenManager(rootNavController)
    }
}