package com.example.maiplan.network.sync

interface Syncable {
    suspend fun sync()
}

data class SyncRequest<T>(
    val userId: Int,
    val changes: List<T>
)

data class SyncResponse<T>(
    val userId: Int,
    val acknowledged: List<T>,   // records that were acknowledged during sync
    val rejected: List<T>       // records that were rejected during sync
)

// figure out a more advanced method, to do batch processing to maybe