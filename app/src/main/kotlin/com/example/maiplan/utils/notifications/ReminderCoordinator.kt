package com.example.maiplan.utils.notifications

import android.content.Context
import android.util.Log
import androidx.room.withTransaction
import androidx.work.*
import com.example.maiplan.database.MaiPlanDatabase
import com.example.maiplan.database.entities.ScheduledReminderEntity
import com.example.maiplan.repository.event.occurrencesIntersecting
import com.example.maiplan.utils.SessionManager
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.CancellationException
import java.time.Instant
import java.time.LocalDate
import java.util.concurrent.TimeUnit

class ReminderCoordinator(
    context: Context,
    private val database: MaiPlanDatabase = MaiPlanDatabase.getDatabase(context.applicationContext),
) {
    private val context = context.applicationContext
    private val queue = database.scheduledReminderDAO()
    private val writer = ReminderPlanWriter(database)

    suspend fun recover(userLocalId: Long? = null) = lock.withLock {
        val users = queue.pendingUserIds().toMutableSet()
        userLocalId?.let(users::add)
        SessionManager(context).getActiveUserSyncId()?.let {
            database.userDAO().getActiveUserBySyncId(it)?.userLocalId?.let(users::add)
        }
        val now = Instant.now()
        deliverDue()
        database.withTransaction {
            for (row in queue.pending()) {
                if (currentContent(row) == null) {
                    queue.removePending(row.alarmKey)
                    ReminderAlarmScheduler.cancel(context, row.sourceKey)
                    WorkManager.getInstance(context).cancelUniqueWork(backupName(row.alarmKey))
                }
            }
            for (user in users) reconcileUser(user, now)
        }

        deliverDue()

        for (row in queue.pending()) {
            if (row.triggerAt > System.currentTimeMillis()) try {
                val exact = ReminderAlarmScheduler.schedule(context, row)
                val warning = when {
                    !NotificationHelper.canDeliverReminders(context) -> "Notifications are disabled. Enable them in Settings."
                    !exact -> "Allow Alarms & reminders in Settings for on-time delivery."
                    else -> null
                }
                queue.markScheduled(row.alarmKey, System.currentTimeMillis(), warning)
                Log.i(TAG, "Armed ${row.alarmKey}, exact=$exact")
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                queue.markScheduled(row.alarmKey, null, "Could not register the alarm. Recovery will retry.")
                Log.e(TAG, "Could not arm ${row.alarmKey}", error)
            }
            try {
                ensureBackup(row)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Log.e(TAG, "Backup registration will be retried for ${row.alarmKey}", error)
                enqueueEventAlarmRecovery(context)
            }
        }
    }

    private suspend fun deliverDue() {
        for (row in queue.pending().filter { it.triggerAt <= System.currentTimeMillis() }) {
            database.withTransaction {
                val pending = queue.get(row.alarmKey)?.takeIf { it.deliveredAt == null }
                    ?: return@withTransaction
                val current = currentContent(pending)
                if (current == null) {
                    queue.removePending(row.alarmKey)
                    return@withTransaction
                }
                val posted = NotificationHelper.showNotification(
                    context, current.title, current.message, 0,
                    notificationTag = current.alarmKey, scheduledTime = current.triggerAt,
                )
                if (posted) {
                    if (row.eventLocalId != null && row.occurrenceDate != null) {
                        val event = database.eventDAO().getEventByLocalId(row.eventLocalId, row.userLocalId)
                        event?.let {
                            eventReminderPlan(it, null, Instant.now())?.let { next -> writer.enqueue(next) }
                        }
                    }
                    queue.markDelivered(row.alarmKey, System.currentTimeMillis())
                    Log.i(TAG, "Delivered ${row.alarmKey}")
                } else {
                    queue.markScheduled(row.alarmKey, null, "Notifications are disabled. Enable them in Settings.")
                }
            }
            if (queue.get(row.alarmKey)?.deliveredAt != null) {
                WorkManager.getInstance(context).cancelUniqueWork(backupName(row.alarmKey))
            }
        }

    }

    suspend fun warning(source: String): String? = queue.pending()
        .firstOrNull { it.sourceKey == source && it.lastError != null }?.lastError

    private suspend fun reconcileUser(user: Long, now: Instant) {
        if (database.userDAO().getActiveUserByLocalId(user) == null) return
        val reminders = database.reminderDAO().getAllForUser(user).associateBy { it.reminderLocalId }
        for (id in database.eventDAO().getAllEventIdsForUser(user)) {
            val event = database.eventDAO().getEventByLocalId(id, user) ?: continue
            val reminder = event.reminderLocalId?.let(reminders::get)
            val plan = eventReminderPlan(event, reminder, now)
            if (plan != null) enqueueFuture(plan, now)
            if (plan == null && queue.pending().none { it.sourceKey == "event:$id" }) {
                ReminderAlarmScheduler.cancel(context, "event:$id")
            }
        }
        for (note in database.noteDAO().getNotes(user)) {
            val reminder = note.reminderLocalId?.let(reminders::get)
            val plan = noteReminderPlan(note, reminder)
            if (plan != null) enqueueFuture(plan, now)
            else ReminderAlarmScheduler.cancel(context, "note:${note.noteLocalId}")
        }
    }

    private suspend fun enqueueFuture(plan: ScheduledReminderEntity, now: Instant) {
        if (plan.triggerAt > now.toEpochMilli()) {
            writer.enqueue(plan)
        }
    }

    private suspend fun currentContent(row: ScheduledReminderEntity): ScheduledReminderEntity? {
        if (database.userDAO().getActiveUserByLocalId(row.userLocalId) == null) return null
        row.eventLocalId?.let { id ->
            val event = database.eventDAO().getEventByLocalId(id, row.userLocalId)
                ?.takeIf { it.deletedAt == null } ?: return null
            if (row.occurrenceDate != null) {
                val date = LocalDate.parse(row.occurrenceDate)
                val occurrence = event.occurrencesIntersecting(date, date.plusDays(1))
                    .firstOrNull { it.originalStartDate == date } ?: return null
                if (eventReminderTrigger(event, occurrence) != row.triggerAt) return null
                return row.copy(title = event.title, message = event.relativeReminderMessage.orEmpty())
            }
            val reminder = event.reminderLocalId?.let { database.reminderDAO().getReminderByLocalId(it, row.userLocalId) }
                ?.takeIf { it.deletedAt == null && it.reminderTime == row.triggerAt } ?: return null
            return row.copy(title = event.title, message = reminder.message.orEmpty())
        }
        row.noteLocalId?.let { id ->
            val note = database.noteDAO().getNoteByLocalId(id, row.userLocalId)
                ?.takeIf { it.deletedAt == null } ?: return null
            val reminder = note.reminderLocalId?.let { database.reminderDAO().getReminderByLocalId(it, row.userLocalId) }
                ?.takeIf { it.deletedAt == null && it.reminderTime == row.triggerAt } ?: return null
            return row.copy(title = note.title, message = reminder.message.orEmpty())
        }
        return null
    }

    private suspend fun ensureBackup(row: ScheduledReminderEntity) {
        val request = OneTimeWorkRequestBuilder<ReminderBackupWorker>()
            .setInputData(workDataOf("alarm_key" to row.alarmKey))
            .setInitialDelay((row.triggerAt + 60_000L - System.currentTimeMillis()).coerceAtLeast(0L), TimeUnit.MILLISECONDS)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(backupName(row.alarmKey), ExistingWorkPolicy.KEEP, request).await()
    }

    companion object {
        private val lock = Mutex()
        private const val TAG = "ReminderCoordinator"
        private fun backupName(key: String) = "reminder-backup:$key"
    }
}

class ReminderBackupWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return try {
            ReminderCoordinator(applicationContext).recover()
            val key = inputData.getString("alarm_key") ?: return Result.success()
            val row = MaiPlanDatabase.getDatabase(applicationContext).scheduledReminderDAO().get(key)
            if (row != null && row.deliveredAt == null) Result.retry() else Result.success()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Log.e("ReminderBackupWorker", "Retrying pending reminder recovery", error)
            Result.retry()
        }
    }
}
