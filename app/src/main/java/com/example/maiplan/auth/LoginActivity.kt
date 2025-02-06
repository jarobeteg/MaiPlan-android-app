package com.example.maiplan.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
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
        setContent {
            LoginScreen(onLoginClick =  { email, username, password ->
                val user = UserAuth(email, username, password)
                viewModel.login(user)
            }, onRegisterClick = {
                startActivity(Intent(this, RegisterActivity::class.java))
            })
        }

        sessionManager = SessionManager(this)

        val apiService = RetrofitClient.instance
        val authRepository = AuthRepository(apiService)

        val factory = AuthViewModelFactory(authRepository)
        viewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

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
    }

    @Composable
    fun LoginScreen(
        onLoginClick: (String, String, String) -> Unit,
        onRegisterClick: () -> Unit
    ) {
        var email by remember { mutableStateOf("") }
        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onLoginClick(email, username, password) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Don't have an account yet!",
                modifier = Modifier
                    .clickable { onRegisterClick() }
                    .padding(8.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun PreviewLogicScreen() {
        LoginScreen(onLoginClick = { _, _, _ ->}, onRegisterClick = {})
    }
}