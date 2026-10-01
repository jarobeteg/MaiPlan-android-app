package com.example.maiplan.home.event

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.maiplan.home.event.navigation.EventNavHost
import com.example.maiplan.network.sync.SyncScheduler
import com.example.maiplan.repository.category.CategoryLocalDataSource
import com.example.maiplan.repository.category.CategoryRepository
import com.example.maiplan.repository.event.EventLocalDataSource
import com.example.maiplan.repository.event.EventRepository
import com.example.maiplan.repository.reminder.ReminderLocalDataSource
import com.example.maiplan.utils.common.UserSession
import com.example.maiplan.viewmodel.event.EventViewModel
import com.example.maiplan.viewmodel.GenericViewModelFactory
import com.example.maiplan.viewmodel.category.CategoryViewModel

@Composable
fun EventScreenManager(rootNavController: NavHostController) {
    val userLocalId = UserSession.userLocalId ?: return
    val localNavController = rememberNavController()
    val context = LocalContext.current

    val eventViewModel = remember {
        val eventLocal = EventLocalDataSource(context)
        val localCategory = CategoryLocalDataSource(context)
        val localReminder = ReminderLocalDataSource(context)
        val eventRepo = EventRepository(context.applicationContext, eventLocal, localCategory, localReminder) {
            SyncScheduler.runOneTimeSync(context.applicationContext)
        }
        val factory = GenericViewModelFactory { EventViewModel(eventRepo) }
        ViewModelProvider(context as ViewModelStoreOwner, factory)[EventViewModel::class.java]
    }

    val categoryViewModel = remember {
        val categoryLocal = CategoryLocalDataSource(context)
        val categoryRepo = CategoryRepository(categoryLocal)
        val factory = GenericViewModelFactory { CategoryViewModel(categoryRepo, userLocalId) }
        ViewModelProvider(context as ViewModelStoreOwner, factory)[CategoryViewModel::class.java]
    }

    EventNavHost(rootNavController, localNavController, eventViewModel, categoryViewModel)
}
