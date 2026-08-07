package com.example.maiplan.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import com.example.maiplan.utils.LocalAppDesign

@Composable
fun SubmitButtonComponent(
    value: String,
    color: Color = MaterialTheme.colorScheme.primary,
    onButtonClicked: () -> Unit,
    isLoading: Boolean = false,
) {
    Button(
        onClick = { if (!isLoading) onButtonClicked() },
        enabled = !isLoading,
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
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
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
fun ErrorMessageComponent(
    value: String,
) {
    Text(
        text = value,
        color = MaterialTheme.colorScheme.onError,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.bodyLarge,
    )
}
