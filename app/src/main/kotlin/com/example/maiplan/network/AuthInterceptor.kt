package com.example.maiplan.network

import com.example.maiplan.utils.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor (private val sessionManager: SessionManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val accessToken = sessionManager.getAccessToken()

        if (accessToken.isNullOrBlank() || originalRequest.header("Authorization") != null) {
            return chain.proceed(originalRequest)
        }

        val authenticatedRequest = originalRequest
            .newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()

        return chain.proceed(authenticatedRequest)
    }
}