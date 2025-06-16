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
 * [Composable] screen for resetting the user's password.
 *
 * This screen allows the user to:
 * - Enter their email address.
 * - Enter a new password and confirm it.
 * - Submit a password reset request.
 * - Navigate back to the [LoginScreen].
 *
 * It also observes the password reset result from the [AuthViewModel]
 * and displays an error message based on error codes (from the backend server) if the reset fails.
 *
 * @param viewModel The [AuthViewModel] used to observe the password reset result.
 * @param onResetClick A lambda invoked when the user clicks the reset button. Provides email, new password, and confirmation password as parameters.
 * @param onBackToLogin A lambda invoked when the user clicks the `Return to Login` text.
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
fun ForgotPasswordScreen(
    viewModel: AuthViewModel,
    onResetClick: (String, String, String) -> Unit,
    onBackToLogin: () -> Unit
) {
    val resetPasswordResult by viewModel.resetPasswordResult.observeAsState()
    var email by remember { mutableStateOf("") }
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
                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.95f))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HeadingTextComponent(stringResource(R.string.reset_password))

            Spacer(modifier = Modifier.height(16.dp))

            EmailTextComponent(email, onEmailChange = { email = it })

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

            SubmitButtonComponent(stringResource(R.string.reset), onButtonClicked = { onResetClick(email, password, passwordAgain) })

            if (resetPasswordResult is Result.Failure) {
                Spacer(modifier = Modifier.height(8.dp))
                val error = resetPasswordResult as Result.Failure
                val code = error.errorCode

                val errorMessageId = when (code) {
                    1 -> R.string.general_error_1
                    2 -> R.string.general_error_2
                    3 -> R.string.general_error_3
                    4 -> R.string.general_error_4
                    5 -> R.string.general_error_5
                    6 -> R.string.general_error_6
                    7 -> R.string.general_error_7
                    else -> R.string.reset_password_error_default
                }

                val errorMessage = stringResource(errorMessageId)
                ErrorMessageComponent(errorMessage)
            }

            Spacer(modifier = Modifier.height(8.dp))

            ClickableTextComponent(stringResource(R.string.return_to_login), onBackToLogin)
        }
    }
}