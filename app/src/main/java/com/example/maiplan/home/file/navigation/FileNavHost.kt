package com.example.maiplan.home.file.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.maiplan.home.file.screens.FileScreen

@Composable
fun FileNavHost(rootNavController: NavHostController, localNavController: NavHostController) {
    NavHost(
        navController = localNavController,
        startDestination = FileRoutes.FileMain.route
    ) {
        eventNavGraph(localNavController, rootNavController)
    }
}

fun NavGraphBuilder.eventNavGraph(
    localNavController: NavController,
    rootNavController: NavHostController
) {
    composable(FileRoutes.FileMain.route) {
        FileScreen(rootNavController)
    }
}