package com.example.maiplan.database.dao

import androidx.room.*
import com.example.maiplan.database.entities.SubtaskEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

private const val ACTIVE_STEPS = """SELECT s.* FROM subtask s JOIN task t ON t.task_local_id = s.task_local_id
    WHERE s.task_local_id = :taskLocalId AND s.user_local_id = :userLocalId
      AND t.user_local_id = :userLocalId AND t.deleted_at IS NULL AND s.deleted_at IS NULL
    ORDER BY s.sort_order, s.sync_id"""

@Dao
interface SubtaskDAO {
    @Query("SELECT * FROM subtask WHERE task_local_id = :task AND user_local_id = :user ORDER BY sort_order, sync_id")
    suspend fun allForTask(task: Long, user: Long): List<SubtaskEntity>
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insertSubtask(step: SubtaskEntity): Long
    @Update suspend fun updateSubtask(step: SubtaskEntity): Int

    @Query("SELECT * FROM subtask WHERE subtask_local_id = :localId AND user_local_id = :userLocalId")
    suspend fun getSubtaskByLocalId(localId: Long, userLocalId: Long): SubtaskEntity?

    @Query("SELECT * FROM subtask WHERE sync_id = :syncId AND user_local_id = :userLocalId")
    suspend fun getSubtaskBySyncId(syncId: UUID, userLocalId: Long): SubtaskEntity?

    @Query(ACTIVE_STEPS) suspend fun getActiveSubtasks(taskLocalId: Long, userLocalId: Long): List<SubtaskEntity>
    @Query(ACTIVE_STEPS) fun observeActiveSubtasks(taskLocalId: Long, userLocalId: Long): Flow<List<SubtaskEntity>>

    @Query("""UPDATE subtask SET server_version = :serverVersion WHERE sync_id = :syncId
        AND user_local_id = :userLocalId AND :serverVersion > 0
        AND (server_version IS NULL OR server_version < :serverVersion)""")
    suspend fun updateServerVersion(syncId: UUID, userLocalId: Long, serverVersion: Long): Int
}
