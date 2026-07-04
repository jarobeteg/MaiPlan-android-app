package com.example.maiplan.repository.note

import com.example.maiplan.network.api.NoteApi
import com.example.maiplan.network.api.NoteSync
import com.example.maiplan.network.sync.SyncRequest
import com.example.maiplan.network.sync.SyncResponse
import retrofit2.Response

class NoteRemoteDataSource(private val noteApi: NoteApi) {
    suspend fun noteSync(request: SyncRequest<NoteSync>): Response<SyncResponse<NoteSync>> {
        return noteApi.noteSync(request)
    }
}
