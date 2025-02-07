package com.example.maiplan

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.maiplan.home.HomeActivity
import com.example.maiplan.network.RetrofitClient
import com.example.maiplan.network.UserLogin
import com.example.maiplan.network.UserRegister
import com.example.maiplan.repository.AuthRepository
import com.example.maiplan.screens.LoginScreen
import com.example.maiplan.screens.RegisterScreen
import com.example.maiplan.utils.SessionManager
import com.example.maiplan.viewmodel.AuthViewModel
import com.example.maiplan.viewmodel.AuthViewModelFactory

class MainActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var viewModel: AuthViewModel
    private var isRegisterScreen = mutableStateOf(false)

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
            BackHandler(enabled = isRegisterScreen.value) {
                isRegisterScreen.value = false // on back pressed switches to login screen
            }

            if (isRegisterScreen.value) {
                RegisterScreen(
                    onRegisterClick = { email, username, password ->
                        val user = UserRegister(email, username, password)
                        viewModel.register(user)
                    },
                    onBackToLogin = { isRegisterScreen.value = false }
                )
            } else {
                LoginScreen(
                    onLoginClick = { email, password ->
                        val user = UserLogin(email, password)
                        viewModel.login(user)
                    },
                    toRegisterClick = { isRegisterScreen.value = true }
                )
            }
        }
    }

    private fun observeViewModel() {
        viewModel.loginResult.observe(this, Observer { result ->
            when (result) {
                is AuthRepository.Result.Success -> {
                    sessionManager.saveAuthToken(result.data.accessToken)
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
                is AuthRepository.Result.Error -> {
                    println("Login failed: ${result.exception.message}")
                }
            }
        })

        viewModel.registerResult.observe(this, Observer { result ->
            when (result) {
                is AuthRepository.Result.Success -> {
                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                    isRegisterScreen.value = false

                }
                is AuthRepository.Result.Error -> {
                    println("Registration failed: ${result.exception.message}")
                }
            }
        })
    }
}