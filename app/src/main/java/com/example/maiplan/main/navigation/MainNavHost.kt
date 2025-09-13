package com.example.maiplan.main.navigation

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
import com.example.maiplan.repository.Result
import com.example.maiplan.utils.common.PasswordUtils
import com.example.maiplan.utils.common.PasswordValidationException
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
    viewModel: AuthViewModel
) {
    // --- Login Screen ---
    composable(MainRoutes.Login.route) {
        LoginScreen(
            viewModel = viewModel,
            onLoginClick = { email, password ->
                try {
                    val trimmedPassword = PasswordUtils.validatePassword(password)
                    val passwordHash = PasswordUtils.hashPassword(trimmedPassword)
                    println("password hash: $passwordHash")
                    viewModel.login(UserLogin(email, passwordHash))

                } catch (e: PasswordValidationException) {
                    viewModel.setLoginError(Result.Failure(errorCode = e.code))
                }
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
            onRegisterClick = { email, username, password, passwordAgain ->
                try {
                    val trimmedPassword = PasswordUtils.validatePassword(password)
                    PasswordUtils.validatePasswordStrength(trimmedPassword)
                    PasswordUtils.validatePasswordMatch(trimmedPassword, passwordAgain)
                    val passwordHash = PasswordUtils.hashPassword(trimmedPassword)

                    viewModel.register(UserRegister(email, username, passwordHash))

                } catch (e: PasswordValidationException) {
                    viewModel.setRegisterError(Result.Failure(errorCode = e.code))
                }
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
                try {
                    val trimmedPassword = PasswordUtils.validatePassword(password)
                    PasswordUtils.validatePasswordStrength(trimmedPassword)
                    PasswordUtils.validatePasswordMatch(trimmedPassword, passwordAgain)
                    val passwordHash = PasswordUtils.hashPassword(trimmedPassword)

                    viewModel.resetPassword(UserResetPassword(email, passwordHash))

                } catch (e: PasswordValidationException) {
                    viewModel.setResetPasswordError(Result.Failure(errorCode = e.code))
                }
            },
            onBackToLogin = {
                viewModel.clearErrors()
                navController.popBackStack()
            }
        )
    }
}