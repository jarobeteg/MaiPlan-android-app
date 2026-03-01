package com.example.maiplan.main.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
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
import com.example.maiplan.R
import com.example.maiplan.components.AdjustableSpacer
import com.example.maiplan.components.ClickableTextComponent
import com.example.maiplan.components.EmailTextComponent
import com.example.maiplan.components.ErrorMessageComponent
import com.example.maiplan.components.HeadingTextComponent
import com.example.maiplan.components.PasswordTextComponent
import com.example.maiplan.components.SubmitButtonComponent
import com.example.maiplan.repository.Result
import com.example.maiplan.utils.LocalUiScale
import com.example.maiplan.viewmodel.auth.AuthViewModel

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginClick: (String, String) -> Unit,
    toRegisterClick: () -> Unit,
    toForgotPasswordClick: () -> Unit
) {
    val ui = LocalUiScale.current

    val loginResult by viewModel.loginResult.observeAsState()
    val isLoading = loginResult is Result.Loading
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
                .then(ui.components.contentWidth)
                .padding(ui.dimensions.generalPadding)
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.95f))
                .padding(ui.dimensions.cardPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HeadingTextComponent(
                text = stringResource(R.string.welcome_login)
            )

            AdjustableSpacer(ui.dimensions.generalSpacer * 2)

            EmailTextComponent(
                email = email,
                onEmailChange = { email = it }
            )

            AdjustableSpacer(ui.dimensions.generalSpacer)

            PasswordTextComponent(
                password,
                stringResource(R.string.password),
                onPasswordChange = { password = it },
                passwordVisible,
                onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
                )

            AdjustableSpacer(ui.dimensions.generalSpacer)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                ClickableTextComponent(
                    text = stringResource(R.string.forgot_password),
                    onTextClicked = {
                        viewModel.cancelLogin()
                        toForgotPasswordClick()
                    }
                )
            }

            AdjustableSpacer(ui.dimensions.generalSpacer * 2)

            SubmitButtonComponent(
                value = stringResource(R.string.login),
                onButtonClicked = { onLoginClick(email, password) },
                isLoading = isLoading,
                )

            if (loginResult is Result.Failure) {
                AdjustableSpacer(ui.dimensions.generalSpacer)
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
                ErrorMessageComponent(
                    value = errorMessage
                )
            }

            AdjustableSpacer(ui.dimensions.generalSpacer)

            ClickableTextComponent(
                text = stringResource(R.string.no_account),
                onTextClicked = {
                    viewModel.cancelLogin()
                    toRegisterClick()
                }
            )
        }
    }
}