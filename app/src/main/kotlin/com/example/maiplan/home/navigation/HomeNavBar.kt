package com.example.maiplan.home.navigation

import android.content.Context
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.unit.dp
import com.example.maiplan.utils.LocalAdaptiveLayout

private val homeNavigationItems = listOf(
    HomeNavRoutes.Events,
    HomeNavRoutes.Tasks,
    HomeNavRoutes.Notes,
    HomeNavRoutes.More,
)

@Composable
fun HomeNavigationBar(navController: NavHostController, context: Context) {
    if (LocalAdaptiveLayout.current.useNavigationRail) return

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
        homeNavigationItems.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(
                    imageVector = screen.icon,
                    modifier = Modifier.size(24.dp),
                    contentDescription = context.getString(screen.labelResId)
                ) },
                label = { Text(
                    text = context.getString(screen.labelResId),
                    style = MaterialTheme.typography.labelMedium,
                ) },
                selected = currentRoute == screen.route,
                onClick = { navigateHome(navController, currentRoute, screen) },
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

@Composable
fun HomeNavigationRail(navController: NavHostController, context: Context) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    NavigationRail(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
        homeNavigationItems.forEach { screen ->
            NavigationRailItem(
                selected = currentRoute == screen.route,
                onClick = { navigateHome(navController, currentRoute, screen) },
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = context.getString(screen.labelResId),
                        modifier = Modifier.size(24.dp),
                    )
                },
                label = {
                    Text(
                        text = context.getString(screen.labelResId),
                        style = MaterialTheme.typography.labelMedium,
                    )
                },
                colors = androidx.compose.material3.NavigationRailItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.surfaceContainer,
                    selectedTextColor = MaterialTheme.colorScheme.surfaceContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                    unselectedTextColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                    indicatorColor = Color.Transparent,
                ),
            )
        }
    }
}

private fun navigateHome(
    navController: NavHostController,
    currentRoute: String?,
    screen: HomeNavRoutes,
) {
    if (currentRoute == screen.route) return

    navController.navigate(screen.route) {
        popUpTo(navController.graph.startDestinationId) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
