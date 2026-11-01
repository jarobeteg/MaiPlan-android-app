package com.example.maiplan.home.event

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.key
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.example.maiplan.home.navigation.HomeNavRoutes
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
fun EventScreenManager(rootNavController: NavHostController, entry: NavBackStackEntry,
    content: @Composable (Long, EventViewModel, CategoryViewModel) -> Unit) {
    val userLocalId = UserSession.userLocalId ?: return
    val context = LocalContext.current.applicationContext

    key(userLocalId) {
        val owner = remember(rootNavController, entry) { rootNavController.getBackStackEntry(HomeNavRoutes.Events.route) }
        val eventFactory = remember(context, userLocalId) {
            val eventRepo = EventRepository(context, EventLocalDataSource(context),
                CategoryLocalDataSource(context), ReminderLocalDataSource(context)) {
                SyncScheduler.runOneTimeSync(context)
            }
            GenericViewModelFactory { EventViewModel(eventRepo) }
        }
        val categoryFactory = remember(context, userLocalId) {
            val categoryRepo = CategoryRepository(CategoryLocalDataSource(context))
            GenericViewModelFactory { CategoryViewModel(categoryRepo, userLocalId) }
        }
        val eventViewModel: EventViewModel = viewModel(viewModelStoreOwner = owner,
            key = "events-$userLocalId", factory = eventFactory)
        val categoryViewModel: CategoryViewModel = viewModel(viewModelStoreOwner = owner,
            key = "event-categories-$userLocalId", factory = categoryFactory)
        content(userLocalId, eventViewModel, categoryViewModel)
    }
}
