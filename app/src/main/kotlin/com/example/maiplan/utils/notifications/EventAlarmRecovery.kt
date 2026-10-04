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
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException

class EventAlarmRecoveryReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED &&
            !AlarmScheduler.canScheduleExactAlarms(context)) return
        enqueueEventAlarmRecovery(context)
        recoverReminders(context)
    }
}

fun enqueueEventAlarmRecovery(context: Context) {
    val request = OneTimeWorkRequestBuilder<EventAlarmRecoveryWorker>()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        request.setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
    }
    WorkManager.getInstance(context).enqueueUniqueWork(
        "event-alarm-recovery", ExistingWorkPolicy.KEEP,
        request.build(),
    )
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "reminder-queue-maintenance", ExistingPeriodicWorkPolicy.KEEP,
        PeriodicWorkRequestBuilder<EventAlarmRecoveryWorker>(15, TimeUnit.MINUTES).build(),
    )
}

class EventAlarmRecoveryWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return try {
            ReminderCoordinator(applicationContext).recover()
            Result.success()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Log.e("EventAlarmRecovery", "Could not restore reminder alarms", error)
            Result.retry()
        }
    }
}
