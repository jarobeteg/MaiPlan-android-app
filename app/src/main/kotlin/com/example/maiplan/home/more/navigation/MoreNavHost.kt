package com.example.maiplan.home.more.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.maiplan.home.more.screens.*
import com.example.maiplan.network.RetrofitClient
import com.example.maiplan.repository.auth.AccountRepository
import com.example.maiplan.repository.auth.UserLocalDataSource
import com.example.maiplan.utils.SessionManager
import com.example.maiplan.viewmodel.GenericViewModelFactory
import com.example.maiplan.viewmodel.auth.AccountViewModel

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
        moreNavGraph(rootNavController, localNavController)
    }
}

fun NavGraphBuilder.moreNavGraph(
    rootNavController: NavHostController,
    localNavController: NavHostController,
) {
    // --- Main More Screen ---
    composable(MoreRoutes.MoreMain.route) {
        MoreScreen(
            rootNavController = rootNavController,
            onThemeClick = { localNavController.navigate(MoreRoutes.Theme.route) },
            onClockClick = { localNavController.navigate(MoreRoutes.Clock.route) },
            onAccountClick = { localNavController.navigate(MoreRoutes.Account.route) },
        )
    }
    composable(MoreRoutes.Theme.route) {
        ThemeSelectionScreen(onBackClick = { localNavController.popBackStack() })
    }
    composable(MoreRoutes.Clock.route) {
        ClockSelectionScreen(onBackClick = { localNavController.popBackStack() })
    }
    composable(MoreRoutes.Account.route) { entry ->
        val context = LocalContext.current.applicationContext
        val accountViewModel = remember(context, entry) {
            val repository = AccountRepository(
                RetrofitClient.accountApi,
                UserLocalDataSource(context),
                SessionManager(context),
            )
            ViewModelProvider(entry, GenericViewModelFactory { AccountViewModel(repository) })[
                AccountViewModel::class.java
            ]
        }
        AccountScreen(
            accountViewModel = accountViewModel,
            onBackClick = { localNavController.popBackStack() },
        )
    }
}
