package com.example.maiplan.main

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
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
import com.example.maiplan.utils.common.UserSession
import com.example.maiplan.viewmodel.auth.AuthViewModel
import com.example.maiplan.viewmodel.GenericViewModelFactory
import com.example.maiplan.utils.BaseActivity

/**
 * [MainActivity] handles user authentication flows,
 * including login, registration, and session management.
 *
 * Inherits from [BaseActivity] to maintain consistent edge-to-edge UI and
 * system bar styling throughout the app.
 *
 * This activity initializes the [AuthViewModel], checks the user’s authentication status,
 * and either navigates to the main content or presents the authentication screens.
 * It observes authentication-related state changes to provide appropriate UI feedback.
 */
class MainActivity : BaseActivity() {

    /** Manager for handling session-related actions such as saving and retrieving tokens. */
    private lateinit var sessionManager: SessionManager

    /** [AuthViewModel] instance to handle Authentication related logic. */
    private lateinit var viewModel: AuthViewModel

    private var serverOffline = false // ONLY FOR DEBUGGING AND TESTING NON SERVER RELATED FUNCTIONS

    /**
     * Lifecycle method [onCreate] is called when the [MainActivity] is created.
     * - The [AuthRepository] is initialized using the [AuthRemoteDataSource].
     * - The [AuthRemoteDataSource] is initialized via the [RetrofitClient.authApi].
     * - The [AuthRepository] provides authentication logic using [AuthRemoteDataSource].
     * - The [AuthRemoteDataSource] handles communication with the authentication API.
     * - The [GenericViewModelFactory] is used to pass the [AuthRepository] dependency to the [AuthViewModel].
     * - Checks if a token already exists in the [UserSession] (i.e., user previously authenticated).
     * - If a token exists:
     *      - Refreshes the access token.
     *      - Retrieves the user's profile data and stores it in [UserSession].
     * - If no token exists:
     *      - Displays the authentication flow to the user.
     * - Sets up the UI using `Jetpack Compose`
     * - Observes [AuthViewModel] result `LiveData` to provide feedback to the user.
     *
     * @param savedInstanceState Saved state of this [MainActivity] if previously existed.
     *
     * @see RetrofitClient
     * @see AuthRepository
     * @see UserSession
     * @see AuthViewModel
     * @see GenericViewModelFactory
     * @see AuthRemoteDataSource
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show a loading screen while checking authentication state
        setContent { AppTheme { LoadingScreen() } }

        if (serverOffline) { // ONLY FOR DEBUGGING AND TESTING NON SERVER RELATED FUNCTIONS
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

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
     * Initializes the [UserSession] singleton object, and refreshes the JWT token and saves it in the Shared Preferences.
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
     * Displays the `Authentication` navigation flow.
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
     * Observes `LiveData` from the [AuthViewModel] to handle authentication operation results.
     *
     * Displays [Toast] messages based on the result of authentication operations.
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

        /*
         * Sets up the Observers for authentication operations (login, register, reset password).
         */
        viewModel.loginResult.observe(this, Observer { handleResult(it, R.string.login_success) })
        viewModel.registerResult.observe(this, Observer { handleResult(it, R.string.register_success) })
        viewModel.resetPasswordResult.observe(this, Observer { handleResult(it, R.string.reset_password_success) })
    }
}