package com.example.maiplan.home.navigation

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import com.example.maiplan.theme.LocalAppDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.maiplan.utils.AdaptiveLayout
import com.example.maiplan.utils.LocalAdaptiveLayout
import com.example.maiplan.theme.AppThemeManager

private val HomePrimary: Color get() = AppThemeManager.selectedTheme.primary
private val HomePrimaryLight: Color get() = AppThemeManager.selectedTheme.primaryLight
private val HomeMuted = Color(0xFF667085)
private val HomeBorder = Color(0xFFDDE3EC)

private val homeNavigationItems = listOf(
    HomeNavRoutes.Events,
    HomeNavRoutes.Tasks,
    HomeNavRoutes.Home,
    HomeNavRoutes.Notes,
    HomeNavRoutes.More,
)

internal val AdaptiveLayout.useHomeNavigationRail: Boolean
    get() = useNavigationRail || isLandscape

@Composable
fun HomeNavigationBar(navController: NavHostController, context: Context) {
    if (LocalAdaptiveLayout.current.useHomeNavigationRail) return

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val dark = LocalAppDarkTheme.current
    val surface = if (dark) Color(0xFF191D2E) else Color.White
    val selectedLabel = if (dark) Color(0xFFC9D7E5) else HomePrimary
    val unselected = if (dark) Color(0xFFAEB7C9) else HomeMuted

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .navigationBarsPadding()
            .padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 10.dp),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = surface,
            shadowElevation = if (dark) 0.dp else 10.dp,
            border = BorderStroke(
                1.dp,
                if (dark) Color(0xFF30374D) else HomeBorder.copy(alpha = 0.85f),
            ),
        ) {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .clip(RoundedCornerShape(24.dp)),
                containerColor = surface,
                contentColor = unselected,
                tonalElevation = 0.dp,
                windowInsets = WindowInsets(0, 0, 0, 0),
            ) {
                homeNavigationItems.forEach { screen ->
                    val selected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = { navigateHome(navController, currentRoute, screen) },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                modifier = Modifier.size(if (selected) 24.dp else 23.dp),
                                contentDescription = context.getString(screen.labelResId),
                            )
                        },
                        label = {
                            Text(
                                text = context.getString(screen.labelResId),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            )
                        },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = selectedLabel,
                            unselectedIconColor = unselected,
                            unselectedTextColor = unselected,
                            indicatorColor = HomePrimary,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
fun HomeNavigationRail(navController: NavHostController, context: Context) {
    val adaptiveLayout = LocalAdaptiveLayout.current
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val dark = LocalAppDarkTheme.current
    val surface = if (dark) Color(0xFF191D2E) else Color.White
    val selectedLabel = if (dark) Color(0xFFC9D7E5) else HomePrimary
    val unselected = if (dark) Color(0xFFAEB7C9) else HomeMuted

    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .width(92.dp),
        color = surface,
        shadowElevation = if (dark) 0.dp else 5.dp,
        border = BorderStroke(
            1.dp,
            if (dark) Color(0xFF30374D) else HomeBorder.copy(alpha = 0.75f),
        ),
    ) {
        NavigationRail(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(),
            containerColor = surface,
            contentColor = unselected,
            windowInsets = WindowInsets(0, 0, 0, 0),
            header = {
                if (!adaptiveLayout.isShort) {
                    HomeRailBrandMark()
                    Spacer(Modifier.height(22.dp))
                } else {
                    Spacer(Modifier.height(8.dp))
                }
            },
        ) {
            homeNavigationItems.forEach { screen ->
                val selected = currentRoute == screen.route
                NavigationRailItem(
                    selected = selected,
                    onClick = { navigateHome(navController, currentRoute, screen) },
                    icon = {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = context.getString(screen.labelResId),
                            modifier = Modifier.size(if (selected) 24.dp else 23.dp),
                        )
                    },
                    label = {
                        Text(
                            text = context.getString(screen.labelResId),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        )
                    },
                    alwaysShowLabel = true,
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = selectedLabel,
                        unselectedIconColor = unselected,
                        unselectedTextColor = unselected,
                        indicatorColor = HomePrimary,
                    ),
                )
            }
        }
    }
}

@Composable
private fun HomeRailBrandMark() {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.linearGradient(listOf(HomePrimary, HomePrimaryLight))),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.CalendarMonth,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp),
        )
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
