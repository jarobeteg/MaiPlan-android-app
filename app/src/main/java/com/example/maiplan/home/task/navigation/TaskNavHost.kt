package com.example.maiplan.home.task.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.maiplan.home.task.screens.TaskScreen

@Composable
fun TaskNavHost(rootNavController: NavHostController, localNavController: NavHostController) {
    NavHost(
        navController = localNavController,
        startDestination = TaskRoutes.TaskMain.route
    ) {
        eventNavGraph(localNavController, rootNavController)
    }
}

fun NavGraphBuilder.eventNavGraph(
    localNavController: NavController,
    rootNavController: NavHostController
) {
    composable(TaskRoutes.TaskMain.route) {
        TaskScreen(rootNavController)
    }
}