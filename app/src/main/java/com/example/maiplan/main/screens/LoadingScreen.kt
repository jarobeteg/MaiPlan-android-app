package com.example.maiplan.main.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * A simple loading screen displaying a centered [CircularProgressIndicator].
 *
 * This composable is typically shown while the app is performing background operations
 * such as authentication, data loading, or network requests.
 *
 * - Fills the entire available screen space.
 * - Sets the background color to white.
 * - Centers a primary-colored [CircularProgressIndicator] in the middle of the screen.
 */
@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}