package com.example.maiplan.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.maiplan.R

@Composable
fun HeadingTextComponent(
    text: String,
    fontSize: TextUnit,
    style: TextStyle
) {
    Text(
        text = text,
        fontSize = fontSize,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        style = style
    )
}

@Composable
fun ClickableTextComponent(
    text: String,
    fontSize: TextUnit = 18.sp,
    onTextClicked: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Text(
        text = text,
        modifier = Modifier.clickable(
            interactionSource = interactionSource,
            indication = null
        ) { onTextClicked() },
        color = MaterialTheme.colorScheme.primary,
        fontSize = fontSize,
        fontWeight = FontWeight.Medium
    )
}

@Composable
fun EmailTextComponent(
    email: String,
    modifier: Modifier,
    fontSize: TextUnit,
    style: TextStyle,
    iconSize: Dp,
    onEmailChange: (String) -> Unit
) {
    OutlinedTextField(
        value = email,
        onValueChange = { newEmail ->
            if (newEmail.length <= 64) {
                onEmailChange(newEmail.filter { !it.isWhitespace() })
            }
        },
        label = { Text(
            text = stringResource(R.string.email),
            fontSize = fontSize,
            style = style,
            textAlign = TextAlign.Center
        ) },
        textStyle = TextStyle(fontSize = fontSize),
        leadingIcon = { Icon(
            imageVector = Icons.Filled.Email,
            modifier = Modifier.size(iconSize),
            contentDescription = stringResource(R.string.email_icon)
        ) },
        modifier = modifier,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.onPrimary,
            unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary,
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            cursorColor = MaterialTheme.colorScheme.primary,
        ),
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Email,
            autoCorrectEnabled = false
        )
    )
}

@Composable
fun UsernameTextComponent(
    username: String,
    modifier: Modifier,
    fontSize: TextUnit,
    style: TextStyle,
    iconSize: Dp,
    onUsernameChange: (String) -> Unit) {
    OutlinedTextField(
        value = username,
        onValueChange = { newUsername ->
            if (newUsername.length <= 32) {
                onUsernameChange(newUsername.filter { !it.isWhitespace() })
            }
        },
        label = { Text(
            text = stringResource(R.string.username),
            fontSize = fontSize,
            style = style,
            textAlign = TextAlign.Center
        ) },
        textStyle = TextStyle(fontSize = fontSize),
        leadingIcon = { Icon(
            imageVector = Icons.Filled.AccountCircle,
            modifier = Modifier.size(iconSize),
            contentDescription = stringResource(R.string.username_icon)) },
        modifier = modifier,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.onPrimary,
            unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary,
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            cursorColor = MaterialTheme.colorScheme.primary
        ),
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Text,
            autoCorrectEnabled = false
        )
    )
}

@Composable
fun PasswordTextComponent(
    password: String,
    label: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    modifier: Modifier,
    fontSize: TextUnit,
    style: TextStyle,
    iconSize: Dp,
    isCompact: Boolean = true,
    shouldIndicatorBeVisible: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Column {
        OutlinedTextField(
            value = password,
            onValueChange = { newPassword ->
                if (newPassword.length <= 64) {
                    onPasswordChange(newPassword.filter { !it.isWhitespace() })
                }
            },
            label = { Text(
                text = label,
                fontSize = fontSize,
                style = style,
                textAlign = TextAlign.Center
            ) },
            textStyle = TextStyle(fontSize = fontSize),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    modifier = Modifier.size(iconSize),
                    contentDescription = stringResource(R.string.password_icon)
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = onTogglePasswordVisibility,
                    modifier = Modifier.size(iconSize).offset(x = (-8).dp)
                ) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        modifier = Modifier.fillMaxSize(),
                        contentDescription = stringResource(R.string.toggle_password_visibility)
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = modifier,
            interactionSource = interactionSource,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.onPrimary,
                unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Password,
                autoCorrectEnabled = false
            )
        )

        val height: Dp = if (isCompact) 8.dp else 16.dp
        val fontSize: TextUnit = if (isCompact) 12.sp else 22.sp

        if (shouldIndicatorBeVisible) {
            PasswordStrengthBar(password, isFocused, height, fontSize)
        } else {
            PasswordStrengthBar(password, false, height, fontSize)
        }
    }
}

@Composable
fun PasswordStrengthBar(
    password: String,
    isFocused: Boolean,
    height: Dp,
    fontSize: TextUnit,
) {
    if (isFocused) {
        Spacer(modifier = Modifier.height(height))

        val hasMinLength = password.length >= 8
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecialChar = password.any { it in "!_@#$?" }

        val rawScore = listOf(
            hasMinLength,
            hasLowerCase,
            hasUpperCase,
            hasDigit,
            hasSpecialChar
        ).count { it }

        val score = (rawScore - 1).coerceAtLeast(0)


        val strengthColors = listOf(
            Color(0xFFD32F2F),
            Color(0xFFF57C00),
            Color(0xFFFBC02D),
            Color(0xFF388E3C),
            Color(0xFF1976D2)
        )

        val strengthLabels = listOf(
            stringResource(R.string.very_weak),
            stringResource(R.string.weak),
            stringResource(R.string.medium),
            stringResource(R.string.strong),
            stringResource(R.string.very_strong)
        )

        Column(modifier = Modifier.fillMaxWidth()) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .clip(MaterialTheme.shapes.extraSmall)
            ) {
                val progress = (size.width * (score / 4f)).coerceIn(0f, size.width)
                drawRect(
                    color = Color(0xFFB0BEC5),
                    size = size
                )
                drawRect(
                    color = strengthColors[score.coerceIn(0, 4)],
                    size = androidx.compose.ui.geometry.Size(progress, size.height)
                )
            }
            Text(
                text = strengthLabels[score.coerceIn(0, 4)],
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                color = strengthColors[score.coerceIn(0, 4)],
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun AdjustableTextFieldLengthComponent(value: String, label: String, icon: ImageVector, length: Int, onValueChange: (String) -> Unit) {
    val isTablet = isTablet()

    val fontSize = if (isTablet) 24.sp else 16.sp
    val style = if (isTablet) MaterialTheme.typography.labelLarge else MaterialTheme.typography.labelSmall
    val iconSize = if (isTablet) 36.dp else 24.dp
    val fieldHeight = if (isTablet) 72.dp else 64.dp

    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            if (newValue.length <= length) {
                onValueChange(newValue)
            }
        },
        label = { Text(
            text = label,
            fontSize = fontSize,
            style = style
        ) },
        trailingIcon = {
            Box(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(iconSize),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        },
        textStyle = TextStyle(fontSize = fontSize),
        modifier = Modifier.fillMaxWidth().height(fieldHeight),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.background,
            unfocusedContainerColor = MaterialTheme.colorScheme.background,
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.onBackground,
            focusedTextColor = MaterialTheme.colorScheme.onBackground,
            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
            focusedLabelColor = MaterialTheme.colorScheme.onBackground,
            cursorColor = MaterialTheme.colorScheme.primary
        ),
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Text,
            autoCorrectEnabled = false
        )
    )
}

@Composable
fun SearchFieldComponent(searchQuery: String, length: Int, onValueChange: (String) -> Unit) {
    val isTablet = isTablet()

    val fontSize = if (isTablet) 24.sp else 18.sp
    val style = if (isTablet) MaterialTheme.typography.labelLarge else MaterialTheme.typography.labelSmall
    val iconSize = if (isTablet) 32.dp else 24.dp
    val fieldHeight = if (isTablet) 72.dp else 64.dp

    OutlinedTextField(
        value = searchQuery,
        onValueChange = { newValue ->
            if (newValue.length <= length) {
                onValueChange(newValue)
            }
        },
        textStyle = TextStyle(fontSize = fontSize),
        modifier = Modifier.fillMaxWidth().height(fieldHeight),
        placeholder = { Text(
            text = stringResource(R.string.category_search),
            fontSize = fontSize,
            style = style,
            textAlign = TextAlign.Center
        ) },
        leadingIcon = { Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = null,
            modifier = Modifier.size(iconSize)
        ) },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.background,
            unfocusedContainerColor = MaterialTheme.colorScheme.background,
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.onBackground,
            focusedTextColor = MaterialTheme.colorScheme.onBackground,
            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
            focusedLabelColor = MaterialTheme.colorScheme.onBackground,
            cursorColor = MaterialTheme.colorScheme.primary
        )
    )
}