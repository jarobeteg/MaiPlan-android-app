package com.example.maiplan.home

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.maiplan.home.navigation.HomeNavHost
import com.example.maiplan.home.navigation.HomeNavigationBar
import com.example.maiplan.theme.AppTheme

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                val rootNavController = rememberNavController()
                HomeNavHost(rootNavController)
            }
        }
    }
}