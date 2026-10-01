package com.example.maiplan.database.converters

import androidx.room.TypeConverter
import java.time.LocalDate

class LocalDateTypeConverter {
    @TypeConverter
    fun fromLocalDate(value: LocalDate?): Long? = value?.toEpochDay()

    @TypeConverter
    fun toLocalDate(value: Long?): LocalDate? = value?.let(LocalDate::ofEpochDay)
}
