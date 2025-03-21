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

@Composable
fun EventNavHost(navController: NavHostController, selectedDate: LocalDate, context: Context) {
    NavHost(
        navController = navController,
        startDestination = EventRoutes.Monthly.route
    ) {
        eventNavGraph(navController, selectedDate, context)
    }
}

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