package com.example.maiplan.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import com.example.maiplan.theme.LocalAppDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MailOutline
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.maiplan.R
import com.example.maiplan.theme.AppThemeManager

private val AuthPrimary: Color get() = AppThemeManager.selectedTheme.primary
private val AuthPrimaryLight: Color get() = AppThemeManager.selectedTheme.primaryLight
private val AuthTeal = Color(0xFF14B8A6)
private val AuthInk = Color(0xFF172033)
private val AuthMuted = Color(0xFF667085)
private val AuthField = Color(0xFFF8FAFC)
private val AuthBorder = Color(0xFFDDE3EC)

/** A self-contained visual system for the authentication journey. */
@Composable
fun AuthPage(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val dark = LocalAppDarkTheme.current
    val background = if (dark) Color(0xFF101321) else Color(0xFFF4F6FC)
    val card = if (dark) Color(0xFF191D2E) else Color.White
    val titleColor = if (dark) Color(0xFFF6F7FB) else AuthInk
    val mutedColor = if (dark) Color(0xFFAEB7C9) else AuthMuted

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(background)
            .statusBarsPadding()
            .imePadding(),
    ) {
        AuthBackground(dark = dark)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (onBackClick != null) {
                        Surface(
                            shape = CircleShape,
                            color = card.copy(alpha = 0.92f),
                            shadowElevation = 2.dp,
                        ) {
                            IconButton(onClick = onBackClick) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = stringResource(R.string.back),
                                    tint = titleColor,
                                )
                            }
                        }
                    } else {
                        Spacer(Modifier.size(48.dp))
                    }

                    Spacer(Modifier.weight(1f))
                    AuthBrandMark()
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = titleColor,
                        letterSpacing = (-0.4).sp,
                    )
                    Spacer(Modifier.weight(1f))
                    Spacer(Modifier.size(48.dp))
                }

                Spacer(Modifier.height(28.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    color = card.copy(alpha = 0.97f),
                    shadowElevation = if (dark) 0.dp else 10.dp,
                    tonalElevation = 2.dp,
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = titleColor,
                            textAlign = TextAlign.Center,
                            letterSpacing = (-0.6).sp,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = mutedColor,
                            textAlign = TextAlign.Center,
                            lineHeight = 21.sp,
                        )
                        Spacer(Modifier.height(26.dp))
                        content()
                    }
                }

                Spacer(Modifier.height(24.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = AuthTeal,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(7.dp))
                    Text(
                        text = stringResource(R.string.auth_secure_note),
                        style = MaterialTheme.typography.labelMedium,
                        color = mutedColor,
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthBackground(dark: Boolean) {
    val first = AuthPrimary.copy(alpha = if (dark) 0.20f else 0.11f)
    val second = AuthTeal.copy(alpha = if (dark) 0.13f else 0.09f)
    Canvas(Modifier.fillMaxSize()) {
        drawCircle(
            brush = Brush.radialGradient(listOf(first, Color.Transparent)),
            radius = size.minDimension * 0.72f,
            center = Offset(size.width * 0.05f, size.height * 0.04f),
        )
        drawCircle(
            brush = Brush.radialGradient(listOf(second, Color.Transparent)),
            radius = size.minDimension * 0.62f,
            center = Offset(size.width * 0.94f, size.height * 0.92f),
        )
    }
}

@Composable
private fun AuthBrandMark() {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(Brush.linearGradient(listOf(AuthPrimary, AuthPrimaryLight))),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.CalendarMonth,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(23.dp),
        )
    }
}

@Composable
fun AuthEmailField(
    value: String,
    onValueChange: (String) -> Unit,
    imeAction: ImeAction = ImeAction.Next,
) {
    AuthTextField(
        value = value,
        onValueChange = { newValue ->
            if (newValue.length <= 64) onValueChange(newValue.filterNot(Char::isWhitespace))
        },
        label = stringResource(R.string.email),
        icon = Icons.Rounded.MailOutline,
        keyboardType = KeyboardType.Email,
        imeAction = imeAction,
    )
}

@Composable
fun AuthUsernameField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    AuthTextField(
        value = value,
        onValueChange = { newValue ->
            if (newValue.length <= 32) onValueChange(newValue.filterNot(Char::isWhitespace))
        },
        label = stringResource(R.string.username),
        icon = Icons.Rounded.PersonOutline,
        keyboardType = KeyboardType.Text,
        imeAction = ImeAction.Next,
    )
}

@Composable
fun AuthPasswordField(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    passwordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    imeAction: ImeAction = ImeAction.Next,
    showStrength: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Column(Modifier.animateContentSize()) {
        AuthTextField(
            value = value,
            onValueChange = { newValue ->
                if (newValue.length <= 64) onValueChange(newValue.filterNot(Char::isWhitespace))
            },
            label = label,
            icon = Icons.Rounded.Lock,
            keyboardType = KeyboardType.Password,
            imeAction = imeAction,
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            interactionSource = interactionSource,
            trailingIcon = {
                IconButton(onClick = onTogglePasswordVisibility) {
                    Icon(
                        imageVector = if (passwordVisible) {
                            Icons.Rounded.VisibilityOff
                        } else {
                            Icons.Rounded.Visibility
                        },
                        contentDescription = stringResource(R.string.toggle_password_visibility),
                    )
                }
            },
        )

        AnimatedVisibility(visible = showStrength && isFocused) {
            PasswordStrengthIndicator(value)
        }
    }
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    val dark = LocalAppDarkTheme.current
    val fieldColor = if (dark) Color(0xFF20263A) else AuthField
    val borderColor = if (dark) Color(0xFF3A435C) else AuthBorder
    val textColor = if (dark) Color(0xFFF4F6FA) else AuthInk
    val labelColor = if (dark) Color(0xFFADB6C8) else AuthMuted

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        shape = RoundedCornerShape(15.dp),
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(21.dp),
            )
        },
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        interactionSource = interactionSource,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction,
            autoCorrectEnabled = false,
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = fieldColor,
            unfocusedContainerColor = fieldColor,
            focusedBorderColor = AuthPrimary,
            unfocusedBorderColor = borderColor,
            focusedLabelColor = AuthPrimary,
            unfocusedLabelColor = labelColor,
            focusedLeadingIconColor = AuthPrimary,
            unfocusedLeadingIconColor = labelColor,
            focusedTrailingIconColor = labelColor,
            unfocusedTrailingIconColor = labelColor,
            focusedTextColor = textColor,
            unfocusedTextColor = textColor,
            cursorColor = AuthPrimary,
        ),
    )
}

@Composable
private fun PasswordStrengthIndicator(password: String) {
    val checks = listOf(
        password.length >= 8,
        password.any(Char::isLowerCase) && password.any(Char::isUpperCase),
        password.any(Char::isDigit),
        password.any { it in "!_@#$?" },
    )
    val score = checks.count { it }
    val strengthColor = when (score) {
        0, 1 -> Color(0xFFEF4444)
        2 -> Color(0xFFF59E0B)
        3 -> AuthTeal
        else -> Color(0xFF22C55E)
    }
    val label = when (score) {
        0, 1 -> stringResource(R.string.weak)
        2 -> stringResource(R.string.medium)
        3 -> stringResource(R.string.strong)
        else -> stringResource(R.string.very_strong)
    }

    Column(Modifier.padding(top = 10.dp, start = 4.dp, end = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            repeat(4) { index ->
                Canvas(
                    Modifier
                        .weight(1f)
                        .height(4.dp),
                ) {
                    drawLine(
                        color = if (index < score) strengthColor else AuthBorder,
                        start = Offset(0f, size.height / 2),
                        end = Offset(size.width, size.height / 2),
                        strokeWidth = size.height,
                        cap = StrokeCap.Round,
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.password_strength, label),
            style = MaterialTheme.typography.labelSmall,
            color = strengthColor,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun AuthPrimaryButton(
    text: String,
    onClick: () -> Unit,
    isLoading: Boolean,
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(15.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AuthPrimary,
            contentColor = Color.White,
            disabledContainerColor = AuthPrimary.copy(alpha = 0.64f),
            disabledContentColor = Color.White,
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Color.White,
                strokeWidth = 2.5.dp,
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.width(9.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
fun AuthErrorMessage(message: String) {
    val dark = LocalAppDarkTheme.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(13.dp),
        color = if (dark) Color(0xFF3B2027) else Color(0xFFFFF1F2),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.ErrorOutline,
                contentDescription = null,
                tint = Color(0xFFE5484D),
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = if (dark) Color(0xFFFFB4B8) else Color(0xFFA61B29),
            )
        }
    }
}

@Composable
fun AuthInlineAction(text: String, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(
            text = text,
            color = AuthPrimary,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun AuthFooterAction(
    prompt: String,
    action: String,
    onClick: () -> Unit,
) {
    HorizontalDivider(color = AuthBorder.copy(alpha = 0.72f))
    Spacer(Modifier.height(14.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = prompt,
            style = MaterialTheme.typography.bodyMedium,
            color = if (LocalAppDarkTheme.current) Color(0xFFAEB7C9) else AuthMuted,
        )
        TextButton(onClick = onClick) {
            Text(
                text = action,
                color = AuthPrimary,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
