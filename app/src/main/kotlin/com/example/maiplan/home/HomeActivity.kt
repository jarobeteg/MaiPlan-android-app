package com.example.maiplan.home

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.maiplan.home.navigation.HomeNavHost
import com.example.maiplan.theme.AppTheme
import com.example.maiplan.utils.BaseActivity

class HomeActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                val rootNavController: NavHostController = rememberNavController()
                HomeNavHost(rootNavController)
            }
        }
    }
}