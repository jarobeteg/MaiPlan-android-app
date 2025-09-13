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
import com.example.maiplan.repository.auth.AuthLocalDataSource
import com.example.maiplan.repository.auth.AuthRemoteDataSource
import com.example.maiplan.theme.AppTheme
import com.example.maiplan.utils.SessionManager
import com.example.maiplan.utils.common.UserSession
import com.example.maiplan.viewmodel.auth.AuthViewModel
import com.example.maiplan.viewmodel.GenericViewModelFactory
import com.example.maiplan.utils.BaseActivity

class MainActivity : BaseActivity() {
    private lateinit var sessionManager: SessionManager
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent { AppTheme { LoadingScreen() } }
        setupViewModel()

        val token = sessionManager.getAuthToken()
        if (token != null) {
            // If a token exists, fetch user profile and refresh the token
            setupUserSession(token)
        }

        setupComposeUI()
        observeViewModel()
    }

    private fun setupViewModel() {
        val authRemoteDataSource = AuthRemoteDataSource(RetrofitClient.authApi)
        val authLocalDataSource = AuthLocalDataSource(this)
        val authRepository = AuthRepository(authRemoteDataSource, authLocalDataSource)
        val factory = GenericViewModelFactory { AuthViewModel(authRepository) }

        viewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]
        sessionManager = SessionManager(this)
    }

    // needs refactoring as its an unreadable pile of mess
    private fun setupUserSession(token: String, message: Int = R.string.login_success, flag: Boolean = true) {
        viewModel.getProfile(token)

        viewModel.profileResult.observe(this, Observer { result ->
            if (result is Result.Success) {
                UserSession.setup(result.data)

                if (flag) {
                    viewModel.tokenRefresh(token)

                    viewModel.tokenRefreshResult.observe(this, Observer { result ->
                        if (result is Result.Success) {
                            sessionManager.saveAuthToken(result.data.accessToken)
                        }
                        else {
                            Toast.makeText(this, getString(R.string.token_refresh_failed), Toast.LENGTH_SHORT).show()
                        }
                        goToHome(message)
                    })
                } else {
                    goToHome(message)
                }
            } else {
                Toast.makeText(this, getString(R.string.no_user_data_found), Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupComposeUI() {
        setContent {
            AppTheme {
                AuthNavHost(viewModel)
            }
        }
    }

    private fun goToHome(message: Int) {
        startActivity(Intent(this, HomeActivity::class.java))
        Toast.makeText(this, getString(message), Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun observeViewModel() {
        fun handleResult(result: Result<Token>, message: Int) {
            when (result) {
                is Result.Success -> {
                    sessionManager.saveAuthToken(result.data.accessToken)
                    setupUserSession(sessionManager.getAuthToken()!!, message, flag = false)
                }
                is Result.Failure -> {
                    Toast.makeText(this, getString(R.string.failure) + " ${result.errorCode}", Toast.LENGTH_SHORT).show()
                }
                is Result.Error -> {
                    Toast.makeText(this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show()
                }
                is Result.Idle -> {} // No action needed for Idle state
            }
        }

        viewModel.loginResult.observe(this, Observer { handleResult(it, R.string.login_success) })
        viewModel.registerResult.observe(this, Observer { handleResult(it, R.string.register_success)})
        viewModel.resetPasswordResult.observe(this, Observer { handleResult(it, R.string.reset_password_success) })
    }
}