package com.example.maiplan.theme

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.example.maiplan.R

enum class AppColorTheme(
    val storageKey: String,
    @param:StringRes val nameRes: Int,
    @param:StringRes val descriptionRes: Int,
    val primary: Color,
    val primaryLight: Color,
    val primaryDark: Color,
) {
    BLUE_GREY(
        storageKey = "blue_grey",
        nameRes = R.string.theme_blue_grey,
        descriptionRes = R.string.theme_blue_grey_description,
        primary = Color(0xFF4A6583),
        primaryLight = Color(0xFF7089A5),
        primaryDark = Color(0xFF2D3E50),
    ),
    OCEAN(
        storageKey = "ocean",
        nameRes = R.string.theme_ocean,
        descriptionRes = R.string.theme_ocean_description,
        primary = Color(0xFF287C8E),
        primaryLight = Color(0xFF5A9EAC),
        primaryDark = Color(0xFF185866),
    ),
    INDIGO(
        storageKey = "indigo",
        nameRes = R.string.theme_indigo,
        descriptionRes = R.string.theme_indigo_description,
        primary = Color(0xFF5969A6),
        primaryLight = Color(0xFF808DC0),
        primaryDark = Color(0xFF3C477A),
    ),
    FOREST(
        storageKey = "forest",
        nameRes = R.string.theme_forest,
        descriptionRes = R.string.theme_forest_description,
        primary = Color(0xFF41715A),
        primaryLight = Color(0xFF6D927E),
        primaryDark = Color(0xFF294D3C),
    ),
    SAGE(
        storageKey = "sage",
        nameRes = R.string.theme_sage,
        descriptionRes = R.string.theme_sage_description,
        primary = Color(0xFF687D65),
        primaryLight = Color(0xFF8FA08B),
        primaryDark = Color(0xFF465A44),
    ),
    TERRACOTTA(
        storageKey = "terracotta",
        nameRes = R.string.theme_terracotta,
        descriptionRes = R.string.theme_terracotta_description,
        primary = Color(0xFFA45F4B),
        primaryLight = Color(0xFFC38270),
        primaryDark = Color(0xFF743F32),
    ),
    PLUM(
        storageKey = "plum",
        nameRes = R.string.theme_plum,
        descriptionRes = R.string.theme_plum_description,
        primary = Color(0xFFA66F7F),
        primaryLight = Color(0xFFC493A1),
        primaryDark = Color(0xFF744B58),
    ),
    AMBER(
        storageKey = "amber",
        nameRes = R.string.theme_amber,
        descriptionRes = R.string.theme_amber_description,
        primary = Color(0xFF9A6B2F),
        primaryLight = Color(0xFFBE9053),
        primaryDark = Color(0xFF6B481D),
    );

    companion object {
        fun fromStorageKey(key: String?): AppColorTheme =
            entries.firstOrNull { it.storageKey == key } ?: BLUE_GREY
    }
}

enum class AppThemeMode(
    val storageKey: String,
    @param:StringRes val nameRes: Int,
) {
    SYSTEM("system", R.string.theme_mode_system),
    LIGHT("light", R.string.theme_mode_light),
    DARK("dark", R.string.theme_mode_dark);

    fun resolveDarkTheme(systemDarkTheme: Boolean): Boolean = when (this) {
        SYSTEM -> systemDarkTheme
        LIGHT -> false
        DARK -> true
    }

    companion object {
        fun fromStorageKey(key: String?): AppThemeMode =
            entries.firstOrNull { it.storageKey == key } ?: SYSTEM
    }
}

object AppThemeManager {
    private const val PREFERENCES_NAME = "appearance_preferences"
    private const val THEME_KEY = "app_color_theme"
    private const val MODE_KEY = "app_theme_mode"

    var selectedTheme by mutableStateOf(AppColorTheme.BLUE_GREY)
        private set

    var selectedMode by mutableStateOf(AppThemeMode.SYSTEM)
        private set

    fun initialize(context: Context) {
        val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        selectedTheme = AppColorTheme.fromStorageKey(preferences.getString(THEME_KEY, null))
        selectedMode = AppThemeMode.fromStorageKey(preferences.getString(MODE_KEY, null))
    }

    fun selectTheme(context: Context, theme: AppColorTheme) {
        if (theme == selectedTheme) return
        selectedTheme = theme
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(THEME_KEY, theme.storageKey)
            .apply()
    }

    fun selectMode(context: Context, mode: AppThemeMode) {
        if (mode == selectedMode) return
        selectedMode = mode
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(MODE_KEY, mode.storageKey)
            .apply()
    }
}
