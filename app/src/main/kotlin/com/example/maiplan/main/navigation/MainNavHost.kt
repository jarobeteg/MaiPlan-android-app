package com.example.maiplan.main.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.maiplan.main.screens.LoginScreen
import com.example.maiplan.main.screens.RegisterScreen
import com.example.maiplan.network.api.UserLoginRequest
import com.example.maiplan.network.api.UserRegisterRequest
import com.example.maiplan.viewmodel.auth.AuthViewModel
import java.util.UUID

@Composable
fun AuthNavHost(authViewModel: AuthViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = MainRoutes.Login.route,
        enterTransition = { fadeIn(animationSpec = tween(0)) },
        exitTransition = { fadeOut(animationSpec = tween(0)) },
        popEnterTransition = { fadeIn(animationSpec = tween(0)) },
        popExitTransition = { fadeOut(animationSpec = tween(0)) }
    ) {
        authNavGraph(navController, authViewModel)
    }
}

fun NavGraphBuilder.authNavGraph(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    composable(MainRoutes.Login.route) {
        LoginScreen(
            authViewModel = authViewModel,
            onLoginClick = { email, password ->
                authViewModel.login(UserLoginRequest(email, password))
            },
            toRegisterClick = {
                authViewModel.clearErrors()
                navController.navigate(MainRoutes.Register.route)
            }
        )
    }

    composable(MainRoutes.Register.route) {
        val registrationSyncId = rememberSaveable { UUID.randomUUID().toString() }

        RegisterScreen(
            authViewModel = authViewModel,
            onRegisterClick = { email, username, password, passwordAgain ->
                authViewModel.register(
                    UserRegisterRequest(
                        syncId = registrationSyncId,
                        email = email,
                        username = username,
                        password = password,
                        passwordAgain = passwordAgain
                    )
                )
            },
            onBackToLogin = {
                authViewModel.clearErrors()
                navController.popBackStack()
            }
        )
    }
}