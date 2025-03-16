package com.example.maiplan.main_screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.maiplan.network.UserLogin
import com.example.maiplan.network.UserRegister
import com.example.maiplan.network.UserResetPassword
import com.example.maiplan.viewmodel.AuthViewModel

@Composable
fun AuthNavHost(viewModel: AuthViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = MainRoutes.Login.route
    ) {
        authNavGraph(navController, viewModel)
    }
}

fun NavGraphBuilder.authNavGraph(
    navController: NavController,
    viewModel: AuthViewModel
) {
    composable(MainRoutes.Login.route) {
        LoginScreen(
            viewModel = viewModel,
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
