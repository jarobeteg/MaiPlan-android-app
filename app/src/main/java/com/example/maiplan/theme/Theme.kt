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

val AppTypography = Typography(
    titleLarge = TextStyle(fontSize = 22.sp),
    bodyLarge = TextStyle(fontSize = 16.sp),
    bodyMedium = TextStyle(fontSize = 14.sp),
    bodySmall = TextStyle(fontSize = 12.sp),
    labelSmall = TextStyle(fontSize = 12.sp)
)

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(20.dp)
)

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