package com.example.maiplan.home.file

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.maiplan.home.file.navigation.FileNavHost

@Composable
fun FileScreenManager(rootNavController: NavHostController) {
    val localNavController = rememberNavController()

    FileNavHost(rootNavController, localNavController)
}