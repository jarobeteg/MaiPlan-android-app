package com.example.maiplan.main.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.example.maiplan.navigation.AppNavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.maiplan.main.screens.LoginScreen
import com.example.maiplan.main.screens.ForgotPasswordScreen
import com.example.maiplan.main.screens.RegisterScreen
import com.example.maiplan.network.api.UserLoginRequest
import com.example.maiplan.network.api.UserPasswordResetRequest
import com.example.maiplan.network.api.UserRegisterRequest
import com.example.maiplan.viewmodel.auth.AuthViewModel
import java.util.UUID

@Composable
fun AuthNavHost(authViewModel: AuthViewModel) {
    val navController = rememberNavController()

    AppNavHost(
        navController = navController,
        startDestination = MainRoutes.Login.route,
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
            },
            toForgotPasswordClick = {
                authViewModel.clearErrors()
                navController.navigate(MainRoutes.ForgotPassword.route)
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

    composable(MainRoutes.ForgotPassword.route) {
        ForgotPasswordScreen(
            authViewModel = authViewModel,
            onResetPasswordClick = { email, password, passwordAgain ->
                authViewModel.resetPassword(
                    UserPasswordResetRequest(email, password, passwordAgain)
                )
            },
            onBackToLogin = {
                authViewModel.clearErrors()
                navController.popBackStack()
            }
        )
    }
}
