package com.example.maiplan.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getIntExtra("reminder_id", 0)
        val reminderTitle = intent.getStringExtra("reminder_title") ?: "Title"
        val reminderMessage = intent.getStringExtra("reminder_message") ?: "Message"

        showNotification(context, reminderId, reminderTitle, reminderMessage)
    }

    fun showNotification(
        context: Context,
        reminderId: Int,
        reminderTitle: String,
        reminderMessage: String
    ) {
        val channelId = "reminders"

        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            channelId,
            "Reminders",
            NotificationManager.IMPORTANCE_HIGH
        )
        manager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(context.applicationInfo.icon)
            .setContentTitle(reminderTitle)
            .setContentText(reminderMessage)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        manager.notify(reminderId, notification)
    }
}
