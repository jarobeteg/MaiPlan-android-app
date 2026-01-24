package com.example.maiplan.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

class AlarmManager {
    fun scheduleReminder(context: Context, reminder: ReminderData) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("reminder_id", reminder.reminderId)
            putExtra("reminder_title", reminder.reminderTitle)
            putExtra("reminder_message", reminder.reminderMessage)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.reminderId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminder.reminderTime,
                pendingIntent
            )
            return
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            reminder.reminderTime,
            pendingIntent
        )
    }
}