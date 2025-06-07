package com.example.maiplan.home.more.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.maiplan.home.more.screens.MoreScreen

@Composable
fun MoreNavHost(rootNavController: NavHostController, localNavController: NavHostController) {
    NavHost(
        navController = localNavController,
        startDestination = MoreRoutes.MoreMain.route
    ) {
        eventNavGraph(localNavController, rootNavController)
    }
}

fun NavGraphBuilder.eventNavGraph(
    localNavController: NavController,
    rootNavController: NavHostController
) {
    composable(MoreRoutes.MoreMain.route) {
        MoreScreen(rootNavController)
    }
}