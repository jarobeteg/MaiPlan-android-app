package com.example.maiplan.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

class NetworkChecker (context: Context) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private fun isOnline(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }

    private fun isPortOpen(): Boolean {
        val host = "100.70.156.115"
        val port = 8002
        val timeout = 500
        return try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(host, port), timeout)
                true
            }
        } catch (_: Exception) {
            false
        }
    }

    suspend fun canReachServer(): Boolean = withContext(Dispatchers.IO) {

        if (!isOnline()) return@withContext false

        if (!isPortOpen()) return@withContext false

        return@withContext try {
            val response = RetrofitClient.raspiApi.checkHealth()
            if (response.isSuccessful) {
                println("Server response: ${response.body()}")
                true
            } else {
                println("Server error: ${response.code()}")
                false
            }
        } catch (e: Exception) {
            println("Health check failed: ${e.message}")
            false
        }
    }
}