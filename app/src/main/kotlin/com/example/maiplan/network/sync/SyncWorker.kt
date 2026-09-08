package com.example.maiplan.network.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.maiplan.database.MaiPlanDatabase
import com.example.maiplan.utils.SessionManager
import com.example.maiplan.utils.common.UserSession
import kotlinx.coroutines.CancellationException

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val sessionManager = SessionManager(applicationContext)

        if (!sessionManager.hasSession()) {
            sessionManager.clearSession()
            UserSession.clear()
            return Result.success()
        }

        return try {
            val activeUserSyncId = sessionManager.getActiveUserSyncId()
                ?: run {
                    sessionManager.clearSession()
                    UserSession.clear()
                    return Result.success()
                }

            val activeUser = MaiPlanDatabase.getDatabase(applicationContext)
                .userDAO()
                .getActiveUserBySyncId(activeUserSyncId)

            if (activeUser == null) {
                sessionManager.clearSession()
                UserSession.clear()
                return Result.success()
            }

            UserSession.setup(activeUser)

            val manager = ServiceLocator.provideSyncManager(applicationContext)
            manager.syncAll()
            Result.success()
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
