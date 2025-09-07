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
import com.example.maiplan.database.entities.AuthEntity
import com.example.maiplan.main.screens.ForgotPasswordScreen
import com.example.maiplan.main.screens.LoginScreen
import com.example.maiplan.main.screens.RegisterScreen
import com.example.maiplan.network.api.UserLogin
import com.example.maiplan.network.api.UserRegister
import com.example.maiplan.network.api.UserResetPassword
import com.example.maiplan.utils.common.JWTUtils
import com.example.maiplan.utils.common.PasswordUtils
import com.example.maiplan.viewmodel.auth.AuthViewModel

/**
 * [Composable] that sets up the navigation host for the `Authentication` screens.
 *
 * This host defines the entry point and connects the navigation graph for:
 * - Logging in an existing user ([LoginScreen])
 * - Registering a new user ([RegisterScreen])
 * - Resetting a password for an existing user ([ForgotPasswordScreen])
 *
 * @param viewModel Shared `ViewModel` for performing authentication-related operations.
 *
 * @see AuthViewModel
 * @see MainRoutes
 * @see authNavGraph
 */
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

/**
 * Defines the navigation graph for the `Authentication` screens.
 *
 * This graph includes:
 * - [LoginScreen]: Allows the user to log in.
 * - [RegisterScreen]: Allows the user to create a new account.
 * - [ForgotPasswordScreen]: Allows the user to reset their password.
 *
 * Navigation between screens is handled by [navController].
 * The [viewModel] is passed to all screens to ensure shared state.
 *
 * @param navController The controller that handles navigation between screens.
 * @param viewModel The `ViewModel` providing data and logic for the `Authentication` screens.
 *
 * @see LoginScreen
 * @see RegisterScreen
 * @see ForgotPasswordScreen
 * @see AuthViewModel
 * @see UserLogin
 * @see UserRegister
 * @see UserResetPassword
 */
fun NavGraphBuilder.authNavGraph(
    navController: NavController,
    viewModel: AuthViewModel
) {
    // --- Login Screen ---
    composable(MainRoutes.Login.route) {
        LoginScreen(
            viewModel = viewModel,
            onLoginClick = { email, password ->
                //login logic through viewmodel to implement
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
                viewModel.localRegister(AuthEntity(
                    email = email,
                    username = username,
                    passwordHash = PasswordUtils.hashPassword(password)
                ))
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
                //password reset logic through viewmodel to implement
            },
            onBackToLogin = {
                viewModel.clearErrors()
                navController.popBackStack()
            }
        )
    }
}