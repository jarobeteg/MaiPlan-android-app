package com.example.maiplan.main.navigation

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
import com.example.maiplan.viewmodel.AuthViewModel

/**
 * Composable that sets up the navigation host for the Authentication screens.
 *
 * It defines the entry point and connects the navigation graph for managing login,
 * registration, and password reset flows.
 *
 * @param viewModel The ViewModel shared across authentication-related screens,
 * used for performing login, registration, and password reset operations.
 *
 * @see AuthViewModel
 * @see MainRoutes
 */
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

/**
 * Defines the navigation graph for the Authentication screens.
 *
 * This graph includes:
 * - Login Screen: Allows the user to log in.
 * - Register Screen: Allows the user to create a new account.
 * - Forgot Password Screen: Allows the user to reset their password.
 *
 * Navigation between screens is handled by [navController].
 * The [viewModel] is passed to all screens to ensure shared state.
 *
 * @param navController The controller that handles navigation between screens.
 * @param viewModel The ViewModel providing data and logic for the Authentication screens.
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
        /**
         * Screen for user login.
         *
         * @param viewModel Used to perform login operation.
         * @param onLoginClick Called when user submits login form.
         * @param toRegisterClick Navigates to the register screen.
         * @param toForgotPasswordClick Navigates to the forgot password screen.
         */
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

    // --- Register Screen ---
    composable(MainRoutes.Register.route) {
        /**
         * Screen for user registration.
         *
         * @param viewModel Used to perform registration operation.
         * @param onRegisterClick Called when user submits the registration form.
         * @param onBackToLogin Navigates back to the login screen.
         */
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
        /**
         * Screen for resetting user password.
         *
         * @param viewModel Used to perform password reset operation.
         * @param onResetClick Called when user submits the password reset form.
         * @param onBackToLogin Navigates back to the login screen.
         */
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