package com.example.maiplan.home.more.navigation

import com.example.maiplan.home.more.screens.*

/**
 * Represents the different navigation routes for the `More` screens.
 *
 * Each object corresponds to a specific `More` screen.
 *
 * @property route More object holds a route string value to differentiate route endpoints.
 */
sealed class MoreRoutes(val route: String) {

    /**
     * Route for the [MoreScreen], which is the `Main More Screen`, it allows navigation between other more screen as well as home tabs.
     */
    data object MoreMain : MoreRoutes("more-main-screen")
}