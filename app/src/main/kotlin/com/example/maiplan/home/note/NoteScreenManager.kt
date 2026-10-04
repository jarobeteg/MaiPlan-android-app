package com.example.maiplan.home.note

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.maiplan.home.note.navigation.NoteNavHost
import com.example.maiplan.network.sync.SyncScheduler
import com.example.maiplan.repository.category.CategoryLocalDataSource
import com.example.maiplan.repository.note.NoteLocalDataSource
import com.example.maiplan.repository.note.NoteRepository
import com.example.maiplan.repository.reminder.ReminderLocalDataSource
import com.example.maiplan.utils.common.UserSession
import com.example.maiplan.viewmodel.GenericViewModelFactory
import com.example.maiplan.viewmodel.note.NoteViewModel

@Composable
fun NoteScreenManager(rootNavController: NavHostController) {
    val context = LocalContext.current
    val localNavController = rememberNavController()
    val noteViewModel = remember(context) {
        val repository = NoteRepository(
            context = context.applicationContext,
            local = NoteLocalDataSource(context),
            localCategory = CategoryLocalDataSource(context),
            localReminder = ReminderLocalDataSource(context),
            requestSync = { SyncScheduler.runOneTimeSync(context.applicationContext) },
        )
        val factory = GenericViewModelFactory { NoteViewModel(repository) }
        ViewModelProvider(context as ViewModelStoreOwner, factory)[NoteViewModel::class.java]
    }

    LaunchedEffect(Unit) {
        UserSession.userLocalId?.let {
            noteViewModel.loadCategories(it)
            noteViewModel.loadNotes(it)
        }
    }

    NoteNavHost(rootNavController, localNavController, noteViewModel)
}
