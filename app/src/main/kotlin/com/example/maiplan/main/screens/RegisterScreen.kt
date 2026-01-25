package com.example.maiplan.main.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    activity: Activity,
    onRegisterClick: (String, String, String, String) -> Unit,
    onBackToLogin: () -> Unit
) {
    val registerResult by viewModel.registerResult.observeAsState()
    val isLoading = registerResult is Result.Loading
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordAgain by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var passwordAgainVisible by remember { mutableStateOf(false) }

    val windowSizeClass = calculateWindowSizeClass(activity)

    val isCompact = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact

    val cardPadding = if (isCompact) 24.dp else 48.dp
    val itemSpacing = if (isCompact) 12.dp else 20.dp
    val scale = if (isCompact) 1f else 1.5f
    val fieldHeight = if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded) 96.dp else 64.dp

    val contentWidth = when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> Modifier.fillMaxWidth()
        WindowWidthSizeClass.Medium -> Modifier.widthIn(min = 500.dp, max = 700.dp)
        WindowWidthSizeClass.Expanded -> Modifier.fillMaxWidth(0.7f)
        else -> Modifier.fillMaxWidth()
    }

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
                .then(contentWidth)
                .padding(if (isCompact) 16.dp else 0.dp)
                .clip(MaterialTheme.shapes.large)
                .background(Color.White.copy(alpha = 0.95f))
                .padding(cardPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HeadingTextComponent(
                text = stringResource(R.string.create_an_account),
                fontSize = if (isCompact) 24.sp else 24.sp * 2.5f,
                style = if (isCompact) MaterialTheme.typography.displaySmall else MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(itemSpacing * 2))

            EmailTextComponent(
                email = email,
                modifier = Modifier.fillMaxWidth().height(fieldHeight),
                fontSize = if (isCompact) 18.sp else 24.sp * 1.75f,
                style = if (isCompact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelLarge,
                iconSize = if (isCompact) 24.dp else 42.dp,
                onEmailChange = { email = it }
            )

            Spacer(modifier = Modifier.height(itemSpacing))

            UsernameTextComponent(
                username,
                modifier = Modifier.fillMaxWidth().height(fieldHeight),
                fontSize = if (isCompact) 18.sp else 24.sp * 1.75f,
                style = if (isCompact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelLarge,
                iconSize = if (isCompact) 24.dp else 42.dp,
                onUsernameChange = { username = it })

            Spacer(modifier = Modifier.height(itemSpacing))

            PasswordTextComponent(
                password,
                stringResource(R.string.password),
                onPasswordChange = { password = it },
                passwordVisible,
                onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
                fontSize = if (isCompact) 18.sp else 24.sp * 1.75f,
                style = if (isCompact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelLarge,
                iconSize = if (isCompact) 24.dp else 42.dp,
                modifier = Modifier.fillMaxWidth().height(fieldHeight),
                isCompact = isCompact,
                shouldIndicatorBeVisible = true
            )

            Spacer(modifier = Modifier.height(itemSpacing))

            PasswordTextComponent(
                passwordAgain,
                stringResource(R.string.password_again),
                onPasswordChange = { passwordAgain = it },
                passwordAgainVisible,
                onTogglePasswordVisibility = { passwordAgainVisible = !passwordAgainVisible },
                fontSize = if (isCompact) 18.sp else 24.sp * 1.75f,
                style = if (isCompact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelLarge,
                iconSize = if (isCompact) 24.dp else 42.dp,
                modifier = Modifier.fillMaxWidth().height(fieldHeight)
            )

            Spacer(modifier = Modifier.height(itemSpacing * 2))

            SubmitButtonComponent(
                value = stringResource(R.string.register),
                onButtonClicked = { onRegisterClick(email, username, password, passwordAgain) },
                isLoading = isLoading,
                fontSize = if (isCompact) 18.sp else 24.sp * 1.75f,
                modifier = Modifier.fillMaxWidth().height(fieldHeight)
            )

            if (registerResult is Result.Failure) {
                Spacer(modifier = Modifier.height(itemSpacing))
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
                ErrorMessageComponent(
                    value = errorMessage,
                    fontSize = if (isCompact) 16.sp else 24.sp * 1.5f,
                    style = if (isCompact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelLarge,
                )
            }

            Spacer(modifier = Modifier.height(itemSpacing))

            ClickableTextComponent(
                text = stringResource(R.string.have_account),
                fontSize = if (isCompact) 18.sp else 24.sp * 1.75f,
                onTextClicked = {
                    viewModel.cancelRegister()
                    onBackToLogin()
                }
            )
        }
    }
}