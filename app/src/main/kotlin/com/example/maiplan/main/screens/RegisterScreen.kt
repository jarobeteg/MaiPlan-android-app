package com.example.maiplan.main.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.maiplan.R
import com.example.maiplan.components.AuthEmailField
import com.example.maiplan.components.AuthErrorMessage
import com.example.maiplan.components.AuthFooterAction
import com.example.maiplan.components.AuthPage
import com.example.maiplan.components.AuthPasswordField
import com.example.maiplan.components.AuthPrimaryButton
import com.example.maiplan.components.AuthUsernameField
import com.example.maiplan.repository.Result
import com.example.maiplan.viewmodel.auth.AuthViewModel

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegisterClick: (String, String, String, String) -> Unit,
    onBackToLogin: () -> Unit,
) {
    val registerResult by viewModel.registerResult.observeAsState()
    val isLoading = registerResult is Result.Loading
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordAgain by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var passwordAgainVisible by remember { mutableStateOf(false) }

    AuthPage(
        title = stringResource(R.string.auth_register_title),
        subtitle = stringResource(R.string.auth_register_subtitle),
        onBackClick = {
            viewModel.cancelRegister()
            onBackToLogin()
        },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            AuthEmailField(value = email, onValueChange = { email = it })
            AuthUsernameField(value = username, onValueChange = { username = it })
            AuthPasswordField(
                value = password,
                label = stringResource(R.string.password),
                onValueChange = { password = it },
                passwordVisible = passwordVisible,
                onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
                showStrength = true,
            )
            AuthPasswordField(
                value = passwordAgain,
                label = stringResource(R.string.auth_confirm_password),
                onValueChange = { passwordAgain = it },
                passwordVisible = passwordAgainVisible,
                onTogglePasswordVisibility = { passwordAgainVisible = !passwordAgainVisible },
                imeAction = ImeAction.Done,
            )

            Spacer(Modifier.height(2.dp))
            AuthPrimaryButton(
                text = stringResource(R.string.auth_register_action),
                onClick = { onRegisterClick(email, username, password, passwordAgain) },
                isLoading = isLoading,
            )

            if (registerResult is Result.Failure) {
                val errorCode = (registerResult as Result.Failure).errorCode
                val errorMessageId = when (errorCode) {
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
                AuthErrorMessage(stringResource(errorMessageId))
            }

            AuthFooterAction(
                prompt = stringResource(R.string.auth_have_account_prompt),
                action = stringResource(R.string.auth_sign_in_action),
                onClick = {
                    viewModel.cancelRegister()
                    onBackToLogin()
                },
            )
        }
    }
}
