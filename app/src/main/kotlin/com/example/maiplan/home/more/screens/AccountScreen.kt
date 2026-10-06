package com.example.maiplan.home.more.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.maiplan.R
import com.example.maiplan.components.AuthErrorMessage
import com.example.maiplan.components.AuthPasswordField
import com.example.maiplan.components.AuthPrimaryButton
import com.example.maiplan.components.AuthUsernameField
import com.example.maiplan.repository.Result
import com.example.maiplan.theme.LocalAppDarkTheme
import com.example.maiplan.utils.BaseActivity
import com.example.maiplan.utils.common.UserSession
import com.example.maiplan.viewmodel.auth.AccountViewModel
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(accountViewModel: AccountViewModel, onBackClick: () -> Unit) {
    val dark = LocalAppDarkTheme.current
    val foreground = if (dark) Color(0xFFF5F7FB) else Color(0xFF172033)
    val muted = if (dark) Color(0xFFAEB7C9) else Color(0xFF667085)
    val usernameResult by accountViewModel.usernameResult.observeAsState(Result.Idle)
    val passwordResult by accountViewModel.passwordResult.observeAsState(Result.Idle)
    val busy = usernameResult is Result.Loading || passwordResult is Result.Loading
    var username by rememberSaveable(UserSession.userSyncId) {
        mutableStateOf(UserSession.username.orEmpty())
    }
    var password by remember { mutableStateOf("") }
    var passwordAgain by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var passwordAgainVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(usernameResult) {
        if (usernameResult is Result.Success) username = UserSession.username.orEmpty()
    }
    LaunchedEffect(passwordResult) {
        if (passwordResult is Result.Success) {
            password = ""
            passwordAgain = ""
            passwordVisible = false
            passwordAgainVisible = false
            focusManager.clearFocus()
            keyboard?.hide()
        }
    }

    Scaffold(
        containerColor = if (dark) Color(0xFF101321) else Color(0xFFF4F6FC),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.account_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (dark) Color(0xFF191D2E) else Color.White,
                    titleContentColor = foreground,
                    navigationIconContentColor = foreground,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).imePadding()
                .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier.widthIn(max = 520.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Column {
                    Text(
                        UserSession.username.orEmpty(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = foreground,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(UserSession.email.orEmpty(), style = MaterialTheme.typography.bodyMedium, color = muted)
                }
                AccountCard(R.string.account_username_title, R.string.account_username_subtitle) {
                    AuthUsernameField(
                        value = username,
                        onValueChange = { username = it; accountViewModel.clearUsernameResult() },
                        imeAction = ImeAction.Done,
                        enabled = !busy,
                    )
                    AuthPrimaryButton(
                        text = stringResource(R.string.account_save_username),
                        onClick = { accountViewModel.changeUsername(username) },
                        isLoading = usernameResult is Result.Loading,
                        enabled = !busy && username != UserSession.username,
                    )
                    AccountResultFeedback(usernameResult, R.string.account_username_saved)
                }
                AccountCard(R.string.account_password_title, R.string.account_password_subtitle) {
                    AuthPasswordField(
                        value = password,
                        label = stringResource(R.string.auth_new_password),
                        onValueChange = { password = it; accountViewModel.clearPasswordResult() },
                        passwordVisible = passwordVisible,
                        onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
                        showStrength = true,
                        enabled = !busy,
                    )
                    AuthPasswordField(
                        value = passwordAgain,
                        label = stringResource(R.string.auth_confirm_new_password),
                        onValueChange = { passwordAgain = it; accountViewModel.clearPasswordResult() },
                        passwordVisible = passwordAgainVisible,
                        onTogglePasswordVisibility = { passwordAgainVisible = !passwordAgainVisible },
                        imeAction = ImeAction.Done,
                        enabled = !busy,
                    )
                    AuthPrimaryButton(
                        text = stringResource(R.string.account_save_password),
                        onClick = { accountViewModel.changePassword(password, passwordAgain) },
                        isLoading = passwordResult is Result.Loading,
                        enabled = !busy,
                    )
                    AccountResultFeedback(passwordResult, R.string.account_password_saved)
                }
            }
        }
    }
}

@Composable
private fun AccountCard(title: Int, subtitle: Int, content: @Composable ColumnScope.() -> Unit) {
    val dark = LocalAppDarkTheme.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = if (dark) Color(0xFF191D2E) else Color.White,
        border = BorderStroke(1.dp, if (dark) Color(0xFF30374D) else Color(0xFFDDE3EC)),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    stringResource(title), style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (dark) Color(0xFFF5F7FB) else Color(0xFF172033),
                )
                Text(
                    stringResource(subtitle), style = MaterialTheme.typography.bodyMedium,
                    color = if (dark) Color(0xFFAEB7C9) else Color(0xFF667085),
                )
            }
            content()
        }
    }
}

@Composable
private fun AccountResultFeedback(result: Result<*>, successMessage: Int) {
    val context = LocalContext.current
    val sessionExpired = result is Result.Failure && result.httpStatus in listOf(401, 403)
    val errorMessage = when (result) {
        is Result.Failure -> if (sessionExpired) R.string.account_session_expired else when (result.errorCode) {
            4 -> R.string.general_error_4
            5 -> R.string.general_error_5
            6 -> R.string.general_error_6
            7 -> R.string.general_error_7
            9 -> R.string.register_error_9
            10 -> R.string.register_error_10
            else -> R.string.account_update_failed
        }
        is Result.Error -> if (result.exception is IOException) R.string.server_unreachable_try_again
            else R.string.account_update_failed
        else -> null
    }
    if (result is Result.Success) {
        Text(
            stringResource(successMessage), color = Color(0xFF14B8A6),
            style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold,
        )
    }
    if (errorMessage != null) AuthErrorMessage(stringResource(errorMessage))
    if (sessionExpired) {
        TextButton(onClick = { (context as? BaseActivity)?.logout() }) {
            Text(stringResource(R.string.auth_sign_in_action))
        }
    }
}
