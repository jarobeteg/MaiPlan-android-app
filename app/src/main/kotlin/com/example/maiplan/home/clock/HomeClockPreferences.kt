package com.example.maiplan.home.clock

import android.content.Context
import android.text.format.DateFormat
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.maiplan.R
import androidx.core.content.edit

enum class HomeClockStyle(
    val storageKey: String,
    @param:StringRes val nameRes: Int,
) {
    DIGITAL("digital", R.string.clock_style_digital),
    ANALOG("analog", R.string.clock_style_analog);

    companion object {
        fun fromStorageKey(key: String?): HomeClockStyle =
            entries.firstOrNull { it.storageKey == key } ?: DIGITAL
    }
}

enum class HomeClockHourFormat(
    val storageKey: String,
    @param:StringRes val nameRes: Int,
) {
    TWELVE_HOUR("12_hour", R.string.clock_format_12_hour),
    TWENTY_FOUR_HOUR("24_hour", R.string.clock_format_24_hour);

    companion object {
        fun fromStorageKey(
            key: String?,
            default: HomeClockHourFormat = TWELVE_HOUR,
        ): HomeClockHourFormat = entries.firstOrNull { it.storageKey == key } ?: default
    }
}

object HomeClockPreferences {
    private const val PREFERENCES_NAME = "home_clock_preferences"
    private const val STYLE_KEY = "clock_style"
    private const val HOUR_FORMAT_KEY = "clock_hour_format"

    var selectedStyle by mutableStateOf(HomeClockStyle.DIGITAL)
        private set

    var selectedHourFormat by mutableStateOf(HomeClockHourFormat.TWELVE_HOUR)
        private set

    fun initialize(context: Context) {
        val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        val deviceDefault = if (DateFormat.is24HourFormat(context)) {
            HomeClockHourFormat.TWENTY_FOUR_HOUR
        } else {
            HomeClockHourFormat.TWELVE_HOUR
        }

        selectedStyle = HomeClockStyle.fromStorageKey(preferences.getString(STYLE_KEY, null))
        selectedHourFormat = HomeClockHourFormat.fromStorageKey(
            key = preferences.getString(HOUR_FORMAT_KEY, null),
            default = deviceDefault,
        )
    }

    fun selectStyle(context: Context, style: HomeClockStyle) {
        if (style == selectedStyle) return
        selectedStyle = style
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit {
                putString(STYLE_KEY, style.storageKey)
            }
    }

    fun selectHourFormat(context: Context, format: HomeClockHourFormat) {
        if (format == selectedHourFormat) return
        selectedHourFormat = format
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit {
                putString(HOUR_FORMAT_KEY, format.storageKey)
            }
    }
}
