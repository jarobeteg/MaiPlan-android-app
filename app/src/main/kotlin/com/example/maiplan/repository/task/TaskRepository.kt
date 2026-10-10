package com.example.maiplan.repository.task

import android.content.Context
import com.example.maiplan.database.MaiPlanDatabase
import com.example.maiplan.network.sync.SyncScheduler
import com.example.maiplan.repository.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.map

class TaskRepository(
    private val local: TaskLocalDataSource,
    private val requestSync: () -> Unit = {},
    private val invalidateAlarm: (Long) -> Unit = {},
    private val recoverReminder: suspend (Long) -> String? = { null },
) {
    fun observeTasks(user: Long, query: TaskQuery = TaskQuery()) = local.observeTasks(user, query)
    fun observeTask(localId: Long, user: Long) = local.observeTask(localId, user)
    fun observeTaskSummaries(user: Long, query: TaskQuery = TaskQuery(), sort: TaskReadSort = TaskReadSort.PLANNED_DAY,
        limit: Int = TASK_READ_PAGE_SIZE, offset: Int = 0) = local.observeTaskSummaries(user, query, sort, limit, offset)

    fun observeTasksForDate(user: Long, date: java.time.LocalDate, categoryFilter: TaskCategoryFilter = TaskCategoryFilter.All,
        statusFilter: Set<TaskStatus> = TaskStatus.entries.toSet(), limit: Int = TASK_READ_PAGE_SIZE, offset: Int = 0) =
        observeTaskSummaries(user, TaskQuery(categoryFilter, TaskDateFilter.On(date), statusFilter), limit = limit, offset = offset)
            .map { TaskDayPage(it.items.map(TaskSummary::dayItem), it.offset, it.hasMore) }

    fun observePastPlannedTasks(user: Long, before: java.time.LocalDate,
        categoryFilter: TaskCategoryFilter = TaskCategoryFilter.All, limit: Int = TASK_READ_PAGE_SIZE, offset: Int = 0) =
        observeTaskSummaries(user, TaskQuery(categoryFilter, TaskDateFilter.Before(before)), limit = limit, offset = offset)
    suspend fun getTask(localId: Long, user: Long) = local.getTask(localId, user)
    suspend fun getTasks(user: Long, query: TaskQuery = TaskQuery()) = local.getTasks(user, query)
    suspend fun createTask(user: Long, input: CreateTaskInput) = write { local.createTask(user, input) }
    suspend fun updateTask(user: Long, input: UpdateTaskInput) = write { local.updateTask(user, input) }
    suspend fun setTaskStatus(localId: Long, user: Long, status: TaskStatus) = write { local.setTaskStatus(localId, user, status) }
    suspend fun setSubtaskStatus(localId: Long, user: Long, status: TaskStatus) = write { local.setSubtaskStatus(localId, user, status) }
    suspend fun addSubtask(localId: Long, user: Long, input: SubtaskDraft) = write { local.addSubtask(localId, user, input) }
    suspend fun updateSubtask(localId: Long, user: Long, input: SubtaskDraft) = write { local.updateSubtask(localId, user, input) }
    suspend fun reorderSubtasks(localId: Long, user: Long, order: List<Long>, expectedOrder: List<Long>? = null) =
        write { local.reorderSubtasks(localId, user, order, expectedOrder) }
    suspend fun softDeleteSubtask(localId: Long, user: Long) = write { local.softDeleteSubtask(localId, user) }
    suspend fun softDeleteTask(localId: Long, user: Long) = write { local.softDeleteTask(localId, user) }
    suspend fun createSeries(user: Long, input: CreateTaskSeriesInput) = write { local.createSeries(user, input) }
    suspend fun seriesDefinition(user: Long, id: java.util.UUID) = local.seriesDefinition(user, id)
    suspend fun categorySyncId(user: Long, id: Long?) = local.categorySyncId(user, id)
    suspend fun categoryLocalId(user: Long, id: String?) = local.categoryLocalId(user, id)
    suspend fun loadMoreHistory(user: Long) = local.series.loadMoreHistory(user).also { requestSync() }
    suspend fun deleteAllSeries(localId: Long, user: Long, id: java.util.UUID) = write { local.deleteAllSeries(user, id, localId) }
    suspend fun editFuture(user: Long, id: java.util.UUID, revision: TaskSeriesRevision, expected: String, cutoff: java.time.LocalDate, taskId: Long): com.example.maiplan.repository.Result<TaskWriteOutcome> = write {
        com.example.maiplan.repository.handleLocalResponse {
            local.series.editFuture(user, id, revision, expected, cutoff)
            val current = checkNotNull(local.taskEntity(user, taskId))
            val next = if (current.deletedAt == null) current else {
                val page = local.series.ensurePage(user, id, cutoff, TaskContract.MAX_DATE)
                page.taskIds.firstOrNull()?.let { local.taskEntity(user, it) } ?: current
            }
            TaskWriteOutcome(next, true)
        }
    }
    suspend fun nextOccurrence(user: Long, seriesId: java.util.UUID, after: java.time.LocalDate): Long? {
        if (after == TaskContract.MAX_DATE) return null
        var from = after.plusDays(1)
        while (true) {
            val page = local.series.ensurePage(user, seriesId, from, TaskContract.MAX_DATE)
            page.taskIds.firstOrNull()?.let { requestSync(); return it }
            from = page.nextDate ?: return null
        }
    }

    private suspend fun write(block: suspend () -> Result<TaskWriteOutcome>): Result<TaskWriteOutcome> = withContext(NonCancellable + Dispatchers.IO) {
        val result = block()
        if (result !is Result.Success || !result.data.changed) return@withContext result
        val warnings = mutableListOf<String>()
        if (result.data.invalidateReminder) runCatching { invalidateAlarm(result.data.task.taskLocalId) }
            .onFailure { warnings += "Task saved; reminder cancellation needs recovery." }
        if (result.data.invalidateReminder) runCatching { recoverReminder(result.data.task.taskLocalId) }
            .onSuccess { it?.let(warnings::add) }
            .onFailure { warnings += "Task saved; reminder scheduling needs recovery." }
        runCatching(requestSync).onFailure { warnings += "Task saved; sync scheduling needs retry." }
        Result.Success(result.data.copy(sideEffectWarning = warnings.takeIf { it.isNotEmpty() }?.joinToString(" ")))
    }

    companion object {
        fun create(context: Context): TaskRepository {
            val app = context.applicationContext
            return TaskRepository(TaskLocalDataSource(MaiPlanDatabase.getDatabase(app), generationCommitted = { SyncScheduler.runOneTimeSync(app) }),
                requestSync = { SyncScheduler.runOneTimeSync(app) },
                recoverReminder = {
                    val coordinator = com.example.maiplan.utils.notifications.ReminderCoordinator(app)
                    com.example.maiplan.utils.notifications.enqueueEventAlarmRecovery(app)
                    coordinator.refreshTask(it)
                    coordinator.warning("task:$it")
                })
        }
    }
}
