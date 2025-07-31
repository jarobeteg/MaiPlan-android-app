package com.example.maiplan.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.maiplan.database.dao.AuthDAO
import com.example.maiplan.database.entities.AuthEntity

@Database(
    entities = [AuthEntity::class],
    version = 1,
    exportSchema = true
)
abstract class MaiPlanDatabase: RoomDatabase() {
    abstract fun authDAO(): AuthDAO

    companion object {
        @Volatile
        private var INSTANCE: MaiPlanDatabase? = null

        fun getDatabase(context: Context): MaiPlanDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MaiPlanDatabase::class.java,
                    "mai_plan_database"
                ).fallbackToDestructiveMigration(false).build()
                INSTANCE = instance
                instance
            }
        }
    }
}