package com.example.maiplan.utils.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class EventReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val eventId = intent.getLongExtra("event_local_id", 0L)
        val date = runCatching {
            LocalDate.parse(intent.getStringExtra("occurrence_date"))
        }.getOrNull() ?: return
        val trigger = intent.getLongExtra("trigger_at", 0L)
        if (eventId <= 0L || trigger <= 0L) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                EventAlarmCoordinator(context.applicationContext)
                    .deliverIfCurrent(eventId, date, trigger)
            } catch (error: Exception) {
                Log.e("EventReminderReceiver", "Could not deliver event reminder", error)
            } finally {
                pending.finish()
            }
        }
    }
}
