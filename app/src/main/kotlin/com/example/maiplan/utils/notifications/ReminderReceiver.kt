package com.example.maiplan.utils.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ALARM_TEST", "!!! RECEIVER HIT SUCCESSFULLY !!!")
        val reminderLocalId = intent.getLongExtra("reminder_local_id", 0L)
        val reminderTitle = intent.getStringExtra("reminder_title") ?: "Title"
        val reminderMessage = intent.getStringExtra("reminder_message") ?: "Message"

        NotificationHelper.createNotificationChannel(context)

        val notificationId = (reminderLocalId xor (reminderLocalId ushr 32)).toInt()
        NotificationHelper.showNotification(context, reminderTitle, reminderMessage, notificationId)
    }
}
