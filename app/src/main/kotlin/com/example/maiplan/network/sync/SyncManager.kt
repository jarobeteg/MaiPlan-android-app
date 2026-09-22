package com.example.maiplan.network.sync

import java.util.UUID

class SyncManager (
    private val tideSynchronizer: TideSynchronizer
) {

    suspend fun syncAll(userLocalId: Long, userSyncId: UUID): Boolean {
        val result = tideSynchronizer.sync(
            userLocalId = userLocalId,
            userSyncId = userSyncId
        )
        return result.hasMoreWork
    }
}
