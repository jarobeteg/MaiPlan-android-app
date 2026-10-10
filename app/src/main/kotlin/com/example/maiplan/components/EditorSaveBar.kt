package com.example.maiplan.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.maiplan.theme.LocalAppDarkTheme
import com.example.maiplan.utils.LocalAdaptiveLayout

@Composable
internal fun EditorSaveBar(content: @Composable () -> Unit) {
    Surface(
        color = if (LocalAppDarkTheme.current) Color(0xFF191D2E) else Color.White,
        shadowElevation = 8.dp,
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().imePadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.widthIn(max = LocalAdaptiveLayout.current.formMaxWidth).fillMaxWidth()) {
                content()
            }
        }
    }
}
