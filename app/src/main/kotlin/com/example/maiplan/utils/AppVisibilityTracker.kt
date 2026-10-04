package com.example.maiplan.utils

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.example.maiplan.utils.notifications.enqueueEventAlarmRecovery

object AppVisibilityTracker : Application.ActivityLifecycleCallbacks {
    @Volatile
    private var startedActivityCount = 0

    val isAppInForeground: Boolean
        get() = startedActivityCount > 0

    override fun onActivityStarted(activity: Activity) {
        if (startedActivityCount == 0) enqueueEventAlarmRecovery(activity.applicationContext)
        startedActivityCount += 1
    }

    override fun onActivityStopped(activity: Activity) {
        startedActivityCount = (startedActivityCount - 1).coerceAtLeast(0)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityResumed(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit
}
