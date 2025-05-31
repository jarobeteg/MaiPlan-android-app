package com.example.maiplan.category

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.maiplan.category.screens.CategoryManagementScreen
import com.example.maiplan.category.screens.CreateCategoryScreen
import com.example.maiplan.category.screens.UpdateCategoryScreen
import com.example.maiplan.network.CategoryCreate
import com.example.maiplan.network.CategoryResponse
import com.example.maiplan.repository.Result
import com.example.maiplan.utils.UserSession
import com.example.maiplan.viewmodel.CategoryViewModel

/**
 * Composable that sets up the navigation host for the Category screens.
 *
 * It defines the entry point and connects the navigation graph for managing categories,
 * viewing, creating, and updating categories.
 *
 * @param categoryViewModel The ViewModel shared across category-related screens,
 * used for performing CRUD operations on categories.
 *
 * @see CategoryViewModel
 * @see CategoryRoutes
 */
@Composable
fun CategoryNavHost(categoryViewModel: CategoryViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = CategoryRoutes.Management.route
    ) {
        categoryNavGraph(navController, categoryViewModel)
    }
}

/**
 * Defines the navigation graph for the Category screens.
 *
 * This graph includes:
 * - Category Management Screen: Displays all categories with options to edit or delete.
 * - Create Category Screen: Allows user to create a new category.
 * - Update Category Screen: Enables editing of an existing category by its Id.
 *
 * Navigation between screens is handled by [navController]
 * The [categoryViewModel] is passed to all screens to ensure shared state.
 *
 * @param navController The controller that handles navigation between screens.
 * @param categoryViewModel The ViewModel providing data and logic for the Category screens.
 *
 * @see CategoryManagementScreen
 * @see CreateCategoryScreen
 * @see UpdateCategoryScreen
 * @see CategoryViewModel
 * @see CategoryCreate
 * @see CategoryResponse
 * @see UserSession
 */
fun NavGraphBuilder.categoryNavGraph(
    navController: NavController,
    categoryViewModel: CategoryViewModel
) {
    val userId = UserSession.userId!!

    // --- Category Management Screen ---
    composable(CategoryRoutes.Management.route) {
        /**
         * Displays a list of categories.
         *
         * @param viewModel Used to fetch and manage category data.
         * @param onCardSwipeDelete Triggered when a category card is swiped to delete.
         * @param onCardSwipeEdit Triggered when a category card is swiped to edit.
         * @param onCreateCategoryClick Called when the user clicks the add new category button.
         */
        CategoryManagementScreen(
            viewModel = categoryViewModel,
            onCardSwipeDelete = { categoryId ->
                categoryViewModel.deleteCategory(categoryId, userId)
            },
            onCardSwipeEdit = { category ->
                /** Prevent double navigation using [isNavigating] flag */
                if (categoryViewModel.isNavigating.value == false) {
                    categoryViewModel.startNavigation()
                    navController.navigate(CategoryRoutes.Update.withArgs(category.categoryId))
                    categoryViewModel.resetNavigation()
                }
            },
            onCreateCategoryClick = {
                navController.navigate(CategoryRoutes.Create.route)
            }
        )
    }

    // --- Create Category Screen ---
    composable(CategoryRoutes.Create.route) {
        /**
         * Screen for creating a new category.
         *
         * @param viewModel Supplies the creation logic.
         * @param onSaveClick Called when the user submits the category creation form.
         * @param onBackClick Cancels creating and pops back without saving.
         */
        CreateCategoryScreen(
            viewModel = categoryViewModel,
            onSaveClick = { name, description, color, icon ->
                categoryViewModel.createCategory(CategoryCreate(userId, name, description, color, icon))
            },
            onBackClick = {
                navController.popBackStack()
                categoryViewModel.clearErrors()
            }
        )

        val result = categoryViewModel.createCategoryResult.observeAsState().value
        LaunchedEffect(result) {
            if (result is Result.Success) {
                navController.popBackStack()
                categoryViewModel.clearErrors()
                categoryViewModel.clearCreateResult()
            }
        }
    }

    // --- Update Category Screen ---
    composable(
        route = CategoryRoutes.Update.route,
        arguments = listOf(navArgument("categoryId") { type = NavType.IntType })
    ) { backStackEntry ->
        /**
         * Retrieves the category Id from the formatted route.
         *
         * Retrieves selected category using the retrieved category Id.
         */
        val categoryId = backStackEntry.arguments?.getInt("categoryId") ?: return@composable
        val selectedCategory = categoryViewModel.getCategoryById(categoryId)

        /**
         * Screen for updating an existing category.
         *
         * @param viewModel Supplies the update logic.
         * @param category Pre-filled category data to display in the form.
         * @param onSaveClick Called when user submits edit, updates the category and pops back.
         * @param onBackClick Cancels updating and pops back without saving.
         */
        UpdateCategoryScreen(
            viewModel = categoryViewModel,
            category = selectedCategory,
            onSaveClick = { name, description, color, icon ->
                categoryViewModel.updateCategory(CategoryResponse(selectedCategory.categoryId, name, description, color, icon), userId)
            },
            onBackClick = {
                navController.popBackStack()
                categoryViewModel.clearUpdateResult()
            }
        )

        val result = categoryViewModel.updateCategoryResult.observeAsState().value
        LaunchedEffect(result) {
            if (result is Result.Success) {
                navController.popBackStack()
                categoryViewModel.clearErrors()
                categoryViewModel.clearUpdateResult()
            }
        }
    }
}