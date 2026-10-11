package com.example.maiplan.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.integerResource
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.example.maiplan.R

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier,
    builder: NavGraphBuilder.() -> Unit,
) {
    val durationMillis = integerResource(R.integer.app_screen_transition_duration_ms)
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = { fadeIn(animationSpec = tween(durationMillis)) },
        exitTransition = { fadeOut(animationSpec = tween(durationMillis)) },
        popEnterTransition = { fadeIn(animationSpec = tween(durationMillis)) },
        popExitTransition = { fadeOut(animationSpec = tween(durationMillis)) },
        builder = builder,
    )
}
