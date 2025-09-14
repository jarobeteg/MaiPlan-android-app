package com.example.maiplan.network

import com.example.maiplan.network.api.AuthApi
import com.example.maiplan.network.api.CategoryApi
import com.example.maiplan.network.api.EventApi
import com.example.maiplan.network.api.ReminderApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://100.70.156.115:8000/"

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authApi: AuthApi by lazy { retrofit.create(AuthApi::class.java) }
    val categoryApi: CategoryApi by lazy { retrofit.create(CategoryApi::class.java) }
    val eventApi: EventApi by lazy { retrofit.create(EventApi::class.java) }
    val reminderApi: ReminderApi by lazy { retrofit.create(ReminderApi::class.java) }
}