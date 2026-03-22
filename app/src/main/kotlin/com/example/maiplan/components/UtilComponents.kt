package com.example.maiplan.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import com.example.maiplan.R

@Composable
fun AdjustableSpacer(height: Dp) {
    Spacer(modifier = Modifier.height(height))
}

@Composable
fun getMonthText(monthNumber: Int): String {
    return when(monthNumber) {
        1 -> stringResource(R.string.january)
        2 -> stringResource(R.string.february)
        3 -> stringResource(R.string.march)
        4 -> stringResource(R.string.april)
        5 -> stringResource(R.string.may)
        6 -> stringResource(R.string.june)
        7 -> stringResource(R.string.july)
        8 -> stringResource(R.string.august)
        9 -> stringResource(R.string.september)
        10 -> stringResource(R.string.october)
        11 -> stringResource(R.string.november)
        12 -> stringResource(R.string.december)
        else -> stringResource(R.string.january)
    }
}