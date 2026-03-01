package com.example.maiplan.utils

import android.R
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import android.content.res.Configuration.UI_MODE_NIGHT_MASK
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Typography
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
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

        val scale = remember { getScale(window.widthSizeClass)}

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

    private fun getScale(widthSizeClass: WindowWidthSizeClass): UiScale {
        return when (widthSizeClass) {
            WindowWidthSizeClass.Compact -> {
                UiScale(
                    dimensions = AppDimensions (
                        generalPadding = 8.dp,
                        cardPadding = 24.dp,
                        generalSpacer = 12.dp,
                        generalDividerThickness = 1.dp,
                        generalBorder = 1.dp,
                        dialogPadding = 16.dp,
                        iconPickerDialogPadding = 16.dp,
                        gridPadding = 8.dp,
                        topBarIconPadding = 8.dp,
                        dropdownPadding = 4.dp
                    ),

                    typographies = AppTypographies(
                        generalHeadingStyle = Typography().headlineSmall,
                        generalTextStyle = Typography().labelSmall,
                        dropdownTextStyle = Typography().headlineMedium,
                        cardTitleStyle = Typography().titleSmall,
                        cardBodyStyle = Typography().bodySmall
                    ),

                    fonts = AppFonts (
                        bottomBarTextSize = 12.sp,
                        generalTopBarTitleSize = 24.sp,
                        generalTextSize = 16.sp,
                        generalHeadingSize = 24.sp,
                        passwordStrengthTextSize = 12.sp
                    ),

                    components = AppComponents(
                        contentWidth = Modifier.fillMaxWidth(),
                        bottomBarHeight = 96.dp,
                        bottomBarIconSize = 24.dp,
                        generalTopBarHeight = 112.dp,
                        generalIconSize = 24.dp,
                        generalFieldHeight = 64.dp,
                        passwordBarHeight = 8.dp,
                        sliderHeight = 64.dp,
                        thumbSize = 24.dp,
                        trackHeight = 12.dp,
                        dialogWidth = Dp.Unspecified,
                        previewSize = 100.dp,
                        gridHeight = 300.dp,
                        iconPickerIconSize = 48.dp,
                        dropdownHeight = 40.dp,
                        dropdownItemHeight = 48.dp,
                        iconCardHeight = 128.dp,
                        cardIconSize = 32.dp
                    )
                )
            }

            WindowWidthSizeClass.Medium -> {
                UiScale(
                    dimensions = AppDimensions (
                        generalPadding = 4.dp,
                        cardPadding = 30.dp,
                        generalSpacer = 15.dp,
                        generalDividerThickness = 2.dp,
                        generalBorder = 2.dp,
                        dialogPadding = 24.dp,
                        iconPickerDialogPadding = 20.dp,
                        gridPadding = 10.dp,
                        topBarIconPadding = 12.dp,
                        dropdownPadding = 6.dp
                    ),

                    typographies = AppTypographies(
                        generalHeadingStyle = Typography().headlineMedium,
                        generalTextStyle = Typography().labelMedium,
                        dropdownTextStyle = Typography().headlineMedium,
                        cardTitleStyle = Typography().titleMedium,
                        cardBodyStyle = Typography().bodyMedium
                    ),

                    fonts = AppFonts (
                        bottomBarTextSize = 14.sp,
                        generalTopBarTitleSize = 28.sp,
                        generalTextSize = 20.sp,
                        generalHeadingSize = 28.sp,
                        passwordStrengthTextSize = 16.sp
                    ),

                    components = AppComponents(
                        contentWidth = Modifier.widthIn(min = 500.dp, max = 700.dp),
                        bottomBarHeight = 112.dp,
                        bottomBarIconSize = 28.dp,
                        generalTopBarHeight = 112.dp,
                        generalIconSize = 28.dp,
                        generalFieldHeight = 68.dp,
                        passwordBarHeight = 12.dp,
                        sliderHeight = 68.dp,
                        thumbSize = 27.dp,
                        trackHeight = 15.dp,
                        dialogWidth = 700.dp,
                        previewSize = 125.dp,
                        gridHeight = 450.dp,
                        iconPickerIconSize = 56.dp,
                        dropdownHeight = 45.dp,
                        dropdownItemHeight = 56.dp,
                        iconCardHeight = 134.dp,
                        cardIconSize = 40.dp
                    )
                )
            }

            WindowWidthSizeClass.Expanded -> {
                UiScale(
                    dimensions = AppDimensions (
                        generalPadding = 0.dp,
                        cardPadding = 36.dp,
                        generalSpacer = 18.dp,
                        generalDividerThickness = 3.dp,
                        generalBorder = 2.dp,
                        dialogPadding = 32.dp,
                        iconPickerDialogPadding = 24.dp,
                        gridPadding = 12.dp,
                        topBarIconPadding = 16.dp,
                        dropdownPadding = 8.dp
                    ),

                    typographies = AppTypographies(
                        generalHeadingStyle = Typography().headlineLarge,
                        generalTextStyle = Typography().labelLarge,
                        dropdownTextStyle = Typography().headlineSmall,
                        cardTitleStyle = Typography().titleLarge,
                        cardBodyStyle = Typography().bodyLarge
                    ),

                    fonts = AppFonts (
                        bottomBarTextSize = 16.sp,
                        generalTopBarTitleSize = 32.sp,
                        generalTextSize = 24.sp,
                        generalHeadingSize = 32.sp,
                        passwordStrengthTextSize = 20.sp
                    ),

                    components = AppComponents(
                        contentWidth = Modifier.fillMaxWidth(0.7f),
                        bottomBarHeight = 128.dp,
                        bottomBarIconSize = 32.dp,
                        generalTopBarHeight = 112.dp,
                        generalIconSize = 32.dp,
                        generalFieldHeight = 72.dp,
                        passwordBarHeight = 16.dp,
                        sliderHeight = 72.dp,
                        thumbSize = 30.dp,
                        trackHeight = 18.dp,
                        dialogWidth = 800.dp,
                        previewSize = 150.dp,
                        gridHeight = 600.dp,
                        iconPickerIconSize = 64.dp,
                        dropdownHeight = 50.dp,
                        dropdownItemHeight = 64.dp,
                        iconCardHeight = 160.dp,
                        cardIconSize = 48.dp
                    )
                )
            }

            else -> DefaultUiScale
        }
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