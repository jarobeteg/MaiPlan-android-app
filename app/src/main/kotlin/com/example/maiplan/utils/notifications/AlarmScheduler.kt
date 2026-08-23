package com.example.maiplan.utils.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.net.toUri

object AlarmScheduler {

    fun attemptSchedule(context: Context, reminder: ReminderData): Boolean {
        if (reminder.reminderTime <= System.currentTimeMillis()) {
            Log.e("AlarmScheduler", "Cannot schedule a reminder in the past.")
            return false
        }

        return scheduleAlarm(context, reminder)
    }

    fun canScheduleExactAlarms(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return alarmManager.canScheduleExactAlarms()
    }

    fun requestExactAlarmPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = "package:${context.packageName}".toUri()
                }
                context.startActivity(intent)
            }
        }
    }

    fun scheduleAlarm(context: Context, reminder: ReminderData): Boolean {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (!canScheduleExactAlarms(context)) {
            scheduleInexactAlarm(context, reminder)
            Log.w("AlarmScheduler", "Exact alarm access unavailable; scheduled an inexact fallback.")
            return false
        }

        return try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminder.reminderTime,
                reminderPendingIntent(context, reminder),
            )
            Log.d("AlarmScheduler", "Scheduled exact reminder ${reminder.reminderId}.")
            true
        } catch (e: SecurityException) {
            Log.w("AlarmScheduler", "Exact alarm rejected; scheduling fallback.", e)
            scheduleInexactAlarm(context, reminder)
            false
        }
    }

    private fun scheduleInexactAlarm(context: Context, reminder: ReminderData) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            reminder.reminderTime,
            reminderPendingIntent(context, reminder),
        )
    }

    private fun reminderPendingIntent(context: Context, reminder: ReminderData): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("reminder_id", reminder.reminderId)
            putExtra("reminder_title", reminder.reminderTitle)
            putExtra("reminder_message", reminder.reminderMessage)
        }
        return PendingIntent.getBroadcast(
            context,
            reminder.reminderId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    fun cancelAlarm(context: Context, reminderId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
