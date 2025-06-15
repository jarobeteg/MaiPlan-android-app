package com.example.maiplan.home.file.navigation

import com.example.maiplan.home.file.screens.*

/**
 * Represents the different navigation routes for the `File` screens.
 *
 * Each object corresponds to a specific `File` screen.
 *
 * @property route File object holds a route string value to differentiate route endpoints.
 */
sealed class FileRoutes(val route: String) {

    /**
     * Route for the [FileScreen], which is the `Main File Screen`, it allows navigation between other file screen as well as home tabs.
     */
    data object FileMain : FileRoutes("file-main-screen")
}