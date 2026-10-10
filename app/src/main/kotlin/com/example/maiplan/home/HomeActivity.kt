package com.example.maiplan.home

import android.os.Bundle
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.*
import com.example.maiplan.database.MaiPlanDatabase
import com.example.maiplan.main.MainActivity
import com.example.maiplan.utils.notifications.TaskReminderDestination
import com.example.maiplan.utils.notifications.resolveTaskReminderDestination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.maiplan.home.navigation.HomeNavHost
import com.example.maiplan.network.sync.SyncScheduler
import com.example.maiplan.theme.AppTheme
import com.example.maiplan.utils.BaseActivity

class HomeActivity : BaseActivity() {
    private var destination by mutableStateOf<TaskReminderDestination?>(null)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        destination = TaskReminderDestination.from(savedInstanceState?.getString("taskReminderDestination")?.let(Uri::parse) ?: intent.data)
        if (destination != null && !sessionManager.hasSession()) {
            startActivity(Intent(this, MainActivity::class.java).apply { data = destination?.uri() })
            finish()
            return
        }

        setAppContent {
            AppTheme {
                val rootNavController: NavHostController = rememberNavController()
                var taskId by remember { mutableStateOf<Long?>(null) }
                LaunchedEffect(destination) {
                    taskId = destination?.let { target ->
                        withContext(Dispatchers.IO) {
                            resolveTaskReminderDestination(MaiPlanDatabase.getDatabase(applicationContext), target,
                                sessionManager.getActiveUserSyncId().takeIf { sessionManager.hasSession() })
                        } ?: -1L
                    }
                }
                HomeNavHost(rootNavController, taskId) { taskId = null; destination = null; intent.data = null }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        destination = TaskReminderDestination.from(intent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        destination?.let { outState.putString("taskReminderDestination", it.uri().toString()) }
        super.onSaveInstanceState(outState)
    }

    override fun onStart() {
        super.onStart()
        SyncScheduler.runOneTimeSync(applicationContext)
    }
}
