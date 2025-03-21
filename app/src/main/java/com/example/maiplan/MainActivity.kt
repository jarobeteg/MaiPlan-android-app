package com.example.maiplan

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.maiplan.home.event.EventActivity
import com.example.maiplan.main_screens.AuthNavHost
import com.example.maiplan.network.RetrofitClient
import com.example.maiplan.network.Token
import com.example.maiplan.repository.AuthRepository
import com.example.maiplan.repository.Result
import com.example.maiplan.main_screens.LoadingScreen
import com.example.maiplan.theme.AppTheme
import com.example.maiplan.utils.SessionManager
import com.example.maiplan.utils.UserSession
import com.example.maiplan.viewmodel.AuthViewModel
import com.example.maiplan.viewmodel.GenericViewModelFactory

class MainActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent { AppTheme { LoadingScreen() } }

        val authApi = RetrofitClient.authApi
        val authRepository = AuthRepository(authApi)
        val factory = GenericViewModelFactory { AuthViewModel(authRepository) }

        sessionManager = SessionManager(this)
        viewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        val token = sessionManager.getAuthToken()
        if (token != null) {
            viewModel.getProfile(token)
            viewModel.tokenRefresh(token)

            viewModel.profileResult.observe(this, Observer { result ->
                if (result is Result.Success) {
                    UserSession.init(result.data)
                }
            })

            viewModel.tokenRefreshResult.observe(this, Observer { result ->
                if (result is Result.Success) {
                    sessionManager.saveAuthToken(result.data.accessToken)
                }
                startActivity(Intent(this, EventActivity::class.java))
                finish()
            })
        } else {
            setupComposeUI()
        }

        observeViewModel()
    }

    private fun setupComposeUI() {
        setContent {
            AppTheme {
                AuthNavHost(viewModel)
            }
        }
    }

    private fun observeViewModel() {
        fun handleResult(result: Result<Token>, successMessage: Int) {
            when (result) {
                is Result.Success -> {
                    sessionManager.saveAuthToken(result.data.accessToken)
                    startActivity(Intent(this, EventActivity::class.java))
                    Toast.makeText(this, getString(successMessage), Toast.LENGTH_SHORT).show()
                    finish()
                }
                is Result.Failure -> {}
                is Result.Error -> {
                    Toast.makeText(this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show()
                }
                is Result.Idle -> {}
            }
        }

        viewModel.loginResult.observe(this, Observer { handleResult(it, R.string.login_success) })
        viewModel.registerResult.observe(this, Observer { handleResult(it, R.string.register_success) })
        viewModel.resetPasswordResult.observe(this, Observer { handleResult(it, R.string.reset_password_success) })
    }
}