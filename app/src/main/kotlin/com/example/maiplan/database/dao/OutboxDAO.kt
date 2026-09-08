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
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMutation(mutation: OutboxEntity): Long

    @Query(
        """
        SELECT * FROM outbox
        WHERE user_sync_id = :userSyncId
          AND status = :status
        ORDER BY created_at, outbox_local_id
        LIMIT :limit
        """
    )
    suspend fun getMutations(
        userSyncId: UUID,
        status: String,
        limit: Int
    ): List<OutboxEntity>

    @Query(
        """
        UPDATE outbox
        SET status = :inSyncStatus,
            attempt_count = attempt_count + 1,
            last_attempt_at = :attemptedAt,
            last_error = NULL
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
            last_error = :error
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

    @Query(
        """
        UPDATE outbox
        SET status = :status,
            last_error = :error
        WHERE mutation_id = :mutationId
        """
    )
    suspend fun setMutationOutcome(
        mutationId: UUID,
        status: String,
        error: String?
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
        WHERE user_sync_id = :userSyncId
          AND status = :inSyncStatus
          AND last_attempt_at < :staleBefore
        """
    )
    suspend fun recoverStaleMutations(
        userSyncId: UUID,
        staleBefore: Instant,
        pendingStatus: String,
        inSyncStatus: String,
        recoveryMessage: String
    ): Int
}