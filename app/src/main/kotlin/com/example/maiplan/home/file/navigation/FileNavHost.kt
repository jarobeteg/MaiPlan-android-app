package com.example.maiplan.home.file.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
        startDestination = FileRoutes.FileMain.route,
        enterTransition = { fadeIn(animationSpec = tween(0)) },
        exitTransition = { fadeOut(animationSpec = tween(0)) },
        popEnterTransition = { fadeIn(animationSpec = tween(0)) },
        popExitTransition = { fadeOut(animationSpec = tween(0)) }
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