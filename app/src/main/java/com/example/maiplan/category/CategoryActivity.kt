package com.example.maiplan.category

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import com.example.maiplan.category.screens.CategoryManagementScreen
import com.example.maiplan.category.screens.CreateCategoryScreen
import com.example.maiplan.network.RetrofitClient
import com.example.maiplan.repository.CategoryRepository
import com.example.maiplan.theme.AppTheme
import com.example.maiplan.viewmodel.CategoryViewModel
import com.example.maiplan.viewmodel.GenericViewModelFactory

class CategoryActivity : AppCompatActivity() {
    private lateinit var viewModel: CategoryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val apiService = RetrofitClient.instance
        val categoryRepository = CategoryRepository(apiService)
        val factory = GenericViewModelFactory { CategoryViewModel(categoryRepository) }

        viewModel = ViewModelProvider(this, factory)[CategoryViewModel::class.java]

        setupComposeUI()
        observeViewModel()
    }

    private fun setupComposeUI() {
        setContent {
            AppTheme {
                val isCreateCategoryScreen by viewModel.isCreateCategoryScreen.collectAsState()
                BackHandler(enabled = isCreateCategoryScreen) {
                    viewModel.resetCreateCategoryScreen()
                }

                when {
                    isCreateCategoryScreen -> CreateCategoryScreen(
                        viewModel = viewModel,
                        onBackClick = { viewModel.resetCreateCategoryScreen() }
                    )
                    else -> CategoryManagementScreen(viewModel)
                }
            }
        }
    }

    private fun observeViewModel() {}
}