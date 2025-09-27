package com.example.maiplan.network.sync

interface Syncable {
    suspend fun sync()
}

data class SyncRequest<T>(
    val email: String,
    val changes: List<T>
)

data class SyncResponse<T>(
    val email: String,
    val acknowledged: List<T>,   // records that were acknowledged during sync
    val rejected: List<T>       // records that were rejected during sync
)

// figure out a more advanced method, to do batch processing to maybe