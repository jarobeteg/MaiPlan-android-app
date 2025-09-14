package com.example.maiplan.home.more

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.maiplan.home.more.navigation.MoreNavHost

@Composable
fun MoreScreenManager(rootNavController: NavHostController) {
    val localNavController = rememberNavController()

    MoreNavHost(rootNavController, localNavController)
}