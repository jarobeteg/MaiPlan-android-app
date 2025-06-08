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
 * The [CategoryActivity] is responsible for managing and displaying `Category` screens.
 *
 * The [CategoryActivity] initializes the [CategoryViewModel], sets up the UI (`Jetpack Compose`),
 * and observes result states from `Category` `CRUD` operations.
 */
class CategoryActivity : AppCompatActivity() {
    /** [CategoryViewModel] instance to handle `Category` related logic. */
    private lateinit var categoryViewModel: CategoryViewModel

    /**
     * Lifecycle method [onCreate] called when the [CategoryActivity] is created.
     *
     * - Initializes the [CategoryViewModel] using a [CategoryRepository] and [GenericViewModelFactory].
     * - The [CategoryRepository] needs to be initialized by the [CategoryRemoteDataSource].
     * - The [CategoryRemoteDataSource] needs to be initialized by the [RetrofitClient]'s [RetrofitClient.categoryApi]
     * - The [CategoryRepository] holds the `CRUD` operations for the [CategoryRemoteDataSource].
     * - The [CategoryRemoteDataSource] communicates with the [RetrofitClient.categoryApi].
     * - The [GenericViewModelFactory] is a tool to pass dependency for a `ViewModel`, in our case it's the [CategoryRepository].
     * - Fetches all `Categories` for the current `User`.
     * - Sets up the `Compose` UI.
     * - Observes [CategoryViewModel] result `LiveData` to provide feedback for the `User`.
     *
     * @param savedInstanceState Saved state of this [CategoryActivity] if previously existed.
     *
     * @see CategoryViewModel
     * @see CategoryRepository
     * @see GenericViewModelFactory
     * @see CategoryRemoteDataSource
     * @see RetrofitClient
     * @see UserSession
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initViewModel()
        setupComposeUI()
        observeCategoryViewModel()
    }

    /**
     * Initializes the [CategoryViewModel] with its required dependencies.
     *
     * Sets up the [RetrofitClient.categoryApi], [CategoryRemoteDataSource],
     * and [CategoryRepository] for `Category` `CRUD` operations,
     * and provides them to the [CategoryViewModel] using a [GenericViewModelFactory].
     * Also triggers an initial fetch of all `Categories` for the current `User`.
     *
     * Requires [UserSession.userId] to be non-null.
     *
     * @see CategoryViewModel
     * @see CategoryRepository
     * @see CategoryRemoteDataSource
     * @see GenericViewModelFactory
     */
    private fun initViewModel() {
        val categoryApi = RetrofitClient.categoryApi
        val categoryRemoteDataSource = CategoryRemoteDataSource(categoryApi)
        val categoryRepository = CategoryRepository(categoryRemoteDataSource)
        val categoryFactory = GenericViewModelFactory { CategoryViewModel(categoryRepository) }

        categoryViewModel = ViewModelProvider(this, categoryFactory)[CategoryViewModel::class.java]
        categoryViewModel.getAllCategories(UserSession.userId!!)
    }

    /**
     * Sets up the UI content using `Jetpack Compose` and applies the app a theme.
     *
     * Sets up the navigation between `Category` screens.
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
     * Observes `LiveData` from the [CategoryViewModel] to handle `Category` operation results.
     *
     * Displays `Toast` messages based on the result of the operation.
     */
    private fun observeCategoryViewModel() {
        /**
         * Handles a result from a `Category` operation and displays feedback.
         *
         * @param result The result of the operation.
         * @param successMessage The string `resourceId` to display when the operation ended on [Result.Success].
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

        /*
         * Sets up the Observers for the Category operations.
         */
        categoryViewModel.createCategoryResult.observe(this, Observer { handleResult(it, R.string.category_created_success) })
        categoryViewModel.updateCategoryResult.observe(this, Observer { handleResult(it, R.string.category_updated_success) })
        categoryViewModel.deleteCategoryResult.observe(this, Observer { handleResult(it, R.string.category_deleted) })
    }
}