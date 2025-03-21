package com.example.maiplan.home.event

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.example.maiplan.home.event.screens.EventScreenWithNav
import com.example.maiplan.theme.AppTheme

class EventActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AppTheme { EventScreenWithNav(this) } }
    }
}