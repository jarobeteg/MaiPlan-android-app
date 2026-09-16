package com.example.maiplan.network.sync

import android.content.Context
import com.example.maiplan.database.MaiPlanDatabase
import com.example.maiplan.network.RetrofitClient
import com.example.maiplan.network.TideExchangeClient
import com.example.maiplan.repository.reminder.ReminderLocalDataSource
import com.example.maiplan.repository.reminder.ReminderRemoteDataSource
import com.example.maiplan.repository.reminder.ReminderRepository
import com.example.maiplan.utils.DeviceIdentityStore

object ServiceLocator {
    fun provideSyncManager(context: Context): SyncManager {
        val appContext = context.applicationContext
        val database = MaiPlanDatabase.getDatabase(appContext)
        val requestPreparer = TideRequestPreparer(
            database = database,
            deviceIdentityStore = DeviceIdentityStore(appContext)
        )
        val exchangeClient = TideExchangeClient(
            tideApi = RetrofitClient.tideApi,
            requestPreparer = requestPreparer
        )

        return SyncManager(
            reminderRepo = provideReminderRepo(appContext),
            categorySynchronizer = CategoryTideSynchronizer(
                requestPreparer = requestPreparer,
                exchangeClient = exchangeClient,
                responseValidator = TideCategoryResponseValidator(),
                reconciler = CategoryTideReconciler(database)
            )
        )
    }

    private fun provideReminderRepo(context: Context): ReminderRepository {
        val remote = ReminderRemoteDataSource(RetrofitClient.reminderApi)
        val local = ReminderLocalDataSource(context)
        return ReminderRepository(remote, local)
    }
}
