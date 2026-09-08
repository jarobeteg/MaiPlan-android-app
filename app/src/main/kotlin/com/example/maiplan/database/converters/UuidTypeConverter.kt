package com.example.maiplan.database.converters

import androidx.room.TypeConverter
import java.util.UUID

class UuidTypeConverter {

    @TypeConverter
    fun fromUuid(value: UUID?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toUuid(value: String?): UUID? {
        return value?.let(UUID::fromString)
    }
}