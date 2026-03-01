package com.example.maiplan.main.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
fun ForgotPasswordScreen(
    viewModel: AuthViewModel,
    onResetClick: (String, String, String) -> Unit,
    onBackToLogin: () -> Unit
) {
    val ui = LocalUiScale.current

    val resetPasswordResult by viewModel.resetPasswordResult.observeAsState()
    val isLoading = resetPasswordResult is Result.Loading
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
                .then(ui.components.contentWidth)
                .padding(ui.dimensions.generalPadding)
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.95f))
                .padding(ui.dimensions.cardPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HeadingTextComponent(
                text = stringResource(R.string.reset_password)
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
                shouldIndicatorBeVisible = true
            )

            AdjustableSpacer(ui.dimensions.generalSpacer)

            PasswordTextComponent(
                passwordAgain,
                stringResource(R.string.password_again),
                onPasswordChange = { passwordAgain = it },
                passwordAgainVisible,
                onTogglePasswordVisibility = { passwordAgainVisible = !passwordAgainVisible },
            )

            AdjustableSpacer(ui.dimensions.generalSpacer * 2)

            SubmitButtonComponent(
                value = stringResource(R.string.reset),
                onButtonClicked = { onResetClick(email, password, passwordAgain) },
                isLoading = isLoading,
            )

            if (resetPasswordResult is Result.Failure) {
                AdjustableSpacer(ui.dimensions.generalSpacer)
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
                ErrorMessageComponent(
                    value = errorMessage
                )
            }

            AdjustableSpacer(ui.dimensions.generalSpacer)

            ClickableTextComponent(
                text = stringResource(R.string.return_to_login),
                onTextClicked = {
                    viewModel.cancelResetPassword()
                    onBackToLogin()
                }
            )
        }
    }
}