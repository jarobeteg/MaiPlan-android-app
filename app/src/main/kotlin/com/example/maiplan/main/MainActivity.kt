package com.example.maiplan.main

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.maiplan.R
import com.example.maiplan.database.entities.toUserResponse
import com.example.maiplan.home.HomeActivity
import com.example.maiplan.main.navigation.AuthNavHost
import com.example.maiplan.main.screens.LoadingScreen
import com.example.maiplan.network.RetrofitClient
import com.example.maiplan.network.api.AuthResponse
import com.example.maiplan.repository.auth.AuthRepository
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.auth.AuthLocalDataSource
import com.example.maiplan.repository.auth.AuthRemoteDataSource
import com.example.maiplan.theme.AppTheme
import com.example.maiplan.utils.common.UserSession
import com.example.maiplan.viewmodel.auth.AuthViewModel
import com.example.maiplan.viewmodel.GenericViewModelFactory
import com.example.maiplan.utils.BaseActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : BaseActivity() {
    private lateinit var viewModel: AuthViewModel
    private var messageId: Int? = null
    private var uiInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setAppContent { AppTheme { LoadingScreen() } }
        setupDependencies()
        observeViewModel()

        lifecycleScope.launch {
            val isReachable = withContext(Dispatchers.IO) { networkChecker.canReachServer() }
            if (!isReachable) Toast.makeText(this@MainActivity, getString(R.string.server_unreachable), Toast.LENGTH_SHORT).show()

            val token = withContext(Dispatchers.IO) { sessionManager.getToken() }

            if (token == null) {
                setupComposeUIOnce()
                return@launch
            }

            if (isReachable) {
                viewModel.getProfile(token)
            } else {
                val user = sessionManager.getUserInfo()
                if (user != null) {
                    UserSession.setup(user)
                    messageId = R.string.welcome_back
                    goToHome()
                } else {
                    setupComposeUIOnce()
                }
            }
        }
    }

    private fun setupComposeUIOnce() {
        if (!uiInitialized) {
            uiInitialized = true
            setupComposeUI()
        }
    }

    private fun setupComposeUI() {
        setAppContent { AppTheme { AuthNavHost(viewModel, this) } }
    }

    private fun setupDependencies() {
        val authRemote = AuthRemoteDataSource(RetrofitClient.authApi)
        val authLocal = AuthLocalDataSource(this)
        val authRepo = AuthRepository(authRemote, authLocal)

        val factory = GenericViewModelFactory { AuthViewModel(authRepo, networkChecker) }

        viewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]
    }

    private fun observeViewModel() {
        val authObserver = Observer<Result<AuthResponse>> { result ->
            if (result is Result.Success) {
                sessionManager.saveSession(result.data.accessToken, result.data.user)
                goToHome()
            } else {
                handleAuthFailure(result)
            }
        }

        viewModel.profileResult.observe(this) { result ->
            when (result) {
                is Result.Success -> {
                    sessionManager.saveSession(result.data.accessToken, result.data.user)
                    messageId = R.string.welcome_back
                    goToHome()
                }
                is Result.Failure, is Result.Error -> {
                    Toast.makeText(this, getString(R.string.no_user_data_found), Toast.LENGTH_SHORT).show()
                    sessionManager.clear()
                    setupComposeUIOnce()
                }
                else -> {}
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

        viewModel.localLoginResult.observe(this) { response ->
            if (response != null) {
                UserSession.setup(response.toUserResponse())
                messageId = R.string.login_success
                goToHome()
            } else {
                Toast.makeText(this, getString(R.string.incorrect_cred), Toast.LENGTH_SHORT).show()
                viewModel.resetLoginResult()
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
               launch {
                   viewModel.showServerUnreachableToast.collect {
                       Toast.makeText(
                           this@MainActivity,
                           getString(R.string.server_unreachable_try_again),
                           Toast.LENGTH_SHORT).show()
                   }
               }
                launch {
                    viewModel.showLocalLoginToast.collect {
                        Toast.makeText(
                            this@MainActivity,
                            getString(R.string.server_unreachable_skip_to_login),
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun handleAuthFailure(result: Result<AuthResponse>) {
        val message = when (result) {
            is Result.Failure -> getString(R.string.failure) + "${result.errorCode}"
            is Result.Error -> getString(R.string.unknown_error)
            else -> return
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        setupComposeUIOnce()
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