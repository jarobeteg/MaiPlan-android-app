package com.example.maiplan.category

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.example.maiplan.category.screens.CategoryManagementScreen
import com.example.maiplan.theme.AppTheme

class CategoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AppTheme { CategoryManagementScreen() } }
    }
}