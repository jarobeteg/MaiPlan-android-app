package com.example.maiplan.utils

import androidx.compose.material3.Typography
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Stable
data class UiScale(
    val text: TextUnit,
    val textStyle: TextStyle,
    val heading: TextUnit,
    val headingStyle: TextStyle,
    val error: TextUnit,
    val icon: Dp,
    val fieldHeight: Dp,
    val spacing: Dp
)

val DefaultUiScale = UiScale(
    text = 18.sp,
    textStyle = Typography().displaySmall,
    heading = 24.sp,
    headingStyle = Typography().headlineSmall,
    error = 16.sp,
    icon = 24.dp,
    fieldHeight = 64.dp,
    spacing = 12.dp
)

val LocalUiScale = staticCompositionLocalOf { DefaultUiScale }
