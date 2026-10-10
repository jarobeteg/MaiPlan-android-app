package com.example.maiplan.database.dao

import com.example.maiplan.database.entities.OutboxEntity
import androidx.room.OnConflictStrategy
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Dao
import androidx.room.ColumnInfo
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.flow.Flow

data class TaskIntentState(
    @ColumnInfo(name = "entity_type") val entityType: String,
    @ColumnInfo(name = "entity_sync_id") val entitySyncId: UUID,
    val status: String,
    @ColumnInfo(name = "last_error") val lastError: String?,
)

@Dao
interface OutboxDAO {
    @Query("SELECT COALESCE(MAX(outbox_local_id), 0) FROM outbox WHERE user_local_id = :user")
    suspend fun lastLocalId(user: Long): Long
    @Query("SELECT * FROM outbox WHERE user_local_id = :user AND entity_type = :type AND entity_sync_id = :id ORDER BY outbox_local_id")
    suspend fun entityIntents(user: Long, type: String, id: UUID): List<OutboxEntity>
    @Query("SELECT * FROM outbox WHERE user_local_id = :user ORDER BY outbox_local_id")
    suspend fun allForUser(user: Long): List<OutboxEntity>
    @Query("""SELECT entity_type, entity_sync_id, status, last_error FROM outbox
        WHERE user_local_id = :user AND entity_type IN ('task_action', 'task_series_action') AND status != 'SYNCED'
        ORDER BY outbox_local_id""")
    fun observeTaskIntents(user: Long): Flow<List<TaskIntentState>>
    @Query("""SELECT entity_type, entity_sync_id, status, last_error FROM outbox
        WHERE user_local_id = :user AND entity_type = 'task_action' AND entity_sync_id = :task AND status != 'SYNCED'
        ORDER BY outbox_local_id""")
    fun observeTaskChain(user: Long, task: UUID): Flow<List<TaskIntentState>>

    @Query("""UPDATE outbox SET payload_json = :payload, base_version = :version
        WHERE mutation_id = :id AND user_local_id = :user AND status = 'PENDING' AND attempt_count = 0""")
    suspend fun prepareTaskIntent(id: UUID, user: Long, version: Long?, payload: String): Int

    @Query("""SELECT * FROM outbox WHERE user_local_id = :userLocalId AND entity_type = :entityType
        AND status = 'PENDING' AND attempt_count = 0 ORDER BY outbox_local_id""")
    suspend fun getNeverAttemptedIntents(userLocalId: Long, entityType: String): List<OutboxEntity>

    @Query("""UPDATE outbox SET payload_json = :payload WHERE mutation_id = :mutationId
        AND user_local_id = :userLocalId AND status = 'PENDING' AND attempt_count = 0""")
    suspend fun rewriteNeverAttemptedPayload(mutationId: UUID, userLocalId: Long, payload: String): Int

    @Query("""DELETE FROM outbox WHERE user_local_id = :userLocalId AND entity_type = :entityType
        AND entity_sync_id = :syncId AND status = 'PENDING' AND attempt_count = 0""")
    suspend fun deleteNeverAttemptedIntents(userLocalId: Long, entityType: String, syncId: UUID): Int

    @Query("SELECT * FROM outbox WHERE mutation_id = :mutationId")
    suspend fun getMutation(mutationId: UUID): OutboxEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMutation(mutation: OutboxEntity): Long

    @Query(
        """
        SELECT candidate.* FROM outbox AS candidate
        WHERE candidate.user_local_id = :userLocalId
          AND candidate.status = :status
          AND candidate.entity_type IN (:entityTypes)
          AND NOT EXISTS (
          SELECT 1 FROM outbox as earlier
          WHERE earlier.user_local_id = candidate.user_local_id
            AND earlier.entity_type = candidate.entity_type
            AND earlier.entity_sync_id = candidate.entity_sync_id
            AND (
              (earlier.attempt_count > 0 AND earlier.outbox_local_id < candidate.outbox_local_id)
              OR (candidate.attempt_count = 0 AND earlier.dependency_priority < candidate.dependency_priority)
              OR (earlier.dependency_priority = candidate.dependency_priority AND earlier.outbox_local_id < candidate.outbox_local_id)
            )
            AND earlier.status IN (:blockingStatuses)
          )
        ORDER BY candidate.dependency_priority, candidate.created_at, candidate.outbox_local_id
        LIMIT :limit
        """
    )
    suspend fun getMutations(
        userLocalId: Long,
        status: String,
        entityTypes: List<String>,
        blockingStatuses: List<String>,
        limit: Int
    ): List<OutboxEntity>

    @Query("""
        SELECT * FROM outbox
        WHERE user_local_id = :userLocalId
            AND status = :inSyncStatus
            AND mutation_id IN (:mutationIds)
    """)
    suspend fun getClaimedMutations(
        userLocalId: Long,
        inSyncStatus: String,
        mutationIds: List<UUID>
    ): List<OutboxEntity>

    @Query("""
        SELECT COUNT(*) FROM outbox
        WHERE user_local_id = :userLocalId
            AND entity_type = :entityType
            AND entity_sync_id = :entitySyncId
            AND status IN (:statuses)
    """)
    suspend fun countMutationsForEntity(
        userLocalId: Long,
        entityType: String,
        entitySyncId: UUID,
        statuses: List<String>
    ): Int

    @Query(
        """
        UPDATE outbox
        SET status = :inSyncStatus,
            attempt_count = attempt_count + 1,
            last_attempt_at = :attemptedAt,
            last_error = NULL,
            conflict_server_version = NULL,
            conflict_server_data_json = NULL
        WHERE mutation_id IN (:mutationIds)
          AND status = :pendingStatus
        """
    )
    suspend fun markInSync(
        mutationIds: List<UUID>,
        attemptedAt: Instant,
        pendingStatus: String,
        inSyncStatus: String
    ): Int

    @Query(
        """
        UPDATE outbox
        SET status = :pendingStatus,
            last_error = :error,
            conflict_server_version = NULL,
            conflict_server_data_json = NULL
        WHERE mutation_id IN (:mutationIds)
          AND status = :inSyncStatus
        """
    )
    suspend fun returnToPending(
        mutationIds: List<UUID>,
        error: String?,
        pendingStatus: String,
        inSyncStatus: String
    ): Int

    @Query("""
        UPDATE outbox
        SET base_version = :serverVersion
        WHERE user_local_id = :userLocalId
            AND entity_type = :entityType
            AND entity_sync_id = :entitySyncId
            AND status = :pendingStatus
            AND attempt_count = 0
            AND operation IN (:operations)
    """)
    suspend fun rebasePendingMutations(
        serverVersion: Long,
        userLocalId: Long,
        entityType: String,
        entitySyncId: UUID,
        pendingStatus: String,
        operations: List<String>
    ): Int

    @Query(
        """
        UPDATE outbox
        SET status = :status,
            last_error = :error,
            conflict_server_version = :conflictServerVersion,
            conflict_server_data_json = :conflictServerDataJson
        WHERE mutation_id = :mutationId
            AND status = :expectedStatus
        """
    )
    suspend fun setMutationOutcome(
        mutationId: UUID,
        expectedStatus: String,
        status: String,
        error: String?,
        conflictServerVersion: Long?,
        conflictServerDataJson: String?
    ): Int

    @Query(
        """
        DELETE FROM outbox
        WHERE mutation_id IN (:mutationIds)
        """
    )
    suspend fun deleteMutations(
        mutationIds: List<UUID>
    ): Int

    @Query(
        """
        UPDATE outbox
        SET status = :pendingStatus,
            last_error = :recoveryMessage
        WHERE user_local_id = :userLocalId
          AND status = :inSyncStatus
          AND entity_type IN (:entityTypes)
          AND last_attempt_at < :staleBefore
        """
    )
    suspend fun recoverStaleMutations(
        userLocalId: Long,
        staleBefore: Instant,
        pendingStatus: String,
        inSyncStatus: String,
        entityTypes: List<String>,
        recoveryMessage: String
    ): Int
}
