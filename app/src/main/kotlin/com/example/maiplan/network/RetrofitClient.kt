package com.example.maiplan.network

import com.example.maiplan.network.api.AuthApi
import com.example.maiplan.network.api.CategoryApi
import com.example.maiplan.network.api.EventApi
import com.example.maiplan.network.api.RaspiApi
import com.example.maiplan.network.api.ReminderApi
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "http://100.70.156.115:8001/" // dev server with development port

    private val normalClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES))
        .build()

    private val fastClient = OkHttpClient.Builder()
        .connectTimeout(1, TimeUnit.SECONDS)
        .readTimeout(1, TimeUnit.SECONDS)
        .writeTimeout(1, TimeUnit.SECONDS)
        .callTimeout(2, TimeUnit.SECONDS)
        .retryOnConnectionFailure(false)
        .connectionPool(ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
        .build()

    val normalRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(normalClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val fastRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(fastClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authApi: AuthApi by lazy { normalRetrofit.create(AuthApi::class.java) }
    val categoryApi: CategoryApi by lazy { normalRetrofit.create(CategoryApi::class.java) }
    val eventApi: EventApi by lazy { normalRetrofit.create(EventApi::class.java) }
    val reminderApi: ReminderApi by lazy { normalRetrofit.create(ReminderApi::class.java) }
    val raspiApi: RaspiApi by lazy { fastRetrofit.create(RaspiApi::class.java) }
}