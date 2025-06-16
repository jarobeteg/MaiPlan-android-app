package com.example.maiplan.main.navigation

import com.example.maiplan.main.screens.*

/**
 * Represents the different navigation routes for the `Main` screens.
 *
 * Each object corresponds to a specific `Main` screen.
 *
 * @property route Every object holds a route string value to differentiate route endpoints.
 */
sealed class MainRoutes(val route: String) {
    /**
     * Route for the [LoginScreen], which lets the user to enter their credentials and log into the app.
     */
    data object Login : MainRoutes("login")

    /**
     * Route for the [RegisterScreen], which lets the user create a new user account for the app.
     */
    data object Register : MainRoutes("register")

    /**
     * Route for the [ForgotPasswordScreen], which lets registered users reset their passwords.
     */
    data object ForgotPassword : MainRoutes("forgot-password")
}