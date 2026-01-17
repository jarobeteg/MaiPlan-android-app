package com.example.maiplan

import android.app.Application
import com.example.maiplan.network.sync.SyncScheduler

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        SyncScheduler.schedulePeriodicSync(this)
    }
}