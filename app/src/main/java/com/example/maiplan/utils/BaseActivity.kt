package com.example.maiplan.utils

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat.getInsetsController
import androidx.core.view.WindowCompat.setDecorFitsSystemWindows

/**
 * [BaseActivity] is an abstract base class for all activities in the app,
 * providing common setup and behavior for consistent UI appearance.
 *
 * ## Responsibilities:
 * - Enabling edge-to-edge content by disabling default system window insets.
 * - Dynamically configuring status and navigation bar icon colors based on the current theme (dark or light).
 *
 * This base class should be extended by all Activities to inherit consistent
 * window and system UI behavior across the app.
 */
open class BaseActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Enable edge-to-edge display by telling the system not to fit content within system windows (status/navigation bars).
        setDecorFitsSystemWindows(window, false)

        super.onCreate(savedInstanceState)

        setupSystemBars()
    }

    /**
     * Configures the system bars appearance depending on
     * whether the current UI mode is dark or light.
     *
     * - In dark mode, system bar icons are light.
     * - In light mode, system bar icons are dark.
     *
     * This method can be overridden in subclasses to customize behavior.
     */
    protected open fun setupSystemBars() {
        val isDarkTheme = resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES

        val insetsController = getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = !isDarkTheme
        insetsController.isAppearanceLightNavigationBars = !isDarkTheme
    }
}