package com.example.maiplan.home.event

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.maiplan.home.event.navigation.EventNavHost
import com.example.maiplan.network.RetrofitClient
import com.example.maiplan.repository.category.CategoryRemoteDataSource
import com.example.maiplan.repository.category.CategoryRepository
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

    val eventViewModel = remember {
        val eventApi = RetrofitClient.eventApi
        val eventRemoteDataSource = EventRemoteDataSource(eventApi)
        val eventRepository = EventRepository(eventRemoteDataSource)
        val factory = GenericViewModelFactory { EventViewModel(eventRepository) }
        ViewModelProvider(context as ViewModelStoreOwner, factory)[EventViewModel::class.java]
    }

    val categoryViewModel = remember {
        val categoryApi = RetrofitClient.categoryApi
        val categoryRemoteDataSource = CategoryRemoteDataSource(categoryApi)
        val categoryRepository = CategoryRepository(categoryRemoteDataSource)
        val factory = GenericViewModelFactory { CategoryViewModel(categoryRepository) }
        ViewModelProvider(context as ViewModelStoreOwner, factory)[CategoryViewModel::class.java]
    }

    val reminderViewModel = remember {
        val reminderApi = RetrofitClient.reminderApi
        val reminderReminderRemoteDataSource = ReminderRemoteDataSource(reminderApi)
        val reminderRepository = ReminderRepository(reminderReminderRemoteDataSource)
        val factory = GenericViewModelFactory { ReminderViewModel(reminderRepository) }
        ViewModelProvider(context as ViewModelStoreOwner, factory)[ReminderViewModel::class.java]
    }

    EventNavHost(rootNavController, localNavController, eventViewModel, categoryViewModel, reminderViewModel)
}
