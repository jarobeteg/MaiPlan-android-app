package com.example.maiplan.network.sync

import android.content.Context
import com.example.maiplan.network.RetrofitClient
import com.example.maiplan.repository.category.CategoryLocalDataSource
import com.example.maiplan.repository.category.CategoryRemoteDataSource
import com.example.maiplan.repository.category.CategoryRepository
import com.example.maiplan.repository.event.EventLocalDataSource
import com.example.maiplan.repository.event.EventRemoteDataSource
import com.example.maiplan.repository.event.EventRepository
import com.example.maiplan.repository.reminder.ReminderLocalDataSource
import com.example.maiplan.repository.reminder.ReminderRemoteDataSource
import com.example.maiplan.repository.reminder.ReminderRepository

object ServiceLocator {
    fun provideSyncManager(context: Context): SyncManager {
        return SyncManager(
            provideCategoryRepo(context),
            provideReminderRepo(context),
            provideEventRepo(context)
        )
    }

    private fun provideCategoryRepo(context: Context): CategoryRepository {
        val remote = CategoryRemoteDataSource(RetrofitClient.categoryApi)
        val local = CategoryLocalDataSource(context)
        return CategoryRepository(remote, local)
    }

    private fun provideReminderRepo(context: Context): ReminderRepository {
        val remote = ReminderRemoteDataSource(RetrofitClient.reminderApi)
        val local = ReminderLocalDataSource(context)
        return ReminderRepository(remote, local)
    }

    private fun provideEventRepo(context: Context): EventRepository {
        val remote = EventRemoteDataSource(RetrofitClient.eventApi)
        val local = EventLocalDataSource(context)
        val localCategory = CategoryLocalDataSource(context)
        val localReminder = ReminderLocalDataSource(context)
        return EventRepository(remote, local, localCategory, localReminder)
    }
}