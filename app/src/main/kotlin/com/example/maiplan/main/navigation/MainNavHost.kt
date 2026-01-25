package com.example.maiplan.main.navigation

import android.app.Activity
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
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
import com.example.maiplan.viewmodel.auth.AuthViewModel

@Composable
fun AuthNavHost(viewModel: AuthViewModel, activity: Activity) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = MainRoutes.Login.route,
        enterTransition = { fadeIn(animationSpec = tween(0)) },
        exitTransition = { fadeOut(animationSpec = tween(0)) },
        popEnterTransition = { fadeIn(animationSpec = tween(0)) },
        popExitTransition = { fadeOut(animationSpec = tween(0)) }
    ) {
        authNavGraph(navController, viewModel, activity)
    }
}

fun NavGraphBuilder.authNavGraph(
    navController: NavController,
    viewModel: AuthViewModel,
    activity: Activity
) {
    // --- Login Screen ---
    composable(MainRoutes.Login.route) {
        LoginScreen(
            viewModel = viewModel,
            activity = activity,
            onLoginClick = { email, password ->
                viewModel.login(UserLogin(email, password))
            },
            toRegisterClick = {
                viewModel.clearErrors()
                navController.navigate(MainRoutes.Register.route)
            },
            toForgotPasswordClick = {
                viewModel.clearErrors()
                navController.navigate(MainRoutes.ForgotPassword.route)
            }
        )
    }

    // --- Register Screen ---
    composable(MainRoutes.Register.route) {
        RegisterScreen(
            viewModel = viewModel,
            activity = activity,
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
            activity = activity,
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