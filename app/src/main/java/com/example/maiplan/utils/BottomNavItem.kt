package com.example.maiplan.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Ballot
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Hail
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Reorder
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.maiplan.R

sealed class BottomNavItem(
    val labelResId: Int,
    val icon: ImageVector,
    val route: String
) {
    data object Activity : BottomNavItem(R.string.activity, Icons.Filled.Hail, "activity")
    data object Finance : BottomNavItem(R.string.finance, Icons.Filled.Payments, "finance")
    data object Notes : BottomNavItem(R.string.notes, Icons.Filled.Description, "notes")
    data object Lists : BottomNavItem(R.string.lists, Icons.Filled.Ballot, "lists")
    data object More : BottomNavItem(R.string.more, Icons.Filled.Reorder, "more")
}