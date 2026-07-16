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
    val smallSpacer: Dp,
    val mediumSpacer: Dp,
    val largeSpacer: Dp,
    val smallPaddingValue: Dp,
    val mediumPaddingValue: Dp,
    val largePaddingValue: Dp,
    val generalSpacer: Dp,
    val smallArrangementSpace: Dp,
    val mediumArrangementSpace: Dp,
    val generalDividerThickness: Dp,
    val generalBorder: Dp,
    val dialogPadding: Dp,
    val iconPickerDialogPadding: Dp,
    val gridPadding: Dp,
    val topBarIconPadding: Dp,
    val dropdownPadding: Dp,
    val verticalWeekdayPadding: Dp,
    val generalTouchTarget: Dp,
    val eventDotSize: Dp,
    val spacedByExtraSmall: Dp,
    val spacedByMedium: Dp,
    val singleDotArea: Dp,
    val doubleDotArea: Dp,
    val generalWeight: Float,
    val calendarSectionWeight: Float,
    val eventSectionWeight: Float
)

@Stable
data class AppTypographies(
    val generalHeadingStyle: TextStyle,
    val generalTextStyle: TextStyle,
    val dropdownTextStyle: TextStyle,
    val cardTitleStyle: TextStyle,
    val cardBodyStyle: TextStyle,
    val eventCardTitleStyle: TextStyle,
    val eventCardDescriptionStyle: TextStyle,
    val eventCardTimeStyle: TextStyle
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
    val generalTopBarIconSize: Dp,
    val generalIconSize: Dp,
    val generalFieldHeight: Dp,
    val generalSubmitButtonHeight: Dp,
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
    val cardIconSize: Dp,
    val colorStripSize: Dp,
    val smallCardElevation: Dp
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
        smallSpacer = 4.dp,
        mediumSpacer = 8.dp,
        largeSpacer = 12.dp,
        smallPaddingValue = 8.dp,
        mediumPaddingValue = 12.dp,
        largePaddingValue = 16.dp,
        generalSpacer = 12.dp,
        smallArrangementSpace = 8.dp,
        mediumArrangementSpace = 10.dp,
        generalDividerThickness = 1.dp,
        generalBorder = 1.dp,
        dialogPadding = 16.dp,
        iconPickerDialogPadding = 16.dp,
        gridPadding = 8.dp,
        topBarIconPadding = 8.dp,
        dropdownPadding = 4.dp,
        verticalWeekdayPadding = 4.dp,
        generalTouchTarget = 36.dp,
        eventDotSize = 4.dp,
        spacedByExtraSmall = 2.dp,
        spacedByMedium = 8.dp,
        singleDotArea = 8.dp,
        doubleDotArea = 24.dp,
        generalWeight = 1f,
        calendarSectionWeight = 0.8f,
        eventSectionWeight = 1.2f
    ),

    typographies = AppTypographies(
        generalHeadingStyle = Typography().headlineSmall,
        generalTextStyle = Typography().labelSmall,
        dropdownTextStyle = Typography().headlineMedium,
        cardTitleStyle = Typography().titleSmall,
        cardBodyStyle = Typography().bodySmall,
        eventCardTitleStyle = Typography().titleMedium,
        eventCardDescriptionStyle = Typography().bodyMedium,
        eventCardTimeStyle = Typography().bodySmall
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
        generalTopBarIconSize = 28.dp,
        generalIconSize = 24.dp,
        generalFieldHeight = 64.dp,
        generalSubmitButtonHeight = 48.dp,
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
        cardIconSize = 32.dp,
        colorStripSize = 8.dp,
        smallCardElevation = 4.dp
    )
)

val LocalUiScale = staticCompositionLocalOf { DefaultUiScale }
