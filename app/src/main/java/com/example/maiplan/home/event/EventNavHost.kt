package com.example.maiplan.home.event

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.navigation.compose.composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.example.maiplan.home.event.screens.DailyView
import com.example.maiplan.home.event.screens.MonthlyView
import com.example.maiplan.home.event.screens.WeeklyView
import java.time.LocalDate

/**
 * Composable that sets up the navigation host for the Event views.
 *
 * It defines the entry point and connects the navigation graph for different event views,
 * Monthly, Weekly, and Daily. It also initializes the navigation graph using [selectedDate].
 *
 * @param navController The [NavHostController] is used to control navigation between event views.
 * @param selectedDate The currently selected date used across event views.
 * @param context The [Context] is used for the views to retrieve localized string resources.
 *
 * @see EventRoutes
 */
@Composable
fun EventNavHost(navController: NavHostController, selectedDate: LocalDate, context: Context) {
    NavHost(
        navController = navController,
        startDestination = EventRoutes.Monthly.route
    ) {
        eventNavGraph(navController, selectedDate, context)
    }
}

/**
 * Defines the navigation graph for the Event views.
 *
 * This graph includes:
 * - Monthly View: Displays events for the selected month.
 * - Weekly View: Displays events for the selected week.
 * - Daily View: Displays events for the selected day.
 *
 * The navigation between screens are handled by the [navController].
 * Navigation between views don't require the [navController].
 *
 * @param navController The [NavController] is used to handle navigation actions.
 * @param selectedDate The [LocalDate] is shared across views to show relevant data.
 * @param context The [Context] is passed to views to retrieve localized string resources.
 *
 * @see MonthlyView
 * @see WeeklyView
 * @see DailyView
 */
fun NavGraphBuilder.eventNavGraph(
    navController: NavController,
    selectedDate: LocalDate,
    context: Context
) {
    composable(EventRoutes.Monthly.route) {
        MonthlyView(selectedDate, context)
    }
    composable(EventRoutes.Weekly.route) {
        WeeklyView(selectedDate)
    }
    composable(EventRoutes.Daily.route) {
        DailyView(selectedDate)
    }
}