package com.example.maiplan.home.event

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.maiplan.home.event.navigation.EventNavHost
import com.example.maiplan.network.NetworkChecker
import com.example.maiplan.network.RetrofitClient
import com.example.maiplan.repository.category.CategoryLocalDataSource
import com.example.maiplan.repository.category.CategoryRemoteDataSource
import com.example.maiplan.repository.category.CategoryRepository
import com.example.maiplan.repository.event.EventLocalDataSource
import com.example.maiplan.repository.event.EventRemoteDataSource
import com.example.maiplan.repository.event.EventRepository
import com.example.maiplan.repository.reminder.ReminderRemoteDataSource
import com.example.maiplan.repository.reminder.ReminderRepository
import com.example.maiplan.viewmodel.event.EventViewModel
import com.example.maiplan.viewmodel.GenericViewModelFactory
import com.example.maiplan.viewmodel.category.CategoryViewModel
import com.example.maiplan.viewmodel.reminder.ReminderViewModel

@Composable
fun EventScreenManager(rootNavController: NavHostController) {
    val localNavController = rememberNavController()
    val context = LocalContext.current
    val networkChecker = NetworkChecker(context)

    val eventViewModel = remember {
        val eventRemote = EventRemoteDataSource(RetrofitClient.eventApi)
        val eventLocal = EventLocalDataSource(context)
        val eventRepo = EventRepository(eventRemote, eventLocal)
        val factory = GenericViewModelFactory { EventViewModel(eventRepo) }
        ViewModelProvider(context as ViewModelStoreOwner, factory)[EventViewModel::class.java]
    }

    val categoryViewModel = remember {
        val categoryRemote = CategoryRemoteDataSource(RetrofitClient.categoryApi)
        val categoryLocal = CategoryLocalDataSource(context)
        val categoryRepo = CategoryRepository(categoryRemote, categoryLocal)
        val factory = GenericViewModelFactory { CategoryViewModel(categoryRepo, networkChecker) }
        ViewModelProvider(context as ViewModelStoreOwner, factory)[CategoryViewModel::class.java]
    }

    val reminderViewModel = remember {
        val reminderRemote = ReminderRemoteDataSource(RetrofitClient.reminderApi)
        val reminderRepo = ReminderRepository(reminderRemote)
        val factory = GenericViewModelFactory { ReminderViewModel(reminderRepo) }
        ViewModelProvider(context as ViewModelStoreOwner, factory)[ReminderViewModel::class.java]
    }

    EventNavHost(rootNavController, localNavController, eventViewModel, categoryViewModel, reminderViewModel)
}
