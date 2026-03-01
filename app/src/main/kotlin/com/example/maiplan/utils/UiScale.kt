package com.example.maiplan.utils

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Typography
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Stable
data class AppDimensions(
    val generalPadding: Dp,
    val cardPadding: Dp,
    val generalSpacer: Dp,
    val generalDividerThickness: Dp,
    val generalBorder: Dp,
    val dialogPadding: Dp,
    val iconPickerDialogPadding: Dp,
    val gridPadding: Dp,
    val topBarIconPadding: Dp,
    val dropdownPadding: Dp
)

@Stable
data class AppTypographies(
    val generalHeadingStyle: TextStyle,
    val generalTextStyle: TextStyle,
    val dropdownTextStyle: TextStyle,
    val cardTitleStyle: TextStyle,
    val cardBodyStyle: TextStyle
)

@Stable
data class AppFonts(
    val bottomBarTextSize: TextUnit,
    val generalTopBarTitleSize: TextUnit,
    val generalTextSize: TextUnit,
    val generalHeadingSize: TextUnit,
    val passwordStrengthTextSize: TextUnit
)

@Stable
data class AppComponents(
    val contentWidth: Modifier,
    val bottomBarHeight: Dp,
    val bottomBarIconSize: Dp,
    val generalTopBarHeight: Dp,
    val generalIconSize: Dp,
    val generalFieldHeight: Dp,
    val passwordBarHeight: Dp,
    val sliderHeight: Dp,
    val thumbSize: Dp,
    val trackHeight: Dp,
    val dialogWidth: Dp,
    val previewSize: Dp,
    val gridHeight: Dp,
    val iconPickerIconSize: Dp,
    val dropdownHeight: Dp,
    val dropdownItemHeight: Dp,
    val iconCardHeight: Dp,
    val cardIconSize: Dp
)

@Stable
data class UiScale(
    val dimensions: AppDimensions,
    val typographies: AppTypographies,
    val fonts: AppFonts,
    val components: AppComponents
)

val DefaultUiScale = UiScale(
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

val LocalUiScale = staticCompositionLocalOf { DefaultUiScale }
