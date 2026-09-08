package com.example.maiplan.database.dao

import com.example.maiplan.database.entities.SyncStateEntity
import androidx.room.Upsert
import androidx.room.Query
import androidx.room.Dao
import java.util.UUID

@Dao
interface SyncStateDAO {
    @Query("SELECT * FROM sync_state WHERE user_sync_id = :userSyncId")
    suspend fun getSyncState(userSyncId: UUID): SyncStateEntity?

    @Upsert
    suspend fun upsertSyncState(syncState: SyncStateEntity)

    @Query("DELETE FROM sync_state WHERE user_sync_id = :userSyncId")
    suspend fun deleteSyncState(userSyncId: UUID)
}