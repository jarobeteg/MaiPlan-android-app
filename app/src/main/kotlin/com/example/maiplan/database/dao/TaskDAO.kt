package com.example.maiplan.database.dao

import androidx.room.*
import com.example.maiplan.database.entities.SubtaskEntity
import com.example.maiplan.database.entities.TaskEntity
import com.example.maiplan.database.entities.ReminderEntity
import com.example.maiplan.database.entities.taskSearchText
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.util.UUID

data class TaskRecord(
    @Embedded val task: TaskEntity,
    @ColumnInfo(name = "effective_category_local_id") val effectiveCategoryLocalId: Long?,
    @Relation(parentColumn = "task_local_id", entityColumn = "task_local_id") val subtasks: List<SubtaskEntity>,
    @Relation(parentColumn = "reminder_local_id", entityColumn = "reminder_local_id") val reminder: ReminderEntity?,
)

data class TaskSummaryRecord(
    @Embedded val task: TaskEntity,
    @ColumnInfo(name = "summary_category_id") val categoryLocalId: Long?,
    @ColumnInfo(name = "summary_category_uuid") val categorySyncId: UUID?,
    @ColumnInfo(name = "summary_category_name") val categoryName: String?,
    @ColumnInfo(name = "summary_category_color") val categoryColor: String?,
    @ColumnInfo(name = "summary_category_icon") val categoryIcon: String?,
    @ColumnInfo(name = "summary_step_count") val stepCount: Int,
    @ColumnInfo(name = "summary_finished") val finishedSteps: Int,
    @ColumnInfo(name = "summary_done") val doneSteps: Int,
    @ColumnInfo(name = "summary_known_estimates") val knownEstimates: Int,
    @ColumnInfo(name = "summary_step_estimate") val stepEstimate: Long?,
    @ColumnInfo(name = "summary_reminder_time") val reminderTime: Long?,
    @ColumnInfo(name = "summary_reminder_zone") val reminderZone: String?,
    @ColumnInfo(name = "summary_reminder_status") val reminderStatus: Int?,
)

private const val TASK_JOIN = """
    SELECT t.*, c.category_local_id AS effective_category_local_id FROM task t
    LEFT JOIN category c ON c.category_local_id = t.category_local_id
      AND c.user_local_id = t.user_local_id AND c.deleted_at IS NULL
    WHERE t.user_local_id = :userLocalId AND t.deleted_at IS NULL
    AND (t.series_id IS NULL OR EXISTS (SELECT 1 FROM task_series s WHERE s.sync_id = t.series_id AND s.user_local_id = t.user_local_id AND s.deleted_at IS NULL))
"""
private const val TASK_PREDICATES = """
    AND (:categoryMode = 0 OR (:categoryMode = 1 AND c.category_local_id IS NULL)
      OR (:categoryMode = 2 AND c.category_local_id = :categoryLocalId))
    AND (:dateMode = 0 OR (:dateMode = 1 AND t.scheduled_date IS NULL)
      OR (:dateMode = 2 AND t.scheduled_date BETWEEN :fromDate AND :throughDate)
      OR (:dateMode = 3 AND t.scheduled_date < :throughDate))
    AND t.status IN (:statuses)
    AND (:search = '' OR instr(t.search_text, :search) > 0)
"""
private const val TASK_FILTER = TASK_PREDICATES + " ORDER BY (t.scheduled_date IS NULL), t.scheduled_date, t.title COLLATE NOCASE, t.sync_id"

private const val TASK_SUMMARY = """
    SELECT t.*, c.category_local_id AS summary_category_id, c.sync_id AS summary_category_uuid,
      c.name AS summary_category_name, c.color AS summary_category_color, c.icon AS summary_category_icon,
      (SELECT COUNT(*) FROM subtask s WHERE s.task_local_id = t.task_local_id AND s.user_local_id = t.user_local_id AND s.deleted_at IS NULL) AS summary_step_count,
      (SELECT COUNT(*) FROM subtask s WHERE s.task_local_id = t.task_local_id AND s.user_local_id = t.user_local_id AND s.deleted_at IS NULL AND s.status IN (1, 3, 4)) AS summary_finished,
      (SELECT COUNT(*) FROM subtask s WHERE s.task_local_id = t.task_local_id AND s.user_local_id = t.user_local_id AND s.deleted_at IS NULL AND s.status = 1) AS summary_done,
      (SELECT COUNT(s.estimated_time) FROM subtask s WHERE s.task_local_id = t.task_local_id AND s.user_local_id = t.user_local_id AND s.deleted_at IS NULL) AS summary_known_estimates,
      (SELECT SUM(s.estimated_time) FROM subtask s WHERE s.task_local_id = t.task_local_id AND s.user_local_id = t.user_local_id AND s.deleted_at IS NULL) AS summary_step_estimate,
      r.reminder_time AS summary_reminder_time, r.zone_id AS summary_reminder_zone, r.status AS summary_reminder_status
    FROM (
      SELECT t.task_local_id FROM task t LEFT JOIN category c ON c.category_local_id = t.category_local_id
        AND c.user_local_id = t.user_local_id AND c.deleted_at IS NULL
      WHERE t.user_local_id = :userLocalId AND t.deleted_at IS NULL
        AND (t.series_id IS NULL OR EXISTS (SELECT 1 FROM task_series s WHERE s.sync_id = t.series_id AND s.user_local_id = t.user_local_id AND s.deleted_at IS NULL))
""" + TASK_PREDICATES + """
      ORDER BY CASE WHEN :sortMode = 1 THEN (c.category_local_id IS NULL) END,
        CASE WHEN :sortMode = 1 THEN c.name END COLLATE NOCASE, CASE WHEN :sortMode = 1 THEN c.category_local_id END,
        CASE WHEN :sortMode = 2 THEN t.completed_date END DESC,
        (t.scheduled_date IS NULL), t.scheduled_date, t.title COLLATE NOCASE, t.sync_id
      LIMIT :limit OFFSET :offset
    ) page JOIN task t ON t.task_local_id = page.task_local_id
    LEFT JOIN category c ON c.category_local_id = t.category_local_id
      AND c.user_local_id = t.user_local_id AND c.deleted_at IS NULL
    LEFT JOIN reminder r ON r.reminder_local_id = t.reminder_local_id
      AND r.user_local_id = t.user_local_id AND r.deleted_at IS NULL
    ORDER BY CASE WHEN :sortMode = 1 THEN (c.category_local_id IS NULL) END,
      CASE WHEN :sortMode = 1 THEN c.name END COLLATE NOCASE,
      CASE WHEN :sortMode = 1 THEN c.category_local_id END,
      CASE WHEN :sortMode = 2 THEN t.completed_date END DESC,
      (t.scheduled_date IS NULL), t.scheduled_date, t.title COLLATE NOCASE, t.sync_id
"""

@Dao
interface TaskDAO {
    @Query(TASK_SUMMARY)
    fun observeSummaries(userLocalId: Long, categoryMode: Int, categoryLocalId: Long?, dateMode: Int,
        fromDate: LocalDate?, throughDate: LocalDate?, statuses: List<Int>, search: String,
        sortMode: Int, limit: Int, offset: Int): Flow<List<TaskSummaryRecord>>

    @Query("""SELECT EXISTS(SELECT 1 FROM task t WHERE t.user_local_id = :user AND t.deleted_at IS NULL
        AND (t.series_id IS NULL OR EXISTS (SELECT 1 FROM task_series s WHERE s.sync_id = t.series_id AND s.user_local_id = t.user_local_id AND s.deleted_at IS NULL)))""")
    fun observeHasTasks(user: Long): Flow<Boolean>

    @Query("""SELECT * FROM task WHERE user_local_id = :user AND task_local_id > :after
        AND (reminder_local_id IS NOT NULL OR relative_reminder_json IS NOT NULL
          OR EXISTS(SELECT 1 FROM scheduled_reminder q WHERE q.taskLocalId = task.task_local_id AND q.userLocalId = :user))
        ORDER BY task_local_id LIMIT :limit""")
    suspend fun reminderPage(user: Long, after: Long, limit: Int = 100): List<TaskEntity>
    @Query("SELECT * FROM task WHERE user_local_id = :user AND series_id = :series AND task_local_id > :after ORDER BY task_local_id LIMIT :limit")
    suspend fun seriesPage(user: Long, series: String, after: Long, limit: Int = 32): List<TaskEntity>
    @Query("SELECT * FROM task WHERE user_local_id = :userLocalId")
    suspend fun allForUser(userLocalId: Long): List<TaskEntity>
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insertTaskRow(task: TaskEntity): Long
    @Update suspend fun updateTaskRow(task: TaskEntity): Int
    suspend fun insertTask(task: TaskEntity): Long = insertTaskRow(task.copy(searchText = taskSearchText(task.title, task.description)))
    suspend fun updateTask(task: TaskEntity): Int = updateTaskRow(task.copy(searchText = taskSearchText(task.title, task.description)))

    @Query("SELECT * FROM task WHERE task_local_id = :localId AND user_local_id = :userLocalId")
    suspend fun getTaskByLocalId(localId: Long, userLocalId: Long): TaskEntity?

    @Query("SELECT * FROM task WHERE sync_id = :syncId AND user_local_id = :userLocalId")
    suspend fun getTaskBySyncId(syncId: UUID, userLocalId: Long): TaskEntity?

    @Transaction @Query(TASK_JOIN + " AND t.task_local_id = :localId")
    suspend fun getActiveTask(localId: Long, userLocalId: Long): TaskRecord?

    @Transaction @Query(TASK_JOIN + " AND t.task_local_id = :localId")
    fun observeActiveTask(localId: Long, userLocalId: Long): Flow<TaskRecord?>

    @Transaction @Query(TASK_JOIN + TASK_FILTER)
    suspend fun getActiveTasks(userLocalId: Long, categoryMode: Int, categoryLocalId: Long?, dateMode: Int,
        fromDate: LocalDate?, throughDate: LocalDate?, statuses: List<Int>, search: String): List<TaskRecord>

    @Transaction @Query(TASK_JOIN + TASK_FILTER)
    fun observeActiveTasks(userLocalId: Long, categoryMode: Int, categoryLocalId: Long?, dateMode: Int,
        fromDate: LocalDate?, throughDate: LocalDate?, statuses: List<Int>, search: String): Flow<List<TaskRecord>>

    @Query("""UPDATE task SET server_version = :serverVersion WHERE sync_id = :syncId
        AND user_local_id = :userLocalId AND :serverVersion > 0
        AND (server_version IS NULL OR server_version < :serverVersion)""")
    suspend fun updateServerVersion(syncId: UUID, userLocalId: Long, serverVersion: Long): Int

    @Query("""SELECT COUNT(*) FROM task WHERE reminder_local_id = :reminderLocalId
        AND task_local_id != :exceptTaskLocalId AND deleted_at IS NULL""")
    suspend fun countOtherReminderReferences(reminderLocalId: Long, exceptTaskLocalId: Long): Int
}
