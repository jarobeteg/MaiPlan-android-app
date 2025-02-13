package com.example.maiplan.screens.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.maiplan.utils.BottomNavItem
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun BottomNavWithScreens() {
    val systemUiController = rememberSystemUiController()

    SideEffect {
        systemUiController.setStatusBarColor(color = Color.Transparent, darkIcons = false)
    }

    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Activity.route,
            Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Activity.route) { ActivityScreen() }
            composable(BottomNavItem.Finance.route) { FinanceScreen() }
            composable(BottomNavItem.Notes.route) { NoteScreen() }
            composable(BottomNavItem.Lists.route) { ListScreen() }
            composable(BottomNavItem.More.route) { MoreScreen() }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewBottomNavWithScreens() {
    BottomNavWithScreens()
}

