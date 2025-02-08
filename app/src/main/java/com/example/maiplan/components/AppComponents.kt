package com.example.maiplan.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.maiplan.R

@Composable
fun HeadingTextComponent(text: String) {
    Text(
        text = text,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF4A6583)
    )
}

@Composable
fun ClickableTextComponent(text: String, onTextClicked: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Text(
        text = text,
        modifier = Modifier.clickable(
            interactionSource = interactionSource,
            indication = null
        ) { onTextClicked() },
        color = Color(0xFF4A6583),
        fontWeight = FontWeight.Medium
    )
}

@Composable
fun EmailTextComponent(email: String, onEmailChange: (String) -> Unit) {
    OutlinedTextField(
        value = email,
        onValueChange = onEmailChange,
        label = { Text(stringResource(R.string.email)) },
        leadingIcon = { Icon(Icons.Filled.Email, contentDescription = stringResource(R.string.email_icon)) },
        modifier = Modifier.fillMaxWidth(),
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = Color(0xFFFFFFFF),
            focusedContainerColor = Color(0xFFFFFFFF),
            focusedIndicatorColor = Color(0xFF4A6583),
            focusedLabelColor = Color(0xFF4A6583),
            cursorColor = Color(0xFF4A6583),
        )
    )
}

@Composable
fun UsernameTextComponent(username: String, onUsernameChange: (String) -> Unit) {
    OutlinedTextField(
        value = username,
        onValueChange = onUsernameChange,
        label = { Text(stringResource(R.string.username)) },
        leadingIcon = { Icon(Icons.Filled.AccountCircle, contentDescription = stringResource(R.string.username_icon)) },
        modifier = Modifier.fillMaxWidth(),
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = Color(0xFFFFFFFF),
            focusedContainerColor = Color(0xFFFFFFFF),
            focusedIndicatorColor = Color(0xFF4A6583),
            focusedLabelColor = Color(0xFF4A6583),
            cursorColor = Color(0xFF4A6583)
        )
    )
}

@Composable
fun PasswordTextComponent(
    password: String,
    label: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit
) {
    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text(label) },
        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = stringResource(R.string.password_icon)) },
        trailingIcon = { IconButton(onClick = onTogglePasswordVisibility) {
          Icon(imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
              contentDescription = stringResource(R.string.toggle_password_visibility))
        } },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth(),
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = Color(0xFFFFFFFF),
            focusedContainerColor = Color(0xFFFFFFFF),
            focusedIndicatorColor = Color(0xFF4A6583),
            focusedLabelColor = Color(0xFF4A6583),
            cursorColor = Color(0xFF4A6583)
        )
    )
}

@Composable
fun SubmitButtonComponent(value: String, onButtonClicked: () -> Unit) {
    Button(
        onClick = onButtonClicked,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50.dp)),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A6583))
    ) {
        Text(value, fontSize = 18.sp)
    }
}