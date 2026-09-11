package com.example.maiplan.main

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.maiplan.database.entities.UserEntity
import com.example.maiplan.R
import com.example.maiplan.home.HomeActivity
import com.example.maiplan.main.navigation.AuthNavHost
import com.example.maiplan.main.screens.LoadingScreen
import com.example.maiplan.network.RetrofitClient
import com.example.maiplan.repository.auth.AuthRepository
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.auth.UserLocalDataSource
import com.example.maiplan.repository.auth.AuthRemoteDataSource
import com.example.maiplan.theme.AppTheme
import com.example.maiplan.utils.common.UserSession
import com.example.maiplan.utils.DeviceIdentityStore
import com.example.maiplan.viewmodel.auth.AuthViewModel
import com.example.maiplan.viewmodel.GenericViewModelFactory
import com.example.maiplan.utils.BaseActivity
import java.io.IOException

class MainActivity : BaseActivity() {
    private lateinit var authViewModel: AuthViewModel
    private var messageId: Int? = null
    private var uiInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setAppContent { AppTheme { LoadingScreen() } }
        setupDependencies()
        observeViewModel()
        authViewModel.loadCachedSessionUser()
    }

    private fun setupComposeUIOnce() {
        if (!uiInitialized) {
            uiInitialized = true
            setupComposeUI()
        }
    }

    private fun setupComposeUI() {
        setAppContent { AppTheme { AuthNavHost(authViewModel) } }
    }

    private fun setupDependencies() {
        val authRemote = AuthRemoteDataSource(
            publicAuthApi = RetrofitClient.publicAuthApi,
            deviceId = DeviceIdentityStore(applicationContext).getOrCreateDeviceId()
        )
        val authLocal = UserLocalDataSource(applicationContext)
        val authRepo = AuthRepository(authRemote, authLocal, sessionManager)

        val factory = GenericViewModelFactory { AuthViewModel(authRepo) }

        authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]
    }

    private fun observeViewModel() {
        authViewModel.cachedSessionUser.observe(this) { result ->
            when (result) {
                is Result.Success -> {
                    val user = result.data

                    if (user == null) {
                        UserSession.clear()
                        setupComposeUIOnce()
                    } else {
                        UserSession.setup(user)
                        authViewModel.refreshSession()
                    }
                }

                is Result.Error,
                is Result.Failure -> {
                    sessionManager.clearSession()
                    UserSession.clear()
                    setupComposeUIOnce()
                }

                else -> Unit
            }
        }

        authViewModel.sessionRefreshResult.observe(this) { result ->
            when (result) {
                is Result.Success -> {
                    UserSession.setup(result.data)
                    messageId = R.string.welcome_back
                    goToHome()
                }
                is Result.Failure,
                is Result.Error -> {
                    if (
                        canUseCachedSession(result) &&
                        sessionManager.hasSession() &&
                        UserSession.isLoggedIn()
                    ) {
                        messageId = R.string.welcome_back
                        goToHome()
                    } else {
                        sessionManager.clearSession()
                        UserSession.clear()
                        setupComposeUIOnce()
                    }
                }
                else -> {}
            }
        }

        authViewModel.loginResult.observe(this) { result ->
            handleAuthenticationResult(result, R.string.login_success)
        }

        authViewModel.registerResult.observe(this) { result ->
            handleAuthenticationResult(result, R.string.register_success)
        }
    }

    private fun handleAuthenticationResult(
        result: Result<UserEntity>,
        successMessageId: Int
    ) {
        if (result is Result.Success) {
            UserSession.setup(result.data)
            messageId = successMessageId
            goToHome()
        } else {
            handleAuthFailure(result)
        }
    }

    private fun canUseCachedSession(result: Result<UserEntity>): Boolean {
        return when (result) {
            is Result.Error -> result.exception is IOException
            is Result.Failure -> result.httpStatus == 408 ||
                    result.httpStatus == 425 ||
                    result.httpStatus == 429 ||
                    result.httpStatus?.let { it in 500..599 } == true
            else -> false
        }
    }

    private fun handleAuthFailure(result: Result<*>) {
        val message = when (result) {
            is Result.Failure -> getString(R.string.failure) + "${result.errorCode}"
            is Result.Error -> getString(R.string.unknown_error)
            else -> return
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        setupComposeUIOnce()
    }

    private fun goToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        messageId?.let {
            Toast.makeText(this, getString(it), Toast.LENGTH_SHORT).show()
        }
        finish()
    }
}
