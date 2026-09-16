package com.example.maiplan.network.api

import com.example.maiplan.network.sync.TideSyncRequest
import com.example.maiplan.network.sync.TideSyncResponse
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.Response

interface TideApi {
    @POST("sync")
    suspend fun exchange(@Body request: TideSyncRequest): Response<TideSyncResponse>
}