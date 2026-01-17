package com.example.maiplan.network.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.maiplan.utils.common.UserSession

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        if (!UserSession.isLoggedIn()) {
            return Result.success()
        }

        val manager = ServiceLocator.provideSyncManager(applicationContext)
        return try {
            manager.syncAll()
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}