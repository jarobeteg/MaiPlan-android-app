package com.example.maiplan.home.navigation

import android.app.Activity
import android.app.ActivityOptions
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
import com.example.maiplan.home.event.EventActivity
import com.example.maiplan.home.file.FileActivity
import com.example.maiplan.home.more.MoreActivity
import com.example.maiplan.home.task.TaskActivity

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
 * @see BottomNavRoutes
 * @see EventActivity
 * @see TaskActivity
 * @see FileActivity
 * @see MoreActivity
 */
@Composable
fun BottomNavigationBar(context: Context) {
    val items = listOf(
        BottomNavRoutes.Events to EventActivity::class.java,
        BottomNavRoutes.Tasks to TaskActivity::class.java,
        BottomNavRoutes.Files to FileActivity::class.java,
        BottomNavRoutes.More to MoreActivity::class.java
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        val currentActivity = (context as? Activity)

        items.forEach { (item, activityClass) ->
            NavigationBarItem(
                icon = { Icon(imageVector = item.icon, contentDescription = context.getString(item.labelResId)) },
                label = { Text(context.getString(item.labelResId)) },
                selected = currentActivity?.javaClass == activityClass,
                onClick = {
                    if (currentActivity?.javaClass != activityClass) {
                        val intent = Intent(context, activityClass).apply {
                            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        }
                        val options = ActivityOptions.makeCustomAnimation(context, 0, 0).toBundle()
                        context.startActivity(intent, options)
                        currentActivity?.finish()
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