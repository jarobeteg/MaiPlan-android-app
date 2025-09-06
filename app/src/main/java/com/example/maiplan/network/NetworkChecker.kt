package com.example.maiplan.network

import com.example.maiplan.network.api.RaspiApi
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

class NetworkChecker (private val context: Context) {

    fun isOnline(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }

    suspend fun canReachServer(): Boolean {
        return try {
            val response = RetrofitClient.retrofit.create(RaspiApi::class.java).checkHealth()
            if (response.isSuccessful) {
                println("Server response: ${response.body()}")
                true
            } else {
                println("Server error: ${response.code()}")
                false
            }
        } catch (_: Exception) {
            false
        }
    }
}