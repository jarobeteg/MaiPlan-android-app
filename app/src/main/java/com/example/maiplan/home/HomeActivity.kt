package com.example.maiplan.home

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.example.maiplan.screens.home.BottomNavWithScreens
import com.example.maiplan.theme.AppTheme

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AppTheme { BottomNavWithScreens() } }
    }
}