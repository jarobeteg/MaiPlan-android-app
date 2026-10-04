package com.example.maiplan.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.maiplan.database.dao.ScheduledReminderDAO
import com.example.maiplan.database.entities.ScheduledReminderEntity
import com.example.maiplan.database.converters.InstantTypeConverter
import com.example.maiplan.database.converters.LocalDateTypeConverter
import com.example.maiplan.database.converters.LocalTimeTypeConverter
import com.example.maiplan.database.converters.UuidTypeConverter
import com.example.maiplan.database.dao.CategoryDAO
import com.example.maiplan.database.dao.EventDAO
import com.example.maiplan.database.dao.NoteDAO
import com.example.maiplan.database.dao.ReminderDAO
import com.example.maiplan.database.dao.OutboxDAO
import com.example.maiplan.database.dao.SyncStateDAO
import com.example.maiplan.database.dao.UserDAO
import com.example.maiplan.database.entities.UserEntity
import com.example.maiplan.database.entities.CategoryEntity
import com.example.maiplan.database.entities.EventEntity
import com.example.maiplan.database.entities.NoteEntity
import com.example.maiplan.database.entities.OutboxEntity
import com.example.maiplan.database.entities.ReminderEntity
import com.example.maiplan.database.entities.SyncStateEntity

@Database(
    entities = [
        UserEntity::class,
        CategoryEntity::class,
        ReminderEntity::class,
        EventEntity::class,
        NoteEntity::class,
        OutboxEntity::class,
        SyncStateEntity::class,
        ScheduledReminderEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    value = [
        UuidTypeConverter::class,
        InstantTypeConverter::class,
        LocalDateTypeConverter::class,
        LocalTimeTypeConverter::class
    ]
)
abstract class MaiPlanDatabase: RoomDatabase() {
    abstract fun userDAO(): UserDAO
    abstract fun categoryDAO(): CategoryDAO
    abstract fun reminderDAO(): ReminderDAO
    abstract fun eventDAO(): EventDAO
    abstract fun noteDAO(): NoteDAO
    abstract fun outboxDAO(): OutboxDAO
    abstract fun syncStateDAO(): SyncStateDAO
    abstract fun scheduledReminderDAO(): ScheduledReminderDAO

    companion object {
        @Volatile
        private var INSTANCE: MaiPlanDatabase? = null

        fun getDatabase(context: Context): MaiPlanDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MaiPlanDatabase::class.java,
                    "mai_plan_database"
                )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
