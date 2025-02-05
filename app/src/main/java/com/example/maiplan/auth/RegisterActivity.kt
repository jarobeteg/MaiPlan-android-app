package com.example.maiplan.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.maiplan.R
import com.example.maiplan.network.RetrofitClient
import com.example.maiplan.network.UserAuth
import com.example.maiplan.repository.AuthRepository
import com.example.maiplan.viewmodel.AuthViewModel
import com.example.maiplan.viewmodel.AuthViewModelFactory

class RegisterActivity : AppCompatActivity() {

    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val apiService = RetrofitClient.instance
        val authRepository = AuthRepository(apiService)

        val emailEditText = findViewById<EditText>(R.id.registerEmailEditText)
        val usernameEditText = findViewById<EditText>(R.id.registerUsernameEditText)
        val passwordEditText = findViewById<EditText>(R.id.registerPasswordEditText)
        val registerButton = findViewById<Button>(R.id.registerButton)

        val factory = AuthViewModelFactory(authRepository)
        viewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()
            val user = UserAuth(email, username, password)

            viewModel.register(user)
        }

        viewModel.registerResult.observe(this, Observer { result ->
            when (result) {
                is AuthRepository.Result.Success -> {
                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                is AuthRepository.Result.Error -> {
                    println("Registration failed: ${result.exception.message}")
                }
            }
        })
    }
}