package com.example.maiplan.home.navigation

import android.content.Context
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

/**
 * A [Composable] that displays the bottom navigation bar for the `Home` tabs.
 *
 * - Renders a [NavigationBar] with four primary navigation items: `Events`, `Tasks`, `Files`, and `More`.
 * - Each item includes an icon and a label, and highlights the currently active route.
 * - When a user taps a different item:
 *   - Navigates to the selected screen using the provided [navController].
 *   - Pops up to the start destination and saves the back stack state.
 *   - Prevents duplicate destinations with `launchSingleTop`.
 *   - Restores saved state for previously visited destinations.
 *
 * @param navController The [NavHostController] used to manage navigation between composable destinations.
 * @param context The [Context] used to fetch localized string resources and for intent-based operations (if needed).
 *
 * @see HomeNavRoutes
 * @see NavigationBar
 * @see NavHostController
 */
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


    NavigationBar(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(imageVector = screen.icon, contentDescription = context.getString(screen.labelResId)) },
                label = { Text(context.getString(screen.labelResId)) },
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