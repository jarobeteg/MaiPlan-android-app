package com.example.maiplan.home

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.maiplan.home.navigation.HomeNavHost
import com.example.maiplan.theme.AppTheme

/**
 * The [HomeActivity] is the main entry point and single-activity host for the application.
 *
 * It sets up the root [NavHostController] and applies the app-wide [AppTheme].
 * The [HomeActivity] is responsible for initializing navigation between major feature sections
 * such as Events, Tasks, Files, and More via the [HomeNavHost].
 */
class HomeActivity : ComponentActivity() {

    /**
     * Lifecycle method [onCreate] called when the [HomeActivity] is created.
     *
     * - Initializes the root navigation controller using [rememberNavController].
     * - Applies the [AppTheme] for consistent styling across the app.
     * - Sets the main content to the [HomeNavHost], which manages navigation between
     *   the core sections of the app.
     *
     * @param savedInstanceState Saved state of this [HomeActivity] if it previously existed.
     *
     * @see AppTheme
     * @see HomeNavHost
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                val rootNavController: NavHostController = rememberNavController()
                HomeNavHost(rootNavController)
            }
        }
    }
}