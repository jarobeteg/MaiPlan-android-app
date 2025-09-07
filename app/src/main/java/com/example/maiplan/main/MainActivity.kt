package com.example.maiplan.main

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.maiplan.R
import com.example.maiplan.home.HomeActivity
import com.example.maiplan.main.navigation.AuthNavHost
import com.example.maiplan.main.screens.LoadingScreen
import com.example.maiplan.network.NetworkChecker
import com.example.maiplan.network.RetrofitClient
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
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {
    private lateinit var sessionManager: SessionManager
    private lateinit var viewModel: AuthViewModel
    private lateinit var networkChecker: NetworkChecker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent { AppTheme { LoadingScreen() } }

        networkChecker = NetworkChecker(this)
        println("Is android device online: ${networkChecker.isOnline()}")
        lifecycleScope.launch {
            println("Is server reachable: ${networkChecker.canReachServer()}")
        }

        initViewModel()

        val token = sessionManager.getAuthToken()
        if (token != null) {
            // If a token exists, fetch user profile and refresh the token
            //initUserSession(token)
            setupComposeUI()
        } else {
            setupComposeUI()
        }

        observeViewModel()
    }

    private fun initViewModel() {
        val authRemoteDataSource = AuthRemoteDataSource(RetrofitClient.authApi)
        val authLocalDataSource = AuthLocalDataSource(this)
        val authRepository = AuthRepository(authRemoteDataSource, authLocalDataSource)
        val factory = GenericViewModelFactory { AuthViewModel(authRepository) }

        viewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]
        sessionManager = SessionManager(this)
    }

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

    private fun setupComposeUI() {
        setContent {
            AppTheme {
                AuthNavHost(viewModel)
            }
        }
    }

    private fun observeViewModel() {

        fun handleResult(token: String, successMessage: Int) {
            //sessionManager.saveAuthToken(token)
            //initUserSession(sessionManager.getAuthToken()!!, false)
            lifecycleScope.launch {
                if (networkChecker.isOnline() && networkChecker.canReachServer()) {
                    viewModel.sync()
                }
            }
            startActivity(Intent(this, HomeActivity::class.java))
            Toast.makeText(this, getString(successMessage), Toast.LENGTH_SHORT).show()
            finish()
//            when (result) {
//                is Result.Success -> {
//                    sessionManager.saveAuthToken(result.data.accessToken)
//                    initUserSession(sessionManager.getAuthToken()!!, false)
//                    startActivity(Intent(this, HomeActivity::class.java))
//                    Toast.makeText(this, getString(successMessage), Toast.LENGTH_SHORT).show()
//                    finish()
//                }
//                is Result.Failure -> {} // No feedback for Failure
//                is Result.Error -> {
//                    Toast.makeText(this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show()
//                }
//                is Result.Idle -> {} // No action needed for Idle state
//            }
        }

        //viewModel.loginResult.observe(this, Observer { handleResult(it, R.string.login_success) })
        viewModel.localRegisterResult.observe(this, Observer { handleResult(it, R.string.register_success) })
        viewModel.syncResult.observe(this, { result ->
            when (result)  {
                is Result.Success -> println("Sync completed")
                is Result.Error -> println("Sync failed: ${result.exception}")
                else -> println("Unknown error")
            }
        })
        //viewModel.resetPasswordResult.observe(this, Observer { handleResult(it, R.string.reset_password_success) })
    }
}