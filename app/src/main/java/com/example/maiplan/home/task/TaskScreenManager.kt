package com.example.maiplan.home.task

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.maiplan.home.task.navigation.TaskNavHost

/**
 * Still under work.
 */
@Composable
fun TaskScreenManager(rootNavController: NavHostController) {
    val localNavController = rememberNavController()

    TaskNavHost(rootNavController, localNavController)
}