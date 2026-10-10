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
import java.time.Clock
import java.util.UUID

class ReminderCoordinator(
    context: Context,
    private val database: MaiPlanDatabase = MaiPlanDatabase.getDatabase(context.applicationContext),
    private val effects: ReminderEffects = AndroidReminderEffects(context.applicationContext),
    private val clock: Clock = Clock.systemUTC(),
    private val signedInOwner: () -> UUID? = { SessionManager(context).let { if (it.hasSession()) it.getActiveUserSyncId() else null } },
) {
    private val appContext = context.applicationContext
    private val queue = database.scheduledReminderDAO()
    private val writer = ReminderPlanWriter(database)

    suspend fun recover(userLocalId: Long? = null) = lock.withLock { recoverLocked(userLocalId) }

    suspend fun refreshTask(taskLocalId: Long) = lock.withLock {
        val source = "task:$taskLocalId"
        effects.cancelAlarm(source)
        effects.cancelSourceBackups(source)
        effects.clearSourceNotifications(source)
        queue.forSource(source).forEach { effects.clearNotification(it.alarmKey) }
        recoverLocked(null)
    }

    private suspend fun recoverLocked(userLocalId: Long?) {
        val users = queue.pendingUserIds().toMutableSet()
        users += queue.taskUserIds()
        userLocalId?.let(users::add)
        signedInOwner()?.let {
            database.userDAO().getActiveUserBySyncId(it)?.userLocalId?.let(users::add)
        }
        val now = clock.instant()
        for (user in users) if (database.userDAO().getActiveUserByLocalId(user)?.syncId == signedInOwner()) {
            val before = database.outboxDAO().lastLocalId(user)
            runCatching { com.example.maiplan.repository.task.TaskSeriesStore(database, clock).prepareUpcoming(user) }
                .onFailure { Log.w("ReminderCoordinator", "Task series preparation needs retry", it) }
            if (effects is AndroidReminderEffects && database.outboxDAO().lastLocalId(user) > before)
                com.example.maiplan.network.sync.SyncScheduler.runOneTimeSync(appContext)
        }
        deliverDue()
        database.withTransaction {
            for (row in queue.pending()) {
                if (currentContent(row) == null) {
                    queue.removePending(row.alarmKey)
                    effects.cancelAlarm(row.sourceKey)
                    effects.cancelBackup(row.alarmKey)
                    effects.clearNotification(row.alarmKey)
                }
            }
            for (user in users) reconcileUser(user, now)
        }

        deliverDue()

        for (row in queue.pending()) {
            if (row.triggerAt > clock.millis()) try {
                val exact = effects.schedule(row)
                val warning = when {
                    !effects.notificationsEnabled() -> "Notifications are disabled. Enable them in Settings."
                    !exact -> "Allow Alarms & reminders in Settings for on-time delivery."
                    else -> null
                }
                queue.markScheduled(row.alarmKey, clock.millis(), warning)
                Log.i(TAG, "Armed ${row.alarmKey}, exact=$exact")
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                queue.markScheduled(row.alarmKey, null, "Could not register the alarm. Recovery will retry.")
                Log.e(TAG, "Could not arm ${row.alarmKey}", error)
            }
            try {
                effects.backup(row, clock.millis())
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Log.e(TAG, "Backup registration will be retried for ${row.alarmKey}", error)
                effects.retryRecovery()
            }
        }
    }

    private suspend fun deliverDue() {
        for (row in queue.pending().filter { it.triggerAt <= clock.millis() }) {
            database.withTransaction {
                val pending = queue.get(row.alarmKey)?.takeIf { it.deliveredAt == null }
                    ?: return@withTransaction
                val current = currentContent(pending)
                if (current == null) {
                    queue.removePending(row.alarmKey)
                    effects.cancelAlarm(row.sourceKey)
                    effects.cancelBackup(row.alarmKey)
                    effects.clearNotification(row.alarmKey)
                    return@withTransaction
                }
                val destination = current.taskLocalId?.let { id ->
                    val task = database.taskDAO().getTaskByLocalId(id, current.userLocalId) ?: return@withTransaction
                    val owner = database.userDAO().getActiveUserByLocalId(current.userLocalId) ?: return@withTransaction
                    TaskReminderDestination(task.syncId, owner.syncId)
                }
                val posted = effects.post(current, destination)
                if (posted) {
                    if (row.eventLocalId != null && row.occurrenceDate != null) {
                        val event = database.eventDAO().getEventByLocalId(row.eventLocalId, row.userLocalId)
                        event?.let {
                            eventReminderPlan(it, null, clock.instant())?.let { next -> writer.enqueue(next) }
                        }
                    }
                    queue.markDelivered(row.alarmKey, clock.millis())
                    Log.i(TAG, "Delivered ${row.alarmKey}")
                } else {
                    queue.markScheduled(row.alarmKey, null, "Notifications are disabled. Enable them in Settings.")
                }
            }
            if (queue.get(row.alarmKey)?.deliveredAt != null) {
                effects.cancelBackup(row.alarmKey)
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
                effects.cancelAlarm("event:$id")
            }
        }
        for (note in database.noteDAO().getNotes(user)) {
            val reminder = note.reminderLocalId?.let(reminders::get)
            val plan = noteReminderPlan(note, reminder)
            if (plan != null) enqueueFuture(plan, now)
            else effects.cancelAlarm("note:${note.noteLocalId}")
        }
        val owner = database.userDAO().getActiveUserByLocalId(user)
        var after = 0L
        while (true) {
            val page = database.taskDAO().reminderPage(user, after)
            if (page.isEmpty()) break
            for (task in page) {
                val reminder = task.reminderLocalId?.let(reminders::get)
                if (owner?.syncId == signedInOwner()) writer.task(task, reminder, now)
                else queue.replacePending("task:${task.taskLocalId}", null)
                if (taskReminderPlan(task, reminder) == null || owner?.syncId != signedInOwner()) {
                    effects.cancelAlarm("task:${task.taskLocalId}")
                    effects.cancelSourceBackups("task:${task.taskLocalId}")
                    effects.clearSourceNotifications("task:${task.taskLocalId}")
                    queue.forSource("task:${task.taskLocalId}").forEach { effects.clearNotification(it.alarmKey) }
                }
            }
            after = page.last().taskLocalId
            kotlinx.coroutines.yield()
        }
    }

    private suspend fun enqueueFuture(plan: ScheduledReminderEntity, now: Instant) {
        if (plan.triggerAt > now.toEpochMilli()) {
            writer.enqueue(plan)
        }
    }

    private suspend fun currentContent(row: ScheduledReminderEntity): ScheduledReminderEntity? {
        if (database.userDAO().getActiveUserByLocalId(row.userLocalId) == null) return null
        row.taskLocalId?.let { id ->
            val owner = database.userDAO().getActiveUserByLocalId(row.userLocalId) ?: return null
            if (owner.syncId != signedInOwner()) return null
            val task = database.taskDAO().getTaskByLocalId(id, row.userLocalId) ?: return null
            if (taskSeriesSuppressesReminder(database, task)) return null
            val reminder = task.reminderLocalId?.let { database.reminderDAO().getReminderByLocalId(it, row.userLocalId) }
            val plan = taskReminderPlan(task, reminder) ?: return null
            return plan.takeIf { it.alarmKey == row.alarmKey }
        }
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

    companion object {
        private val lock = Mutex()
        private const val TAG = "ReminderCoordinator"
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
