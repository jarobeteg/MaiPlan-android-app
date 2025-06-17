package com.example.maiplan.home

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.maiplan.home.navigation.HomeNavHost
import com.example.maiplan.theme.AppTheme
import com.example.maiplan.utils.BaseActivity

/**
 * [HomeActivity] serves as the main single-activity host and entry point
 * for the app’s core features.
 *
 * Inherits from [BaseActivity] to maintain consistent edge-to-edge UI and
 * system bar styling throughout the app.
 *
 * This activity initializes the root navigation controller ([NavHostController]),
 * applies the app’s theme, and manages navigation across major sections
 * such as Events, Tasks, Files, and More.
 */
class HomeActivity : BaseActivity() {

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