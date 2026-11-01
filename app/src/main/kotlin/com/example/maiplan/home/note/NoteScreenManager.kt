package com.example.maiplan.home.note

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.key
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.example.maiplan.home.navigation.HomeNavRoutes
import com.example.maiplan.network.sync.SyncScheduler
import com.example.maiplan.repository.category.CategoryLocalDataSource
import com.example.maiplan.repository.note.NoteLocalDataSource
import com.example.maiplan.repository.note.NoteRepository
import com.example.maiplan.repository.reminder.ReminderLocalDataSource
import com.example.maiplan.utils.common.UserSession
import com.example.maiplan.viewmodel.GenericViewModelFactory
import com.example.maiplan.viewmodel.note.NoteViewModel

@Composable
fun NoteScreenManager(rootNavController: NavHostController, entry: NavBackStackEntry,
    content: @Composable (Long, NoteViewModel) -> Unit) {
    val context = LocalContext.current.applicationContext
    val userId = UserSession.userLocalId ?: return
    key(userId) {
        val owner = remember(rootNavController, entry) { rootNavController.getBackStackEntry(HomeNavRoutes.Notes.route) }
        val factory = remember(context, userId) {
            val repository = NoteRepository(context, NoteLocalDataSource(context),
                CategoryLocalDataSource(context), ReminderLocalDataSource(context),
                requestSync = { SyncScheduler.runOneTimeSync(context) })
            GenericViewModelFactory { NoteViewModel(repository) }
        }
        val model: NoteViewModel = viewModel(viewModelStoreOwner = owner, key = "notes-$userId", factory = factory)
        LaunchedEffect(userId) { model.loadCategories(userId) }
        content(userId, model)
    }
}
