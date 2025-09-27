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
import com.example.maiplan.network.api.AuthResponse
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
    private var messageId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent { AppTheme { LoadingScreen() } }
        setupDependencies()
        observeViewModel()

        sessionManager.getToken()?.let { token ->
            viewModel.getProfile(token)
        } ?: run {
            setupComposeUI()
        }
    }

    private fun setupComposeUI() {
        setContent { AppTheme { AuthNavHost(viewModel) } }
    }

    private fun setupDependencies() {
        val authRemote = AuthRemoteDataSource(RetrofitClient.authApi)
        val authLocal = AuthLocalDataSource(this)
        val authRepo = AuthRepository(authRemote, authLocal)
        val factory = GenericViewModelFactory { AuthViewModel(authRepo) }

        viewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]
        sessionManager = SessionManager(this)
    }

    private fun observeViewModel() {
        val authObserver = Observer<Result<AuthResponse>> { result ->
            if (result is Result.Success) {
                UserSession.setup(result.data.user)
                sessionManager.saveToken(result.data.accessToken)
                goToHome()
            } else {
                handleAuthFailure(result)
            }
        }

        viewModel.profileResult.observe(this) { result ->
            when (result) {
                is Result.Success -> {
                    UserSession.setup(result.data.user)
                    sessionManager.saveToken(result.data.accessToken)
                    messageId = R.string.welcome_back
                    goToHome()
                }
                is Result.Failure, is Result.Error -> {
                    Toast.makeText(this, getString(R.string.no_user_data_found), Toast.LENGTH_SHORT).show()
                    sessionManager.clearToken()
                    setupComposeUI()
                }
                is Result.Idle -> {}
            }
        }

        viewModel.loginResult.observe(this) { result ->
            messageId = R.string.login_success
            authObserver.onChanged(result)
        }

        viewModel.registerResult.observe(this) { result ->
            messageId = R.string.register_success
            authObserver.onChanged(result)
        }

        viewModel.resetPasswordResult.observe(this) { result ->
            messageId = R.string.reset_password_success
            authObserver.onChanged(result)
        }
    }

    private fun handleAuthFailure(result: Result<AuthResponse>) {
        val message = when (result) {
            is Result.Failure -> getString(R.string.failure) + "${result.errorCode}"
            is Result.Error -> getString(R.string.unknown_error)
            else -> return
        }
        if (result is Result.Error) {
            println("error: ${result.exception.message}")
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        setupComposeUI()
    }

    private fun goToHome() {
        viewModel.sync()
        startActivity(Intent(this, HomeActivity::class.java))
        messageId?.let {
            Toast.makeText(this, getString(it), Toast.LENGTH_SHORT).show()
        }
        finish()
    }
}