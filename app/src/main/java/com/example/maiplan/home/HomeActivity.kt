package com.example.maiplan.home

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Constraints
import com.example.maiplan.home.navigation.HomeNavHost
import com.example.maiplan.network.sync.SyncWorker
import com.example.maiplan.theme.AppTheme
import com.example.maiplan.utils.BaseActivity
import java.util.concurrent.TimeUnit

class HomeActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            15, TimeUnit.MINUTES
        ).setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        ).build()

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                "SyncWork",
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )

        setContent {
            AppTheme {
                val rootNavController: NavHostController = rememberNavController()
                HomeNavHost(rootNavController)
            }
        }
    }
}