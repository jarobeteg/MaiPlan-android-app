package com.example.maiplan.utils.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.os.PowerManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

internal fun BroadcastReceiver.recoverReminders(context: Context) {
    val app = context.applicationContext
    val pending = goAsync()
    val wakeLock = app.getSystemService(PowerManager::class.java)
        .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MaiPlan:reminder-delivery")
    wakeLock.acquire(10_000L)
    CoroutineScope(Dispatchers.IO).launch {
        try {
            withTimeout(8_000L) { ReminderCoordinator(app).recover() }
        } catch (error: Exception) {
            Log.e("ReminderReceiver", "Delivery will be retried from the saved queue", error)
            enqueueEventAlarmRecovery(app)
        } finally {
            if (wakeLock.isHeld) wakeLock.release()
            pending.finish()
        }
    }
}
