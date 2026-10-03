package com.example.maiplan.utils.notifications

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.maiplan.database.MaiPlanDatabase
import com.example.maiplan.utils.SessionManager

class EventAlarmRecoveryReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED &&
            !AlarmScheduler.canScheduleExactAlarms(context)) return
        enqueueEventAlarmRecovery(context)
    }
}

fun enqueueEventAlarmRecovery(context: Context) {
    val request = OneTimeWorkRequestBuilder<EventAlarmRecoveryWorker>()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        request.setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
    }
    WorkManager.getInstance(context).enqueueUniqueWork(
        "event-alarm-recovery", ExistingWorkPolicy.REPLACE,
        request.build(),
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
        } catch (error: Exception) {
            Log.e("EventAlarmRecovery", "Could not restore reminder alarms", error)
            Result.retry()
        }
    }
}
