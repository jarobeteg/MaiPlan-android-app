package com.example.maiplan.utils.notifications

import android.content.Context
import androidx.work.*
import com.example.maiplan.database.entities.ScheduledReminderEntity
import java.util.concurrent.TimeUnit

interface ReminderEffects {
    fun post(row: ScheduledReminderEntity, destination: TaskReminderDestination?): Boolean
    fun schedule(row: ScheduledReminderEntity): Boolean
    fun notificationsEnabled(): Boolean
    fun cancelAlarm(source: String)
    fun cancelBackup(key: String)
    fun cancelSourceBackups(source: String)
    fun clearNotification(key: String)
    fun clearSourceNotifications(source: String)
    suspend fun backup(row: ScheduledReminderEntity, nowMillis: Long)
    fun retryRecovery()
}

class AndroidReminderEffects(private val context: Context) : ReminderEffects {
    override fun post(row: ScheduledReminderEntity, destination: TaskReminderDestination?) =
        NotificationHelper.showNotification(context, row.title, row.message, 0, row.alarmKey, row.triggerAt, destination)
    override fun schedule(row: ScheduledReminderEntity) = ReminderAlarmScheduler.schedule(context, row)
    override fun notificationsEnabled() = NotificationHelper.canDeliverReminders(context)
    override fun cancelAlarm(source: String) = ReminderAlarmScheduler.cancel(context, source)
    override fun cancelBackup(key: String) { WorkManager.getInstance(context).cancelUniqueWork("reminder-backup:$key") }
    override fun cancelSourceBackups(source: String) { WorkManager.getInstance(context).cancelAllWorkByTag("reminder-source:$source") }
    override fun clearNotification(key: String) = NotificationHelper.cancel(context, key)
    override fun clearSourceNotifications(source: String) = NotificationHelper.cancelSource(context, source)
    override fun retryRecovery() = enqueueEventAlarmRecovery(context)
    override suspend fun backup(row: ScheduledReminderEntity, nowMillis: Long) {
        val request = OneTimeWorkRequestBuilder<ReminderBackupWorker>()
            .addTag("reminder-source:${row.sourceKey}")
            .setInputData(workDataOf("alarm_key" to row.alarmKey))
            .setInitialDelay((row.triggerAt + 60_000L - nowMillis).coerceAtLeast(0L), TimeUnit.MILLISECONDS)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES).build()
        WorkManager.getInstance(context).enqueueUniqueWork("reminder-backup:${row.alarmKey}", ExistingWorkPolicy.KEEP, request).await()
    }
}
