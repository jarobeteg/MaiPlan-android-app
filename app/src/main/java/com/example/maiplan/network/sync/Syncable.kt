package com.example.maiplan.network.sync

interface Syncable {
    suspend fun sync()
}

data class SyncRequest<T>(
    val changes: List<T>
)

data class SyncResponse<T>(
    val serverChanges: List<T>, // records the app should apply to it's Room database
    val acknowledged: List<T>   // records the server acknowledged, these record should have a server_id if they didn't had one before
)

// figure out a more advanced method, to do batch processing to maybe