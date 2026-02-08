package com.example.maiplan

import android.app.Application
import com.example.maiplan.network.RetrofitClient
import com.example.maiplan.network.sync.SyncScheduler

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        RetrofitClient.init(this)
        SyncScheduler.schedulePeriodicSync(this)
    }
}