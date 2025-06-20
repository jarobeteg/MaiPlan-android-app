package com.example.maiplan.main.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.maiplan.R
import com.example.maiplan.components.ClickableTextComponent
import com.example.maiplan.components.EmailTextComponent
import com.example.maiplan.components.ErrorMessageComponent
import com.example.maiplan.components.HeadingTextComponent
import com.example.maiplan.components.PasswordTextComponent
import com.example.maiplan.components.SubmitButtonComponent
import com.example.maiplan.repository.Result
import com.example.maiplan.viewmodel.auth.AuthViewModel

/**
 * [Composable] function for the Login screen UI.
 *
 * This screen allows users to input their email and password to log into the application.
 * It provides navigation options to the [RegisterScreen] and [ForgotPasswordScreen].
 * Displays an error message if login fails.
 *
 * - Shows email and password input fields.
 * - Includes password visibility toggle.
 * - Provides links for `Forgot Password?` and `No Account? Register`.
 * - Displays login errors based on error codes.
 * - Uses the app's theme and gradient background styling.
 *
 * @param viewModel The [AuthViewModel] instance to observe login results.
 * @param onLoginClick Callback when the login button is clicked, passing email and password.
 * @param toRegisterClick Callback when the user wants to navigate to the [RegisterScreen].
 * @param toForgotPasswordClick Callback when the user wants to navigate to the [ForgotPasswordScreen].
 *
 * @see AuthViewModel
 * @see Result
 * @see ClickableTextComponent
 * @see EmailTextComponent
 * @see ErrorMessageComponent
 * @see HeadingTextComponent
 * @see PasswordTextComponent
 * @see SubmitButtonComponent
 */
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginClick: (String, String) -> Unit,
    toRegisterClick: () -> Unit,
    toForgotPasswordClick: () -> Unit
) {
    val loginResult by viewModel.loginResult.observeAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

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
                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.95f))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HeadingTextComponent(stringResource(R.string.welcome_login))

            Spacer(modifier = Modifier.height(16.dp))

            EmailTextComponent(email, onEmailChange = { email = it })

            Spacer(modifier = Modifier.height(8.dp))

            PasswordTextComponent(
                password,
                stringResource(R.string.password),
                onPasswordChange = { password = it },
                passwordVisible,
                onTogglePasswordVisibility = { passwordVisible = !passwordVisible }
                )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                ClickableTextComponent(stringResource(R.string.forgot_password), toForgotPasswordClick)
            }

            Spacer(modifier = Modifier.height(8.dp))

            SubmitButtonComponent(stringResource(R.string.login), onButtonClicked = { onLoginClick(email, password) })

            if (loginResult is Result.Failure) {
                Spacer(modifier = Modifier.height(8.dp))
                val error = loginResult as Result.Failure
                val code = error.errorCode

                val errorMessageId = when (code) {
                    1 -> R.string.general_error_1
                    2 -> R.string.general_error_2
                    3 -> R.string.general_error_3
                    4 -> R.string.general_error_4
                    8 -> R.string.login_error_8
                    else -> R.string.login_error_default
                }

                val errorMessage = stringResource(errorMessageId)
                ErrorMessageComponent(errorMessage)
            }

            Spacer(modifier = Modifier.height(8.dp))

            ClickableTextComponent(stringResource(R.string.no_account), toRegisterClick)
        }
    }
}