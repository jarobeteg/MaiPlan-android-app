package com.example.maiplan.category

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.maiplan.R
import com.example.maiplan.category.navigation.CategoryNavHost
import com.example.maiplan.network.RetrofitClient
import com.example.maiplan.repository.category.CategoryRepository
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.category.CategoryRemoteDataSource
import com.example.maiplan.theme.AppTheme
import com.example.maiplan.utils.model.UserSession
import com.example.maiplan.viewmodel.category.CategoryViewModel
import com.example.maiplan.viewmodel.GenericViewModelFactory

/**
 * This activity is responsible for managing and displaying Category screens.
 *
 * This activity initializes the Category ViewModel, sets up the UI (Jetpack Compose),
 * and observes result states from category operations (create, update, delete).
 */
class CategoryActivity : AppCompatActivity() {
    /** ViewModel instance to handle Category related logic. */
    private lateinit var categoryViewModel: CategoryViewModel

    /**
     * Lifecycle method onCreate is called when the activity created.
     *
     * - Initializes the ViewModel using a repository and generic factory.
     * -- The repository needs to be initialized by the retrofit client's category API
     * -- The repository holds the CRUD operations for the category API.
     * -- The factory is a tool to pass dependency for a ViewModel, in our case it's the repository.
     * - Fetches all categories for the current user.
     * - Sets up the Compose UI.
     * - Observes ViewModel result LiveData to provide feedback for the user.
     *
     * @param savedInstanceState Saved state of this activity if previously existed.
     *
     * @see RetrofitClient
     * @see CategoryRepository
     * @see UserSession
     * @see CategoryViewModel
     * @see GenericViewModelFactory
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initViewModel()
        setupComposeUI()
        observeCategoryViewModel()
    }

    private fun initViewModel() {
        val categoryApi = RetrofitClient.categoryApi
        val categoryRemoteDataSource = CategoryRemoteDataSource(categoryApi)
        val categoryRepository = CategoryRepository(categoryRemoteDataSource)
        val categoryFactory = GenericViewModelFactory { CategoryViewModel(categoryRepository) }

        categoryViewModel = ViewModelProvider(this, categoryFactory)[CategoryViewModel::class.java]
        categoryViewModel.getAllCategories(UserSession.userId!!)
    }

    /**
     * Sets up the UI content using Jetpack Compose and applies the app a theme.
     *
     * Sets up the navigation between Category screens.
     *
     * @see AppTheme
     * @see CategoryNavHost
     */
    private fun setupComposeUI() {
        setContent {
            AppTheme {
                CategoryNavHost(categoryViewModel)
            }
        }
    }

    /**
     * Observes LiveData from the ViewModel to handle category operation results.
     *
     * Displays Toast messages based on the result of the operation.
     */
    private fun observeCategoryViewModel() {
        /**
         * Handles a result from a category operation and displays feedback.
         *
         * @param result The result of the operation.
         * @param successMessage The string resource ID to display when the operation ended on success.
         *
         * @see Result
         * @see CategoryViewModel
         */
        fun handleResult(result: Result<Unit>, successMessage: Int) {
            when (result) {
                is Result.Success -> { Toast.makeText(this, getString(successMessage), Toast.LENGTH_SHORT).show() }
                is Result.Error -> { Toast.makeText(this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show() }
                is Result.Failure -> {} // No feedback for Failure
                is Result.Idle -> {} // No action needed since it's Idle
            }
        }

        /**
         * Sets up the Observers for the Category operations.
         */
        categoryViewModel.createCategoryResult.observe(this, Observer { handleResult(it, R.string.category_created_success) })
        categoryViewModel.updateCategoryResult.observe(this, Observer { handleResult(it, R.string.category_updated_success) })
        categoryViewModel.deleteCategoryResult.observe(this, Observer { handleResult(it, R.string.category_deleted) })
    }
}