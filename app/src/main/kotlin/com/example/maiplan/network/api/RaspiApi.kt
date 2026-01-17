package com.example.maiplan.network.api

import retrofit2.Response
import retrofit2.http.GET

data class RaspiResponse(
    val status: String,
    val uptime: String,
    val hostname: String
)

interface RaspiApi {
    @GET("raspi/health")
    suspend fun checkHealth(): Response<RaspiResponse>
}