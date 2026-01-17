package com.example.maiplan.home.more.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.maiplan.home.more.screens.*

@Composable
fun MoreNavHost(rootNavController: NavHostController, localNavController: NavHostController) {
    NavHost(
        navController = localNavController,
        startDestination = MoreRoutes.MoreMain.route,
        enterTransition = { fadeIn(animationSpec = tween(0)) },
        exitTransition = { fadeOut(animationSpec = tween(0)) },
        popEnterTransition = { fadeIn(animationSpec = tween(0)) },
        popExitTransition = { fadeOut(animationSpec = tween(0)) }
    ) {
        moreNavGraph(localNavController, rootNavController)
    }
}

fun NavGraphBuilder.moreNavGraph(
    localNavController: NavController,
    rootNavController: NavHostController
) {
    // --- Main More Screen ---
    composable(MoreRoutes.MoreMain.route) {
        MoreScreen(localNavController, rootNavController)
    }
}