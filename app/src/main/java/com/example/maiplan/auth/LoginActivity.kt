package com.example.maiplan.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.maiplan.R
import com.example.maiplan.home.HomeActivity
import com.example.maiplan.network.RetrofitClient
import com.example.maiplan.network.UserAuth
import com.example.maiplan.repository.AuthRepository
import com.example.maiplan.utils.SessionManager
import com.example.maiplan.viewmodel.AuthViewModel
import com.example.maiplan.viewmodel.AuthViewModelFactory

class LoginActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sessionManager = SessionManager(this)

        val apiService = RetrofitClient.instance
        val authRepository = AuthRepository(apiService)

        val emailEditText = findViewById<EditText>(R.id.loginEmailEditText)
        val usernameEditText = findViewById<EditText>(R.id.loginUsernameEditText)
        val passwordEditText = findViewById<EditText>(R.id.loginPasswordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerTextView = findViewById<TextView>(R.id.toRegisterActivity)

        val factory = AuthViewModelFactory(authRepository)
        viewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()
            val user = UserAuth(email, username, password)

            viewModel.login(user)
        }

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

        registerTextView.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}