package com.example.maiplan.network.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object SyncScheduler {

    fun schedulePeriodicSync(context: Context) {
        val request = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "PeriodicSync",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
    }

    fun runOneTimeSync(context: Context) {
        val request = OneTimeWorkRequestBuilder<SyncWorker>().build()

        WorkManager.getInstance(context)
            .enqueue(request)
    }
}
