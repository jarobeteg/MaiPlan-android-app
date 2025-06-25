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

/**
 * [Composable] function that sets up the `Event` screen management logic, including navigation
 * and `ViewModel` initialization for related features.
 *
 * ## Responsibilities:
 * - Creates a local [NavHostController] for handling event-specific navigation within the tab.
 * - Lazily initializes the following `ViewModel`s using manual dependency injection and
 *   [GenericViewModelFactory]:
 *   - [EventViewModel] for event `CRUD` operations
 *   - [CategoryViewModel] for managing event categories
 *   - [ReminderViewModel] for handling event-related reminders
 * - Provides the initialized `ViewModel`s and navigation controllers to [EventNavHost].
 *
 * @param rootNavController The root [NavHostController] for top-level app navigation.
 *
 * @see EventViewModel
 * @see CategoryViewModel
 * @see ReminderViewModel
 * @see EventNavHost
 * @see EventRemoteDataSource
 * @see CategoryRemoteDataSource
 * @see ReminderRemoteDataSource
 * @see EventRepository
 * @see CategoryRepository
 * @see ReminderRepository
 * @see GenericViewModelFactory
 * @see RetrofitClient
 */
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
