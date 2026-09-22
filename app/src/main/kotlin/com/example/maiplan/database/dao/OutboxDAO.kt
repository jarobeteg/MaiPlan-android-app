package com.example.maiplan.database.dao

import com.example.maiplan.database.entities.OutboxEntity
import androidx.room.OnConflictStrategy
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Dao
import java.time.Instant
import java.util.UUID

@Dao
interface OutboxDAO {
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
            AND earlier.outbox_local_id < candidate.outbox_local_id
            AND earlier.status IN (:blockingStatuses)
          )
        ORDER BY candidate.created_at, candidate.outbox_local_id
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
