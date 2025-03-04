package com.example.maiplan.home.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.maiplan.home.screens.event.EventScreen
import com.example.maiplan.utils.BottomNavItem

@Composable
fun BottomNavWithScreens() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Events.route,
            Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Events.route) { EventScreen() }
            composable(BottomNavItem.Tasks.route) { TaskScreen() }
            composable(BottomNavItem.Files.route) { FileScreen() }
            composable(BottomNavItem.More.route) { MoreScreen() }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewBottomNavWithScreens() {
    BottomNavWithScreens()
}