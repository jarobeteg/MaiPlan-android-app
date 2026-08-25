package com.example.maiplan.utils

import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Stable
data class AppDimensions(
    val generalPadding: Dp = 16.dp,
    val cardPadding: Dp = 24.dp,
    val smallSpacer: Dp = 4.dp,
    val mediumSpacer: Dp = 8.dp,
    val largeSpacer: Dp = 12.dp,
    val smallPaddingValue: Dp = 8.dp,
    val mediumPaddingValue: Dp = 12.dp,
    val largePaddingValue: Dp = 16.dp,
    val generalSpacer: Dp = 12.dp,
    val smallArrangementSpace: Dp = 8.dp,
    val mediumArrangementSpace: Dp = 12.dp,
    val generalDividerThickness: Dp = 1.dp,
    val generalBorder: Dp = 1.dp,
    val dialogPadding: Dp = 24.dp,
    val iconPickerDialogPadding: Dp = 24.dp,
    val gridPadding: Dp = 8.dp,
    val topBarIconPadding: Dp = 8.dp,
    val dropdownPadding: Dp = 4.dp,
    val verticalWeekdayPadding: Dp = 4.dp,
    val generalTouchTarget: Dp = 48.dp,
    val eventDotSize: Dp = 4.dp,
    val spacedByExtraSmall: Dp = 2.dp,
    val spacedByMedium: Dp = 8.dp,
    val singleDotArea: Dp = 8.dp,
    val doubleDotArea: Dp = 24.dp,
    val generalWeight: Float = 1f,
    val calendarSectionWeight: Float = 0.9f,
    val eventSectionWeight: Float = 1.1f,
)

@Stable
data class AppDesign(
    val dimensions: AppDimensions = AppDimensions(),
)

private val DefaultAppDesign = AppDesign()

val LocalAppDesign = staticCompositionLocalOf { DefaultAppDesign }
