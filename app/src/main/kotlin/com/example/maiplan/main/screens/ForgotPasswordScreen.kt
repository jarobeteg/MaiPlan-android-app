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
import com.example.maiplan.repository.Result
import com.example.maiplan.viewmodel.auth.AuthViewModel

@Composable
fun ForgotPasswordScreen(
    viewModel: AuthViewModel,
    onResetClick: (String, String, String) -> Unit,
    onBackToLogin: () -> Unit,
) {
    val resetPasswordResult by viewModel.resetPasswordResult.observeAsState()
    val isLoading = resetPasswordResult is Result.Loading
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordAgain by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var passwordAgainVisible by remember { mutableStateOf(false) }

    AuthPage(
        title = stringResource(R.string.auth_reset_title),
        subtitle = stringResource(R.string.auth_reset_subtitle),
        onBackClick = {
            viewModel.cancelResetPassword()
            onBackToLogin()
        },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            AuthEmailField(value = email, onValueChange = { email = it })
            AuthPasswordField(
                value = password,
                label = stringResource(R.string.auth_new_password),
                onValueChange = { password = it },
                passwordVisible = passwordVisible,
                onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
                showStrength = true,
            )
            AuthPasswordField(
                value = passwordAgain,
                label = stringResource(R.string.auth_confirm_new_password),
                onValueChange = { passwordAgain = it },
                passwordVisible = passwordAgainVisible,
                onTogglePasswordVisibility = { passwordAgainVisible = !passwordAgainVisible },
                imeAction = ImeAction.Done,
            )

            Spacer(Modifier.height(2.dp))
            AuthPrimaryButton(
                text = stringResource(R.string.auth_reset_action),
                onClick = { onResetClick(email, password, passwordAgain) },
                isLoading = isLoading,
            )

            if (resetPasswordResult is Result.Failure) {
                val errorCode = (resetPasswordResult as Result.Failure).errorCode
                val errorMessageId = when (errorCode) {
                    1 -> R.string.general_error_1
                    2 -> R.string.general_error_2
                    3 -> R.string.general_error_3
                    4 -> R.string.general_error_4
                    5 -> R.string.general_error_5
                    6 -> R.string.general_error_6
                    7 -> R.string.general_error_7
                    else -> R.string.reset_password_error_default
                }
                AuthErrorMessage(stringResource(errorMessageId))
            }

            AuthFooterAction(
                prompt = stringResource(R.string.auth_remembered_prompt),
                action = stringResource(R.string.auth_back_to_sign_in),
                onClick = {
                    viewModel.cancelResetPassword()
                    onBackToLogin()
                },
            )
        }
    }
}
