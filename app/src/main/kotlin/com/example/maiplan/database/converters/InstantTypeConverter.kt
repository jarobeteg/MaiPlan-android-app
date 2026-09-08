package com.example.maiplan.database.converters

import androidx.room.TypeConverter
import java.time.Instant

class InstantTypeConverter {

    @TypeConverter
    fun fromInstant(value: Instant?): Long? {
        return value?.toEpochMilli()
    }

    @TypeConverter
    fun toInstant(value: Long?): Instant? {
        return value?.let(Instant::ofEpochMilli)
    }
}