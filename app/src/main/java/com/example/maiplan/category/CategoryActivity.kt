package com.example.maiplan.category

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.maiplan.R
import com.example.maiplan.category.screens.CategoryManagementScreen
import com.example.maiplan.category.screens.CreateCategoryScreen
import com.example.maiplan.category.screens.UpdateCategoryScreen
import com.example.maiplan.network.CategoryCreate
import com.example.maiplan.network.CategoryResponse
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

        val categoryApi = RetrofitClient.categoryApi
        val authApi = RetrofitClient.authApi
        val categoryRepository = CategoryRepository(categoryApi)
        val authRepository = AuthRepository(authApi)
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
                val isUpdateCategoryScreen by categoryViewModel.isUpdateCategoryScreen.collectAsState()
                var selectedCategory by remember { mutableStateOf<CategoryResponse?>(null) }
                val userId by authViewModel.userId.observeAsState()

                BackHandler(enabled = isCreateCategoryScreen) {
                    categoryViewModel.resetCreateCategoryScreen()
                    categoryViewModel.clearErrors()
                }

                when {
                    isCreateCategoryScreen -> CreateCategoryScreen(
                        viewModel = categoryViewModel,
                        onSaveClick = { name, description, color, icon ->
                            categoryViewModel.createCategory(CategoryCreate(userId!!, name, description, color, icon))
                        },
                        onBackClick = {
                            categoryViewModel.resetCreateCategoryScreen()
                            categoryViewModel.clearErrors()
                        }
                    )
                    isUpdateCategoryScreen -> UpdateCategoryScreen(
                        viewModel = categoryViewModel,
                        category = selectedCategory!!,
                        onSaveClick = { name, description, color, icon ->
                            categoryViewModel.updateCategory(CategoryResponse(selectedCategory!!.categoryId, name, description, color, icon), userId!!)
                        },
                        onBackClick = {
                            categoryViewModel.resetUpdateCategoryScreen()
                            categoryViewModel.clearErrors()
                        }
                    )
                    else -> CategoryManagementScreen(
                        viewModel = categoryViewModel,
                        onCardSwipeDelete = { categoryId->
                            categoryViewModel.deleteCategory(categoryId, userId!!)
                        },
                        onCardSwipeEdit = { category ->
                            categoryViewModel.handleUpdateCategoryClicked()
                            selectedCategory = category
                        }
                    )
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
                    categoryViewModel.resetUpdateCategoryScreen()
                }
                is Result.Error -> { Toast.makeText(this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show() }
                is Result.Failure -> {}
                is Result.Idle -> {}
            }
        }

        categoryViewModel.createCategoryResult.observe(this, Observer { handleResult(it, R.string.category_created_success) })
        categoryViewModel.updateCategoryResult.observe(this, Observer { handleResult(it, R.string.category_updated_success) })
        categoryViewModel.deleteCategoryResult.observe(this, Observer { handleResult(it, R.string.category_deleted) })
    }
}