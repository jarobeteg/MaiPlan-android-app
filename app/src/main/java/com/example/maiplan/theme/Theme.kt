package com.example.maiplan.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.systemuicontroller.rememberSystemUiController

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
 * Custom App Theme that applies light/dark color schemes, typography, and shapes.
 *
 * Also sets the system status bar color and icons dynamically based on theme.
 *
 * @param darkTheme Whether to use the dark color scheme. Defaults to system setting.
 * @param content The composable content to which the theme will be applied.
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val systemUiController = rememberSystemUiController()
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = !darkTheme
        )
    }

    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}