package com.example.maiplan.network.sync

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import java.util.UUID

interface Syncable {
    suspend fun sync()
}

data class TideSyncRequest(
    @SerializedName("tide_protocol_version")
    val tideProtocolVersion: Int = TideProtocol.VERSION,

    @SerializedName("request_id")
    val requestId: UUID,

    @SerializedName("device_id")
    val deviceId: UUID,

    val cursor: String?,

    @SerializedName("data_limit")
    val dataLimit: Int = TideProtocol.DEFAULT_DATA_LIMIT,

    val mutations: List<TideMutation>
)

data class TideSyncResponse(
    @SerializedName("tide_protocol_version")
    val tideProtocolVersion: Int,

    @SerializedName("request_id")
    val requestId: UUID,

    @SerializedName("response_id")
    val responseId: UUID,

    val acknowledged: List<TideAcknowledgement>,
    val rejected: List<TideRejection>,
    val conflicts: List<TideConflict>,
    val changes: List<TideChange>,

    @SerializedName("next_cursor")
    val nextCursor: String,

    @SerializedName("more_changes")
    val moreChanges: Boolean
)

data class TideMutation(
    @SerializedName("mutation_id")
    val mutationId: UUID,

    @SerializedName("entity_type")
    val entityType: String,

    @SerializedName("entity_sync_id")
    val entitySyncId: UUID,

    val operation: String,

    @SerializedName("base_version")
    val baseVersion: Long?,

    val data: JsonObject?
)

data class TideAcknowledgement(
    @SerializedName("mutation_id")
    val mutationId: UUID,

    @SerializedName("entity_type")
    val entityType: String,

    @SerializedName("entity_sync_id")
    val entitySyncId: UUID,

    @SerializedName("server_version")
    val serverVersion: Long
)

data class TideRejection(
    @SerializedName("mutation_id")
    val mutationId: UUID,

    @SerializedName("entity_type")
    val entityType: String,

    @SerializedName("entity_sync_id")
    val entitySyncId: UUID,

    @SerializedName("error_code")
    val errorCode: String,

    val message: String?
)

data class TideConflict(
    @SerializedName("mutation_id")
    val mutationId: UUID,

    @SerializedName("entity_type")
    val entityType: String,

    @SerializedName("entity_sync_id")
    val entitySyncId: UUID,

    @SerializedName("server_version")
    val serverVersion: Long,

    @SerializedName("server_data")
    val serverData: JsonObject?
)

data class TideChange(
    val sequence: String,

    @SerializedName("entity_type")
    val entityType: String,

    @SerializedName("entity_sync_id")
    val entitySyncId: UUID,

    val operation: String,

    @SerializedName("server_version")
    val serverVersion: Long,

    val data: JsonObject?
)

data class SyncRequest<T>(
    @SerializedName("user_local_id") val userLocalId: Long,
    val changes: List<T>
)

data class SyncResponse<T>(
    @SerializedName("user_local_id") val userLocalId: Long,
    val acknowledged: List<T>,   // records that were acknowledged during sync
    val rejected: List<T>       // records that were rejected during sync
)

// figure out a more advanced method, to do batch processing to maybe