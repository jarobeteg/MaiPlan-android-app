package com.example.maiplan.category

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.maiplan.R
import com.example.maiplan.category.screens.CategoryManagementScreen
import com.example.maiplan.category.screens.CreateCategoryScreen
import com.example.maiplan.network.CategoryCreate
import com.example.maiplan.network.RetrofitClient
import com.example.maiplan.repository.AuthRepository
import com.example.maiplan.repository.CategoryRepository
import com.example.maiplan.repository.Result
import com.example.maiplan.theme.AppTheme
import com.example.maiplan.utils.SessionManager
import com.example.maiplan.viewmodel.AuthViewModel
import com.example.maiplan.viewmodel.CategoryViewModel
import com.example.maiplan.viewmodel.GenericViewModelFactory

class CategoryActivity : AppCompatActivity() {
    private lateinit var sessionManager: SessionManager
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val apiService = RetrofitClient.instance
        val categoryRepository = CategoryRepository(apiService)
        val authRepository = AuthRepository(apiService)
        val categoryFactory = GenericViewModelFactory { CategoryViewModel(categoryRepository) }
        val authFactory = GenericViewModelFactory { AuthViewModel(authRepository) }

        sessionManager = SessionManager(this)
        categoryViewModel = ViewModelProvider(this, categoryFactory)[CategoryViewModel::class.java]
        authViewModel = ViewModelProvider(this, authFactory)[AuthViewModel::class.java]

        val token = sessionManager.getAuthToken()
        authViewModel.getProfile(token!!)

        authViewModel.userId.observe(this) { userId ->
            if (userId != null) {
                categoryViewModel.getAllCategories(userId)
            }
        }

        setupComposeUI()
        observeCategoryViewModel()
    }

    private fun setupComposeUI() {
        setContent {
            AppTheme {
                val isCreateCategoryScreen by categoryViewModel.isCreateCategoryScreen.collectAsState()
                val userId by authViewModel.userId.observeAsState()

                BackHandler(enabled = isCreateCategoryScreen) {
                    categoryViewModel.resetCreateCategoryScreen()
                }

                when {
                    isCreateCategoryScreen -> CreateCategoryScreen(
                        onSaveClick = { selectedType, name, description, color, icon ->
                            categoryViewModel.createCategory(CategoryCreate(userId!!, selectedType, name, description, color, icon))
                        },
                        onBackClick = { categoryViewModel.resetCreateCategoryScreen() }
                    )
                    else -> CategoryManagementScreen(categoryViewModel)
                }
            }
        }
    }

    private fun observeCategoryViewModel() {
        fun handleResult(result: Result<Unit>, successMessage: Int) {
            when (result) {
                is Result.Success -> {
                    Toast.makeText(this, getString(successMessage), Toast.LENGTH_SHORT).show()
                    categoryViewModel.resetCreateCategoryScreen()
                }
                is Result.Error -> { Toast.makeText(this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show() }
                is Result.Failure -> { Toast.makeText(this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show() }
                is Result.Idle -> {}
            }
        }

        categoryViewModel.createCategoryResult.observe(this, Observer { handleResult(it, R.string.category_success) })
    }
}