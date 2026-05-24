package com.example.maiplan.utils.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ALARM_TEST", "!!! RECEIVER HIT SUCCESSFULLY !!!")
        val reminderId = intent.getIntExtra("reminder_id", 0)
        val reminderTitle = intent.getStringExtra("reminder_title") ?: "Title"
        val reminderMessage = intent.getStringExtra("reminder_message") ?: "Message"

        NotificationHelper.createNotificationChannel(context)

        NotificationHelper.showNotification(context, reminderTitle, reminderMessage, reminderId)
    }
}