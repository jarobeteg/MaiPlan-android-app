package com.example.maiplan.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Ballot
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Hail
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Reorder
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
) {
    data object Activity : BottomNavItem("Activity", Icons.Filled.Hail, "activity")
    data object Finance : BottomNavItem("Finance", Icons.Filled.Payments, "finance")
    data object Notes : BottomNavItem("Notes", Icons.Filled.Description, "notes")
    data object Lists : BottomNavItem("Notes", Icons.Filled.Ballot, "lists")
    data object More : BottomNavItem("More", Icons.Filled.Reorder, "more")
}