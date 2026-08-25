package com.example.maiplan.utils

import androidx.activity.ComponentActivity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration

enum class AppWindowWidth { Compact, Medium, Expanded }

enum class AppWindowHeight { Compact, Medium, Expanded }

@Stable
data class AdaptiveLayout(
    val width: AppWindowWidth,
    val height: AppWindowHeight,
    val isLandscape: Boolean,
    val formMaxWidth: Dp,
) {
    val useTwoPaneLayout: Boolean
        get() = width != AppWindowWidth.Compact && isLandscape

    val useNavigationRail: Boolean
        get() = width == AppWindowWidth.Expanded

    val isShort: Boolean
        get() = height == AppWindowHeight.Compact
}

private val CompactAdaptiveLayout = AdaptiveLayout(
    width = AppWindowWidth.Compact,
    height = AppWindowHeight.Medium,
    isLandscape = false,
    formMaxWidth = 560.dp,
)

val LocalAdaptiveLayout = staticCompositionLocalOf { CompactAdaptiveLayout }

internal fun adaptiveLayoutFor(
    width: AppWindowWidth,
    height: AppWindowHeight,
    isLandscape: Boolean,
): AdaptiveLayout = AdaptiveLayout(
    width = width,
    height = height,
    isLandscape = isLandscape,
    formMaxWidth = when (width) {
        AppWindowWidth.Compact -> 560.dp
        AppWindowWidth.Medium -> 640.dp
        AppWindowWidth.Expanded -> 720.dp
    },
)

@Composable
fun Modifier.adaptiveContentWidth(): Modifier {
    val adaptiveLayout = LocalAdaptiveLayout.current
    return if (adaptiveLayout.width == AppWindowWidth.Compact) {
        this
            .fillMaxWidth()
            .wrapContentWidth(Alignment.CenterHorizontally)
            .widthIn(max = adaptiveLayout.formMaxWidth)
    } else {
        this.fillMaxWidth()
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun AdaptiveLayoutProvider(
    activity: ComponentActivity,
    content: @Composable () -> Unit,
) {
    val windowSizeClass = calculateWindowSizeClass(activity)
    val configuration = LocalConfiguration.current
    val width = when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> AppWindowWidth.Compact
        WindowWidthSizeClass.Medium -> AppWindowWidth.Medium
        else -> AppWindowWidth.Expanded
    }
    val height = when (windowSizeClass.heightSizeClass) {
        WindowHeightSizeClass.Compact -> AppWindowHeight.Compact
        WindowHeightSizeClass.Medium -> AppWindowHeight.Medium
        else -> AppWindowHeight.Expanded
    }
    CompositionLocalProvider(
        LocalAdaptiveLayout provides adaptiveLayoutFor(
            width = width,
            height = height,
            isLandscape = configuration.screenWidthDp > configuration.screenHeightDp,
        ),
        content = content,
    )
}
