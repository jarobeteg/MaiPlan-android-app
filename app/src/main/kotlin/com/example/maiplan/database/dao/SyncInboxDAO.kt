package com.example.maiplan.database.dao

import androidx.room.*
import com.example.maiplan.database.entities.SyncInboxEntity
import java.util.UUID
import kotlinx.coroutines.flow.Flow

data class TaskInboxTombstone(
    @ColumnInfo(name = "entity_type") val entityType: String,
    @ColumnInfo(name = "entity_sync_id") val entitySyncId: UUID,
)

@Dao
interface SyncInboxDAO {
    @Query("""SELECT i.entity_type, i.entity_sync_id FROM sync_inbox i
        WHERE i.user_local_id = :user AND i.operation = 'DELETE' AND EXISTS (
          SELECT 1 FROM outbox o WHERE o.user_local_id = i.user_local_id AND o.entity_sync_id = i.entity_sync_id
            AND o.status != 'SYNCED' AND ((i.entity_type = 'task' AND o.entity_type = 'task_action')
              OR (i.entity_type = 'task_series' AND o.entity_type = 'task_series_action')))
        """)
    fun observeTaskTombstones(user: Long): Flow<List<TaskInboxTombstone>>
    @Query("""SELECT EXISTS(SELECT 1 FROM sync_inbox WHERE user_local_id = :user
        AND entity_type = 'task' AND entity_sync_id = :task AND operation = 'DELETE')""")
    fun observeTaskDeleted(user: Long, task: UUID): Flow<Boolean>
    @Query("SELECT * FROM sync_inbox WHERE user_local_id = :user")
    suspend fun allForUser(user: Long): List<SyncInboxEntity>
    @Query("SELECT * FROM sync_inbox WHERE user_local_id = :user")
    fun observeUser(user: Long): Flow<List<SyncInboxEntity>>
    @Query("SELECT * FROM sync_inbox WHERE user_local_id = :user AND entity_type = :type AND entity_sync_id = :id")
    suspend fun get(user: Long, type: String, id: UUID): SyncInboxEntity?
    @Query("SELECT * FROM sync_inbox WHERE user_local_id = :user AND applied = 0 ORDER BY sequence, entity_type, entity_sync_id")
    suspend fun pending(user: Long): List<SyncInboxEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun put(row: SyncInboxEntity)
}
