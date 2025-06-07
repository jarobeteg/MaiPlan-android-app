package com.example.maiplan.home.navigation

import android.app.Activity
import android.content.Context
import android.content.Intent
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
 * Displays the bottom navigation bar for the Home component Activity screens.
 *
 * Each navigation item:
 * - Shows an icon and label.
 * - Highlights the currently active screen.
 * - Navigates to the corresponding [Activity] when clicked.
 *
 * When a different item is clicked:
 * - Starts the corresponding [Activity] with no animation.
 * - Reuses existing activities if possible (via [Intent.FLAG_ACTIVITY_REORDER_TO_FRONT]).
 * - Finishes the current [Activity] to avoid stacking duplicates.
 *
 * @param context The [Context] used to start new activities and determine the current one.
 *
 * @see HomeNavRoutes
 * @see EventActivity
 * @see TaskActivity
 * @see FileActivity
 * @see MoreActivity
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