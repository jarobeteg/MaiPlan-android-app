package com.example.maiplan.database.converters

import androidx.room.TypeConverter
import java.time.LocalTime

class LocalTimeTypeConverter {
    @TypeConverter
    fun fromLocalTime(value: LocalTime?): String? = value?.toString()

    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? = value?.let(LocalTime::parse)
}
