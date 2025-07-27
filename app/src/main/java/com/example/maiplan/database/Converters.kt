package com.example.maiplan.database

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class Converters {
    @TypeConverter
    fun stringToLocalDateTime(string: String?): LocalDateTime? = string.let { LocalDateTime.parse(it) }

    @TypeConverter
    fun localDateTimeToString(localDateTime: LocalDateTime?): String? = localDateTime?.toString()

    @TypeConverter
    fun stringToLocalDate(string: String?): LocalDate? = string.let { LocalDate.parse(it) }

    @TypeConverter
    fun localDateToString(localDate: LocalDate?): String? = localDate?.toString()

    @TypeConverter
    fun stringToLocalTime(string: String?): LocalTime? = string?.let { LocalTime.parse(it) }

    @TypeConverter
    fun localTimeToString(localTime: LocalTime?): String? = localTime?.toString()
}