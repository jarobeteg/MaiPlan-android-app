package com.example.maiplan

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.maiplan.home.HomeActivity
import com.example.maiplan.network.RetrofitClient
import com.example.maiplan.network.Token
import com.example.maiplan.network.UserLogin
import com.example.maiplan.network.UserRegister
import com.example.maiplan.network.UserResetPassword
import com.example.maiplan.repository.AuthRepository
import com.example.maiplan.screens.ForgotPasswordScreen
import com.example.maiplan.screens.LoginScreen
import com.example.maiplan.screens.RegisterScreen
import com.example.maiplan.utils.SessionManager
import com.example.maiplan.viewmodel.AuthViewModel
import com.example.maiplan.viewmodel.AuthViewModelFactory

class MainActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var viewModel: AuthViewModel
    private var isRegisterScreen = mutableStateOf(false)
    private var isForgotPasswordScreen = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val apiService = RetrofitClient.instance
        val authRepository = AuthRepository(apiService)
        val factory = AuthViewModelFactory(authRepository)

        sessionManager = SessionManager(this)
        viewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        if (sessionManager.getAuthToken() != null) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        } else {
            setupComposeUI()
        }

        observeViewModel()
    }

    private fun setupComposeUI() {
        setContent {
            BackHandler(enabled = isRegisterScreen.value || isForgotPasswordScreen.value) {
                isRegisterScreen.value = false // on back pressed switches to login screen
                isForgotPasswordScreen.value = false // on back pressed switches to login screen
                viewModel.clearErrors()
            }

            when {
                isRegisterScreen.value -> RegisterScreen(
                    viewModel = viewModel,
                    onRegisterClick = { email, username, password, passwordAgain ->
                        viewModel.register(UserRegister(email, username, password, passwordAgain))
                    },
                    onBackToLogin = {
                        isRegisterScreen.value = false
                        viewModel.clearErrors()
                    }
                )

                isForgotPasswordScreen.value -> ForgotPasswordScreen(
                    viewModel = viewModel,
                    onResetClick = { email, password, passwordAgain ->
                        viewModel.resetPassword(UserResetPassword(email, password, passwordAgain))
                    },
                    onBackToLogin = {
                        isForgotPasswordScreen.value = false
                        viewModel.clearErrors()
                    }
                )

                else -> LoginScreen(
                    viewModel = viewModel,
                    onLoginClick = { email, password ->
                        viewModel.login(UserLogin(email, password))
                    },
                    toRegisterClick = {
                        isRegisterScreen.value = true
                        viewModel.clearErrors()
                    },
                    onForgotPasswordClick = {
                        isForgotPasswordScreen.value = true
                        viewModel.clearErrors()
                    }
                )
            }
        }
    }

    private fun observeViewModel() {
        fun handleResult(result: AuthRepository.Result<Token>, successMessage: Int) {
            when (result) {
                is AuthRepository.Result.Success -> {
                    sessionManager.saveAuthToken(result.data.accessToken)
                    startActivity(Intent(this, HomeActivity::class.java))
                    Toast.makeText(this, getString(successMessage), Toast.LENGTH_SHORT).show()
                    finish()
                }
                is AuthRepository.Result.Failure -> {}
                is AuthRepository.Result.Error -> {
                    Toast.makeText(this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show()
                }
                is AuthRepository.Result.Idle -> {}
            }
        }

        viewModel.loginResult.observe(this, Observer { handleResult(it, R.string.login_success) })
        viewModel.registerResult.observe(this, Observer { handleResult(it, R.string.register_success) })
        viewModel.resetPasswordResult.observe(this, Observer { handleResult(it, R.string.reset_password_success) })
    }
}