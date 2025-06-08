package com.example.maiplan.main

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.maiplan.R
import com.example.maiplan.home.HomeActivity
import com.example.maiplan.main.navigation.AuthNavHost
import com.example.maiplan.main.screens.LoadingScreen
import com.example.maiplan.network.RetrofitClient
import com.example.maiplan.network.api.Token
import com.example.maiplan.repository.auth.AuthRepository
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.auth.AuthRemoteDataSource
import com.example.maiplan.theme.AppTheme
import com.example.maiplan.utils.SessionManager
import com.example.maiplan.utils.model.UserSession
import com.example.maiplan.viewmodel.auth.AuthViewModel
import com.example.maiplan.viewmodel.GenericViewModelFactory

/**
 * This activity is responsible for managing and displaying Authentication screens.
 *
 * This activity initializes the Auth ViewModel, checks if a user is already authenticated,
 * and either navigates to the main Event screen or shows the authentication flow.
 * It also observes authentication-related operations and provides user feedback.
 */
class MainActivity : AppCompatActivity() {

    /** Manager for handling session-related actions such as saving and retrieving tokens. */
    private lateinit var sessionManager: SessionManager

    /** ViewModel instance to handle Authentication related logic. */
    private lateinit var viewModel: AuthViewModel

    /**
     * Lifecycle method onCreate is called when the activity is created.
     *
     * - Initializes the ViewModel using a repository and generic factory.
     * -- The repository interacts with the authentication API via Retrofit.
     * -- The factory injects the repository dependency into the ViewModel.
     * - Checks if a user is already authenticated based on the stored token.
     * -- If a token exists, it refreshes the token and fetches the user's profile.
     * -- If no token exists, it displays the authentication screens.
     * - Observes authentication operation results and provides feedback to the user.
     *
     * @param savedInstanceState Saved state of this activity if previously existed.
     *
     * @see RetrofitClient
     * @see AuthRepository
     * @see UserSession
     * @see AuthViewModel
     * @see GenericViewModelFactory
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show a loading screen while checking authentication state
        setContent { AppTheme { LoadingScreen() } }

        // Initialize repository, ViewModel, and session manager
        initViewModel()
        sessionManager = SessionManager(this)

        // Check if there is an existing authentication token
        val token = sessionManager.getAuthToken()
        if (token != null) {
            // If a token exists, fetch user profile and refresh the token
            initUserSession(token)
        } else {
            // No token: show authentication screens
            setupComposeUI()
        }

        observeViewModel()
    }

    private fun initViewModel() {
        val authApi = RetrofitClient.authApi
        val authRemoteDataSource = AuthRemoteDataSource(authApi)
        val authRepository = AuthRepository(authRemoteDataSource)
        val factory = GenericViewModelFactory { AuthViewModel(authRepository) }

        viewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]
    }

    /**
     * Initializes the User Session singleton object, and refreshes the JWT token and saves it in the Shared Preferences.
     *
     * @param token The JWT token string.
     * @param flag Is a boolean data that is only false on login, register or password reset since then we don't need to refresh the token.
     *
     * @see UserSession
     */
    private fun initUserSession(token: String, flag: Boolean = true) {
        viewModel.getProfile(token)

        viewModel.profileResult.observe(this, Observer { result ->
            if (result is Result.Success) {
                UserSession.init(result.data)
            }
        })

        if (flag) {
            viewModel.tokenRefresh(token)

            viewModel.tokenRefreshResult.observe(this, Observer { result ->
                if (result is Result.Success) {
                    sessionManager.saveAuthToken(result.data.accessToken)
                }
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            })
        }
    }

    /**
     * Sets up the UI content using Jetpack Compose and applies the app theme.
     *
     * Displays the Authentication navigation flow.
     *
     * @see AppTheme
     * @see AuthNavHost
     */
    private fun setupComposeUI() {
        setContent {
            AppTheme {
                AuthNavHost(viewModel)
            }
        }
    }

    /**
     * Observes LiveData from the ViewModel to handle authentication operation results.
     *
     * Displays Toast messages based on the result of login, register, and reset password operations.
     */
    private fun observeViewModel() {
        /**
         * Handles a result from an authentication operation and displays feedback.
         *
         * @param result The result of the operation.
         * @param successMessage The string resource Id to display when the operation succeeds.
         *
         * @see Result
         * @see AuthViewModel
         */
        fun handleResult(result: Result<Token>, successMessage: Int) {
            when (result) {
                is Result.Success -> {
                    sessionManager.saveAuthToken(result.data.accessToken)
                    initUserSession(sessionManager.getAuthToken()!!, false)
                    startActivity(Intent(this, HomeActivity::class.java))
                    Toast.makeText(this, getString(successMessage), Toast.LENGTH_SHORT).show()
                    finish()
                }
                is Result.Failure -> {} // No feedback for Failure
                is Result.Error -> {
                    Toast.makeText(this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show()
                }
                is Result.Idle -> {} // No action needed for Idle state
            }
        }

        /**
         * Sets up the Observers for authentication operations (login, register, reset password).
         */
        viewModel.loginResult.observe(this, Observer { handleResult(it, R.string.login_success) })
        viewModel.registerResult.observe(this,
            Observer { handleResult(it, R.string.register_success) })
        viewModel.resetPasswordResult.observe(this,
            Observer { handleResult(it, R.string.reset_password_success) })
    }
}