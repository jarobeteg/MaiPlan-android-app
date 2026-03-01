package com.example.maiplan.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
fun AdjustableSpacer(height: Dp) {
    Spacer(modifier = Modifier.height(height))
}