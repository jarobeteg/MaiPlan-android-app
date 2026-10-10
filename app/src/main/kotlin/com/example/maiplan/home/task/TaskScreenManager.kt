package com.example.maiplan.home.task

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.key
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.maiplan.home.task.navigation.TaskNavHost
import com.example.maiplan.R
import com.example.maiplan.database.MaiPlanDatabase
import com.example.maiplan.home.task.screens.TaskLoadState
import com.example.maiplan.repository.task.TaskRepository
import com.example.maiplan.repository.task.TaskConflictService
import com.example.maiplan.utils.common.UserSession
import com.example.maiplan.viewmodel.task.TaskViewModel
import com.example.maiplan.viewmodel.task.taskViewModelFactory

@Composable
fun TaskScreenManager(rootNavController: NavHostController, notificationTaskId: Long? = null, onNotificationHandled: () -> Unit = {}) {
    val context = LocalContext.current.applicationContext
    val userId = UserSession.userLocalId
    if (userId == null) { TaskLoadState(false, stringResource(R.string.task_sign_in)); return }
    key(userId) {
        val localNavController = rememberNavController()
        val repository = remember(context, userId) { TaskRepository.create(context) }
        val factory = remember(context, userId, repository) {
            taskViewModelFactory { savedState ->
                TaskViewModel(userId, repository, MaiPlanDatabase.getDatabase(context).categoryDAO().observeActiveCategories(userId),
                    TaskConflictService.create(context), savedState)
            }
        }
        val model: TaskViewModel = viewModel(key = "tasks-$userId", factory = factory)
        TaskNavHost(rootNavController, localNavController, userId, repository, model, notificationTaskId, onNotificationHandled)
    }
}
