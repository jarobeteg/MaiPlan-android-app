package com.example.maiplan.main.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.maiplan.R
import com.example.maiplan.components.AuthEmailField
import com.example.maiplan.components.AuthErrorMessage
import com.example.maiplan.components.AuthFooterAction
import com.example.maiplan.components.AuthInlineAction
import com.example.maiplan.components.AuthPage
import com.example.maiplan.components.AuthPasswordField
import com.example.maiplan.components.AuthPrimaryButton
import com.example.maiplan.repository.Result
import com.example.maiplan.viewmodel.auth.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginClick: (String, String) -> Unit,
    toRegisterClick: () -> Unit,
    toForgotPasswordClick: () -> Unit,
) {
    val loginResult by viewModel.loginResult.observeAsState()
    val isLoading = loginResult is Result.Loading
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    AuthPage(
        title = stringResource(R.string.auth_login_title),
        subtitle = stringResource(R.string.auth_login_subtitle),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            AuthEmailField(value = email, onValueChange = { email = it })
            AuthPasswordField(
                value = password,
                label = stringResource(R.string.password),
                onValueChange = { password = it },
                passwordVisible = passwordVisible,
                onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
                imeAction = ImeAction.Done,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AuthInlineAction(
                    text = stringResource(R.string.forgot_password),
                    onClick = {
                        viewModel.cancelLogin()
                        toForgotPasswordClick()
                    },
                )
            }

            AuthPrimaryButton(
                text = stringResource(R.string.auth_login_action),
                onClick = { onLoginClick(email, password) },
                isLoading = isLoading,
            )

            if (loginResult is Result.Failure) {
                val errorCode = (loginResult as Result.Failure).errorCode
                val errorMessageId = when (errorCode) {
                    1 -> R.string.general_error_1
                    2 -> R.string.general_error_2
                    3 -> R.string.general_error_3
                    4 -> R.string.general_error_4
                    8 -> R.string.login_error_8
                    else -> R.string.login_error_default
                }
                AuthErrorMessage(stringResource(errorMessageId))
            }

            Spacer(Modifier.height(2.dp))
            AuthFooterAction(
                prompt = stringResource(R.string.auth_no_account_prompt),
                action = stringResource(R.string.auth_create_account_action),
                onClick = {
                    viewModel.cancelLogin()
                    toRegisterClick()
                },
            )
        }
    }
}
