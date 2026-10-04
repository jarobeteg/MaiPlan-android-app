package com.example.maiplan.utils.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.i("ReminderReceiver", "Alarm received: ${intent.getStringExtra("alarm_key")}")
        recoverReminders(context)
    }
}
