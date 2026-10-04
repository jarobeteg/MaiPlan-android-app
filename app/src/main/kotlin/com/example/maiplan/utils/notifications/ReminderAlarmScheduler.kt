package com.example.maiplan.utils.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.maiplan.database.entities.ScheduledReminderEntity
import com.example.maiplan.main.MainActivity

object ReminderAlarmScheduler {
    private fun pending(context: Context, source: String, key: String?, create: Boolean): PendingIntent? {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            data = Uri.parse("maiplan://scheduled-reminder/${Uri.encode(source)}")
            putExtra("alarm_key", key)
        }
        return PendingIntent.getBroadcast(context, 0, intent,
            (if (create) PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_NO_CREATE) or
                PendingIntent.FLAG_IMMUTABLE)
    }

    fun schedule(context: Context, row: ScheduledReminderEntity): Boolean {
        val manager = context.getSystemService(AlarmManager::class.java)
        val operation = requireNotNull(pending(context, row.sourceKey, row.alarmKey, true))
        if (AlarmScheduler.canScheduleExactAlarms(context)) {
            try {
                val showReminder = PendingIntent.getActivity(context, 0,
                    Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
                manager.setAlarmClock(AlarmManager.AlarmClockInfo(row.triggerAt, showReminder), operation)
                return true
            } catch (_: SecurityException) {}
        }
        manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, row.triggerAt, operation)
        return false
    }

    fun cancel(context: Context, source: String) {
        val operation = pending(context, source, null, false) ?: return
        context.getSystemService(AlarmManager::class.java).cancel(operation)
        operation.cancel()
    }
}
