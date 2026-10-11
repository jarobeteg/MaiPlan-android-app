package com.example.maiplan.home.file.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.example.maiplan.navigation.AppNavHost
import androidx.navigation.compose.composable
import com.example.maiplan.home.file.screens.FileScreen

@Composable
fun FileNavHost(rootNavController: NavHostController, localNavController: NavHostController) {
    AppNavHost(
        navController = localNavController,
        startDestination = FileRoutes.FileMain.route,
    ) {
        fileNavGraph(localNavController, rootNavController)
    }
}

fun NavGraphBuilder.fileNavGraph(
    localNavController: NavController,
    rootNavController: NavHostController
) {
    // --- Main File Screen ---
    composable(FileRoutes.FileMain.route) {
        FileScreen(rootNavController)
    }
}
