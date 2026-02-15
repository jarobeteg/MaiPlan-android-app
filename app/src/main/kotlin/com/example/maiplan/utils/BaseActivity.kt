package com.example.maiplan.utils

import android.R
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import android.content.res.Configuration.UI_MODE_NIGHT_MASK
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.WindowCompat.getInsetsController
import androidx.core.view.WindowCompat.setDecorFitsSystemWindows
import com.example.maiplan.main.MainActivity
import com.example.maiplan.network.NetworkChecker

open class BaseActivity : ComponentActivity() {
    lateinit var networkChecker: NetworkChecker
    lateinit var sessionManager: SessionManager

    @Composable
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    private fun Root(content: @Composable () -> Unit) {
        val window = calculateWindowSizeClass(this)

        val scale = remember(window.widthSizeClass) {
            when (window.widthSizeClass) {
                WindowWidthSizeClass.Compact -> UiScale(
                    text = 18.sp, heading = 24.sp, error = 16.sp,
                    icon = 24.dp, fieldHeight = 64.dp, spacing = 12.dp,
                    textStyle = Typography().displaySmall,
                    headingStyle = Typography().headlineSmall
                )
                WindowWidthSizeClass.Medium -> UiScale(
                    text = 22.sp, heading = 28.sp, error = 20.sp,
                    icon = 30.dp, fieldHeight = 68.dp, spacing = 16.dp,
                    textStyle = Typography().displaySmall,
                    headingStyle = Typography().headlineSmall
                )
                WindowWidthSizeClass.Expanded -> UiScale(
                    text = 24.sp, heading = 32.sp, error = 24.sp,
                    icon = 36.dp, fieldHeight = 72.dp, spacing = 18.dp,
                    textStyle = Typography().displaySmall,
                    headingStyle = Typography().headlineLarge
                )
                else -> DefaultUiScale
            }
        }

        CompositionLocalProvider(LocalUiScale provides scale) {
            content()
        }
    }

    protected fun setAppContent(content: @Composable () -> Unit) {
        setContent { Root { content() } }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Enable edge-to-edge display by telling the system not to fit content within system windows (status/navigation bars).
        setDecorFitsSystemWindows(window, false)

        super.onCreate(savedInstanceState)

        networkChecker = NetworkChecker(this)
        sessionManager = SessionManager(this)

        setupSystemBars()
    }

    protected open fun setupSystemBars() {
        val isDarkTheme = resources.configuration.uiMode and UI_MODE_NIGHT_MASK == UI_MODE_NIGHT_YES

        val insetsController = getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = !isDarkTheme
        insetsController.isAppearanceLightNavigationBars = !isDarkTheme

        window.setBackgroundDrawable(null)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
    }

    fun logout() {
        sessionManager.clearAll()

        val options = ActivityOptionsCompat.makeCustomAnimation(
            this,
            R.anim.fade_in,
            R.anim.fade_out
        )

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent, options.toBundle())
        finish()
    }
}