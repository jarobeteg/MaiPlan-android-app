package com.example.maiplan.main.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.maiplan.main.screens.ForgotPasswordScreen
import com.example.maiplan.main.screens.LoginScreen
import com.example.maiplan.main.screens.RegisterScreen
import com.example.maiplan.network.api.UserLogin
import com.example.maiplan.network.api.UserRegister
import com.example.maiplan.network.api.UserResetPassword
import com.example.maiplan.utils.common.PasswordUtils
import com.example.maiplan.viewmodel.auth.AuthViewModel

@Composable
fun AuthNavHost(viewModel: AuthViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = MainRoutes.Login.route,
        enterTransition = { fadeIn(animationSpec = tween(0)) },
        exitTransition = { fadeOut(animationSpec = tween(0)) },
        popEnterTransition = { fadeIn(animationSpec = tween(0)) },
        popExitTransition = { fadeOut(animationSpec = tween(0)) }
    ) {
        authNavGraph(navController, viewModel)
    }
}

fun NavGraphBuilder.authNavGraph(
    navController: NavController,
    viewModel: AuthViewModel,
) {
    // --- Login Screen ---
    composable(MainRoutes.Login.route) {
        val isServerReachable by viewModel.isServerReachable.collectAsStateWithLifecycle(false)
        LoginScreen(
            viewModel = viewModel,
            onLoginClick = { email, password ->
                if (isServerReachable) {
                    viewModel.login(UserLogin(email, password))
                } else {
                    viewModel.localLogin(UserLogin(email, password))
                }
            },
            toRegisterClick = {
                if (isServerReachable) {
                    viewModel.clearErrors()
                    navController.navigate(MainRoutes.Register.route)
                }
            },
            toForgotPasswordClick = {
                if (isServerReachable) {
                    viewModel.clearErrors()
                    navController.navigate(MainRoutes.ForgotPassword.route)
                }
            }
        )
    }

    // --- Register Screen ---
    composable(MainRoutes.Register.route) {
        RegisterScreen(
            viewModel = viewModel,
            onRegisterClick = { email, username, password, passwordAgain ->
                viewModel.register(UserRegister(email, username, password, passwordAgain))
            },
            onBackToLogin = {
                viewModel.clearErrors()
                navController.popBackStack()
            }
        )
    }

    // --- Forgot Password Screen ---
    composable(MainRoutes.ForgotPassword.route) {
        ForgotPasswordScreen(
            viewModel = viewModel,
            onResetClick = { email, password, passwordAgain ->
                viewModel.resetPassword(UserResetPassword(email, password, passwordAgain))
            },
            onBackToLogin = {
                viewModel.clearErrors()
                navController.popBackStack()
            }
        )
    }
}