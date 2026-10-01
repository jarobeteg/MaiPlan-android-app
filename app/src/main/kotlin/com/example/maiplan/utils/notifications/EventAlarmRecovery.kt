package com.example.maiplan.utils.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.maiplan.database.MaiPlanDatabase
import com.example.maiplan.utils.SessionManager

class EventAlarmRecoveryReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        enqueueEventAlarmRecovery(context)
    }
}

fun enqueueEventAlarmRecovery(context: Context) {
    WorkManager.getInstance(context).enqueueUniqueWork(
        "event-alarm-recovery", ExistingWorkPolicy.REPLACE,
        OneTimeWorkRequestBuilder<EventAlarmRecoveryWorker>().build(),
    )
}

class EventAlarmRecoveryWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val session = SessionManager(applicationContext)
        val syncId = session.getActiveUserSyncId() ?: return Result.success()
        val user = MaiPlanDatabase.getDatabase(applicationContext).userDAO()
            .getActiveUserBySyncId(syncId) ?: return Result.success()
        return try {
            EventAlarmCoordinator(applicationContext).reconcileAll(user.userLocalId)
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
