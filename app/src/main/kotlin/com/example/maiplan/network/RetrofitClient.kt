package com.example.maiplan.network

import android.content.Context
import com.example.maiplan.BuildConfig
import com.example.maiplan.network.api.AuthApi
import com.example.maiplan.network.api.CategoryApi
import com.example.maiplan.network.api.EventApi
import com.example.maiplan.network.api.NoteApi
import com.example.maiplan.network.api.RaspiApi
import com.example.maiplan.network.api.ReminderApi
import com.example.maiplan.network.api.TokenRefreshApi
import com.example.maiplan.utils.DeviceIdentityStore
import com.example.maiplan.utils.SessionManager
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private lateinit var sessionManager: SessionManager
    private lateinit var deviceIdentityStore: DeviceIdentityStore
    private lateinit var BASE_URL: String

    fun init(context: Context) {
        sessionManager = SessionManager(context.applicationContext)
        deviceIdentityStore = DeviceIdentityStore(context.applicationContext)
        BASE_URL = BuildConfig.API_BASE_URL
    }

    private fun standardClientBuilder(): OkHttpClient.Builder {
        return OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES))
    }

    private val publicClient: OkHttpClient by lazy {
        standardClientBuilder().build()
    }

    private val authenticatedClient: OkHttpClient by lazy {
        standardClientBuilder()
            .addInterceptor(AuthInterceptor(sessionManager))
            .authenticator(
                ForegroundTokenAuthenticator(
                    sessionManager = sessionManager,
                    refreshApi = tokenRefreshApi,
                    deviceId = deviceIdentityStore.getOrCreateDeviceId()
                )
            )
            .build()
    }

    private val fastClient = OkHttpClient.Builder()
        .connectTimeout(1, TimeUnit.SECONDS)
        .readTimeout(1, TimeUnit.SECONDS)
        .writeTimeout(1, TimeUnit.SECONDS)
        .callTimeout(2, TimeUnit.SECONDS)
        .retryOnConnectionFailure(false)
        .connectionPool(ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
        .build()

    private val publicRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(publicClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val normalRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(authenticatedClient)
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

    val publicAuthApi: AuthApi by lazy { publicRetrofit.create(AuthApi::class.java) }
    private val tokenRefreshApi: TokenRefreshApi by lazy {
        publicRetrofit.create(TokenRefreshApi::class.java)
    }
    val categoryApi: CategoryApi by lazy { normalRetrofit.create(CategoryApi::class.java) }
    val eventApi: EventApi by lazy { normalRetrofit.create(EventApi::class.java) }
    val noteApi: NoteApi by lazy { normalRetrofit.create(NoteApi::class.java) }
    val reminderApi: ReminderApi by lazy { normalRetrofit.create(ReminderApi::class.java) }
    val raspiApi: RaspiApi by lazy { fastRetrofit.create(RaspiApi::class.java) }
}
