package com.example.maiplan.category

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.maiplan.R
import com.example.maiplan.network.RetrofitClient
import com.example.maiplan.repository.CategoryRepository
import com.example.maiplan.repository.Result
import com.example.maiplan.theme.AppTheme
import com.example.maiplan.utils.UserSession
import com.example.maiplan.viewmodel.CategoryViewModel
import com.example.maiplan.viewmodel.GenericViewModelFactory

class CategoryActivity : AppCompatActivity() {
    private lateinit var categoryViewModel: CategoryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val categoryApi = RetrofitClient.categoryApi
        val categoryRepository = CategoryRepository(categoryApi)
        val categoryFactory = GenericViewModelFactory { CategoryViewModel(categoryRepository) }

        categoryViewModel = ViewModelProvider(this, categoryFactory)[CategoryViewModel::class.java]
        categoryViewModel.getAllCategories(UserSession.userId!!)

        setupComposeUI()
        observeCategoryViewModel()
    }

    private fun setupComposeUI() {
        setContent {
            AppTheme {
                CategoryNavHost(categoryViewModel)
            }
        }
    }

    private fun observeCategoryViewModel() {
        fun handleResult(result: Result<Unit>, successMessage: Int) {
            when (result) {
                is Result.Success -> { Toast.makeText(this, getString(successMessage), Toast.LENGTH_SHORT).show() }
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