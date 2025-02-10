package com.example.maiplan.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.maiplan.R
import com.example.maiplan.components.ClickableTextComponent
import com.example.maiplan.components.EmailTextComponent
import com.example.maiplan.components.ErrorMessageComponent
import com.example.maiplan.components.UsernameTextComponent
import com.example.maiplan.components.HeadingTextComponent
import com.example.maiplan.components.PasswordTextComponent
import com.example.maiplan.components.SubmitButtonComponent
import com.example.maiplan.repository.AuthRepository
import com.example.maiplan.viewmodel.AuthViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel? = null,
    onRegisterClick: (String, String, String, String) -> Unit,
    onBackToLogin: () -> Unit
) {
    val systemUiController = rememberSystemUiController()

    SideEffect {
        systemUiController.setStatusBarColor(color = Color.Transparent, darkIcons = false)
    }

    val registerResult by viewModel!!.registerResult.observeAsState()
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordAgain by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var passwordAgainVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF8A9DB2), Color(0xFF4A6583))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.95f))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HeadingTextComponent(stringResource(R.string.create_an_account))

            Spacer(modifier = Modifier.height(16.dp))

            EmailTextComponent(email, onEmailChange = { email = it })

            Spacer(modifier = Modifier.height(8.dp))

            UsernameTextComponent(username, onUsernameChange = { username = it })

            Spacer(modifier = Modifier.height(8.dp))

            PasswordTextComponent(
                password,
                stringResource(R.string.password),
                onPasswordChange = { password = it },
                passwordVisible,
                onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
                shouldIndicatorBeVisible = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            PasswordTextComponent(
                passwordAgain,
                stringResource(R.string.password_again),
                onPasswordChange = { passwordAgain = it },
                passwordAgainVisible,
                onTogglePasswordVisibility = { passwordAgainVisible = !passwordAgainVisible }
            )

            Spacer(modifier = Modifier.height(8.dp))

            SubmitButtonComponent(stringResource(R.string.register), onButtonClicked = { onRegisterClick(email, username, password, passwordAgain) })

            if (registerResult is AuthRepository.Result.Failure) {
                Spacer(modifier = Modifier.height(8.dp))
                val error = registerResult as AuthRepository.Result.Failure
                val code = error.errorCode

                val errorMessageId = when (code) {
                    1 -> R.string.register_error_1
                    2 -> R.string.register_error_2
                    3 -> R.string.register_error_3
                    4 -> R.string.register_error_4
                    5 -> R.string.register_error_5
                    6 -> R.string.register_error_6
                    7 -> R.string.register_error_7
                    8 -> R.string.register_error_8
                    9 -> R.string.register_error_9
                    else -> R.string.register_error_default
                }

                val errorMessage = stringResource(errorMessageId)
                ErrorMessageComponent(errorMessage)
            }

            Spacer(modifier = Modifier.height(8.dp))

            ClickableTextComponent(stringResource(R.string.have_account), onBackToLogin)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewRegisterScreen() {
    RegisterScreen(onRegisterClick = { _, _, _, _ ->}, onBackToLogin = {})
}