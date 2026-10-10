package com.example.maiplan.network.sync

import android.content.Context
import com.example.maiplan.database.MaiPlanDatabase
import com.example.maiplan.network.RetrofitClient
import com.example.maiplan.network.TideExchangeClient
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
            tideSynchronizer = TideSynchronizer(
                requestPreparer = requestPreparer,
                exchangeClient = exchangeClient,
                responseValidator = TideResponseValidator(),
                reconciler = TideReconciler(database) { ids ->
                    com.example.maiplan.utils.notifications.enqueueEventAlarmRecovery(appContext)
                    val coordinator = com.example.maiplan.utils.notifications.ReminderCoordinator(appContext)
                    ids.forEach { coordinator.refreshTask(it) }
                }
            )
        )
    }
}
