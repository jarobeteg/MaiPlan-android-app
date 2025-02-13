package com.example.maiplan.screens.home

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.maiplan.utils.BottomNavItem

@Composable
fun BottomNavigationBar(
    navController: NavHostController
) {
    val items = listOf(
        BottomNavItem.Activity,
        BottomNavItem.Finance,
        BottomNavItem.Notes,
        BottomNavItem.Lists,
        BottomNavItem.More
    )

    NavigationBar(
        containerColor = Color(0xFF4A6583),
        contentColor = Color.White
    ) {
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF8A9DB2),
                    selectedTextColor = Color(0xFF8A9DB2),
                    unselectedIconColor = Color.White.copy(alpha = 0.7f),
                    unselectedTextColor = Color.White.copy(alpha = 0.7f),
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
