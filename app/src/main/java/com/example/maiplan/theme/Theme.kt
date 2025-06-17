package com.example.maiplan.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Defines the light color scheme used across the app.
 */
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF4A6583),
    onPrimary = Color.White,
    secondary = Color(0xFF2D3E50),
    onSecondary = Color.White,
    background = Color(0xFFF5F5F5),
    onBackground = Color(0xFF101314),
    surface = Color.White,
    onSurface = Color(0xFF101314),
    onError = Color.Red,
    tertiaryContainer = Color(0xFFBDC6D1),
    onTertiary = Color(0xFF33424D),
    surfaceContainer = Color(0xFF8A9DB2),
    secondaryContainer = Color(0xFFB0BEC5)
)

/**
 * Defines the dark color scheme used across the app.
 */
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF1A3A57),
    onPrimary = Color.White,
    secondary = Color(0xFF0E2538),
    onSecondary = Color.White,
    background = Color(0xFF211F25),
    onBackground = Color.White,
    surface = Color(0xFF211F25),
    onSurface = Color.Black,
    onError = Color.Red,
    tertiaryContainer = Color(0xFF57555A),
    onTertiary = Color(0xFFC2CED6),
    surfaceContainer = Color(0xFF8A9DB2),
    secondaryContainer = Color(0xFFB0BEC5)
)

/**
 * Defines the typography styles used throughout the app.
 */
val AppTypography = Typography(
    titleLarge = TextStyle(fontSize = 22.sp),
    bodyLarge = TextStyle(fontSize = 16.sp),
    bodyMedium = TextStyle(fontSize = 14.sp),
    bodySmall = TextStyle(fontSize = 12.sp),
    labelSmall = TextStyle(fontSize = 12.sp)
)

/**
 * Defines the shapes (corner radii) used throughout the app.
 */
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(20.dp)
)

/**
 * Applies the custom app theme including color schemes, typography, and shapes,
 * adapting dynamically to light or dark mode.
 *
 * This composable sets the Material3 [MaterialTheme] colors, typography, and shapes
 * according to the current theme preference.
 *
 * It also manages system UI elements like the status bar color and icon appearance
 * to ensure seamless integration with the app’s theme.
 *
 * @param darkTheme Controls whether to use the dark color scheme.
 *                  Defaults to the device’s current system theme setting.
 * @param content The composable content that will be styled with the app theme.
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}