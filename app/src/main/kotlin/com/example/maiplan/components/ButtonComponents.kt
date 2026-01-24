package com.example.maiplan.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SubmitButtonComponent(
    value: String,
    color: Color = MaterialTheme.colorScheme.primary,
    onButtonClicked: () -> Unit,
    isLoading: Boolean = false
) {
    Button(
        onClick = { if (!isLoading) onButtonClicked() },
        enabled = !isLoading,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraSmall,
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp,
                modifier = Modifier
                    .size(32.dp)
                    .padding(2.dp)
            )
        } else {
            Text(
                text = value,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun ErrorMessageComponent(value: String) {
    Text(
        text = value,
        color = MaterialTheme.colorScheme.onError,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    )
}