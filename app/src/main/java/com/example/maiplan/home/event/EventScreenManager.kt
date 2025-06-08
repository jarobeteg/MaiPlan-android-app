package com.example.maiplan.home.event

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.maiplan.home.event.navigation.EventNavHost
import com.example.maiplan.home.event.navigation.EventRoutes
import com.example.maiplan.home.event.screens.EventScreen
import com.example.maiplan.network.RetrofitClient
import com.example.maiplan.repository.event.EventRemoteDataSource
import com.example.maiplan.repository.event.EventRepository
import com.example.maiplan.viewmodel.event.EventViewModel
import com.example.maiplan.viewmodel.GenericViewModelFactory

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

    EventNavHost(rootNavController, localNavController, eventViewModel)
}
