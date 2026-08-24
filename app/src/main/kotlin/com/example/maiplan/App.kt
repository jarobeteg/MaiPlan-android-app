package com.example.maiplan

import android.app.Application
import com.example.maiplan.network.RetrofitClient
import com.example.maiplan.network.sync.SyncScheduler
import com.example.maiplan.theme.AppThemeManager
import com.example.maiplan.utils.notifications.NotificationHelper

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        AppThemeManager.initialize(this)
        NotificationHelper.createNotificationChannel(this)
        RetrofitClient.init()
        SyncScheduler.schedulePeriodicSync(this)
    }
}
