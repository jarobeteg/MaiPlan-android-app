package com.example.maiplan.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.example.maiplan.utils.LocalUiScale

@Composable
fun SubmitButtonComponent(
    value: String,
    color: Color = MaterialTheme.colorScheme.primary,
    onButtonClicked: () -> Unit,
    isLoading: Boolean = false,
) {
    val ui = LocalUiScale.current

    Button(
        onClick = { if (!isLoading) onButtonClicked() },
        enabled = !isLoading,
        modifier = Modifier.fillMaxWidth().height(ui.components.generalFieldHeight),
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
                fontSize = ui.fonts.generalTextSize
            )
        }
    }
}

@Composable
fun ErrorMessageComponent(
    value: String,
) {
    val ui = LocalUiScale.current

    Text(
        text = value,
        color = MaterialTheme.colorScheme.onError,
        fontSize = ui.fonts.generalTextSize,
        fontWeight = FontWeight.Bold,
        style = ui.typographies.generalTextStyle
    )
}