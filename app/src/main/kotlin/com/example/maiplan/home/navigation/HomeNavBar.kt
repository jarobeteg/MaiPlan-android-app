package com.example.maiplan.home.navigation

import android.content.Context
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.maiplan.components.isTablet

@Composable
fun HomeNavigationBar(navController: NavHostController, context: Context) {
    val items = listOf(
        HomeNavRoutes.Events,
        HomeNavRoutes.Tasks,
        HomeNavRoutes.Files,
        HomeNavRoutes.More
    )
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    val isTablet = isTablet()
    val iconSize = if (isTablet) 48.dp else 24.dp
    val fontSize = if (isTablet) 32.sp else 12.sp
    val barHeight = if (isTablet) 192.dp else 96.dp

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.height(barHeight)
    ) {
        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(
                    imageVector = screen.icon,
                    modifier = Modifier.size(iconSize),
                    contentDescription = context.getString(screen.labelResId)
                ) },
                label = { Text(
                    text = context.getString(screen.labelResId),
                    fontSize = fontSize
                ) },
                selected = currentRoute == screen.route,
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.surfaceContainer,
                    selectedTextColor = MaterialTheme.colorScheme.surfaceContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                    unselectedTextColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}