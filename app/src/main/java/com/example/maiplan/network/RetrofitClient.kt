package com.example.maiplan.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Singleton object responsible for providing a configured instance of [Retrofit]
 * and various API service instances.
 *
 * Lazy-initialization means that the object or resource is created only whe it's first needed,
 * rather than at the start.
 */
object RetrofitClient {
    private const val BASE_URL = "http://100.70.156.115:8000/"

    /**
     * Lazy-initialized Retrofit instance configured with the base URL and Gson converter.
     */
    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Lazy-initialized instance of [AuthApi] for authentication-related requests.
     */
    val authApi: AuthApi by lazy { retrofit.create(AuthApi::class.java) }

    /**
     * Lazy-initialized instance of [CategoryApi] for category-related requests.
     */
    val categoryApi: CategoryApi by lazy { retrofit.create(CategoryApi::class.java) }

    /**
     * Lazy-initialized instance of [EventApi] for event-related requests.
     */
    val eventApi: EventApi by lazy { retrofit.create(EventApi::class.java) }

    /**
     * Lazy-initialized instance of [ReminderApi] for reminder-related requests.
     */
    val reminderApi: ReminderApi by lazy { retrofit.create(ReminderApi::class.java) }
}