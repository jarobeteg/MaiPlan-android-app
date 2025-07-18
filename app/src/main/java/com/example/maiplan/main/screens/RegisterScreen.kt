package com.example.maiplan.main.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import com.example.maiplan.R
import com.example.maiplan.components.ClickableTextComponent
import com.example.maiplan.components.EmailTextComponent
import com.example.maiplan.components.ErrorMessageComponent
import com.example.maiplan.components.UsernameTextComponent
import com.example.maiplan.components.HeadingTextComponent
import com.example.maiplan.components.PasswordTextComponent
import com.example.maiplan.components.SubmitButtonComponent
import com.example.maiplan.repository.Result
import com.example.maiplan.viewmodel.auth.AuthViewModel

/**
 * [Composable] function for the Register screen UI.
 *
 * This screen allows users to create a new account by providing an email, username,
 * password, and confirming the password. It also handles displaying registration errors
 * and provides a navigation link to return to the [LoginScreen].
 *
 * - Includes input fields for email, username, password, and password confirmation.
 * - Provides password visibility toggles.
 * - Displays error messages based on specific error codes from registration attempt.
 * - Uses the app's Material theme and a gradient background.
 *
 * @param viewModel The [AuthViewModel] instance used to observe the registration result.
 * @param onRegisterClick Callback invoked when the register button is clicked,
 * passing email, username, password, and password confirmation.
 * @param onBackToLogin Callback invoked when the user wants to navigate back to the [LoginScreen].
 *
 * @see AuthViewModel
 * @see Result
 * @see ClickableTextComponent
 * @see EmailTextComponent
 * @see ErrorMessageComponent
 * @see UsernameTextComponent
 * @see HeadingTextComponent
 * @see PasswordTextComponent
 * @see SubmitButtonComponent
 */
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegisterClick: (String, String, String, String) -> Unit,
    onBackToLogin: () -> Unit
) {
    val registerResult by viewModel.registerResult.observeAsState()
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
                    colors = listOf(MaterialTheme.colorScheme.surfaceContainer, MaterialTheme.colorScheme.primary)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .clip(MaterialTheme.shapes.large)
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

            if (registerResult is Result.Failure) {
                Spacer(modifier = Modifier.height(8.dp))
                val error = registerResult as Result.Failure
                val code = error.errorCode

                val errorMessageId = when (code) {
                    1 -> R.string.general_error_1
                    2 -> R.string.general_error_2
                    4 -> R.string.general_error_4
                    5 -> R.string.general_error_5
                    6 -> R.string.general_error_6
                    7 -> R.string.general_error_7
                    8 -> R.string.register_error_8
                    9 -> R.string.register_error_9
                    10 -> R.string.register_error_10
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