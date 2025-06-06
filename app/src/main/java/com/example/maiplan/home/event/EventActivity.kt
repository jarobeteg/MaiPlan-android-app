package com.example.maiplan.home.event

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.maiplan.home.event.navigation.EventNavHost
import com.example.maiplan.network.RetrofitClient
import com.example.maiplan.repository.EventRepository
import com.example.maiplan.theme.AppTheme
import com.example.maiplan.viewmodel.EventViewModel
import com.example.maiplan.viewmodel.GenericViewModelFactory


/**
 * This activity is responsible for managing and displaying Event screens.
 *
 * The activity sets up the UI content using Jetpack Compose and applies the app a theme.
 * Sets up the navigation between Event screens and with other screens on the Home component.
 */
class EventActivity : AppCompatActivity() {
    /** ViewModel instance to handle Event related logic. */
    private lateinit var eventViewModel: EventViewModel

    /**
     * Lifecycle method onCreate is called when the activity created.
     *
     * - Initializes the ViewModel using a repository and generic factory.
     * -- The repository needs to be initialized by the retrofit client's event API
     * -- The repository holds the CRUD operations for the event API.
     * -- The factory is a tool to pass dependency for a ViewModel, in our case it's the repository.
     * - Sets up the Compose UI.
     *
     * @param savedInstanceState Saved state of this activity if previously existed.
     *
     * @see RetrofitClient
     * @see EventRepository
     * @see EventViewModel
     * @see GenericViewModelFactory
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val eventApi = RetrofitClient.eventApi
        val eventRepository = EventRepository(eventApi)
        val eventFactory = GenericViewModelFactory{ EventViewModel(eventRepository) }

        eventViewModel = ViewModelProvider(this, eventFactory)[EventViewModel::class.java]

        setupComposeUI()
    }

    /**
     * Sets up the UI content using Jetpack Compose and applies the app a theme.
     *
     * Sets up the navigation between Event screens.
     *
     * @see AppTheme
     * @see com.example.maiplan.home.event.navigation.EventNavHost
     */
    private fun setupComposeUI() {
        setContent {
            AppTheme {
                EventNavHost(eventViewModel)
            }
        }
    }
}