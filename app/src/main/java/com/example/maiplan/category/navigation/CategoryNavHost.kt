package com.example.maiplan.category.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import com.example.maiplan.category.screens.*
import com.example.maiplan.network.api.CategoryCreate
import com.example.maiplan.network.api.CategoryResponse
import com.example.maiplan.repository.Result
import com.example.maiplan.utils.model.UserSession
import com.example.maiplan.viewmodel.category.CategoryViewModel

/**
 * [Composable] that sets up the navigation host for the `Category` screens.
 *
 * This host defines the entry point and connects the navigation graph for:
 * - Viewing and managing categories ([CategoryManagementScreen])
 * - Creating a new category ([CreateCategoryScreen])
 * - Updating an existing categories ([UpdateCategoryScreen])
 *
 * Navigation transitions are set to instantly fade between screens for a seamless and subtle effect:
 * - `enterTransition`, `popEnterTransition`: Fade in with no delay.
 * - `exitTransition`, `popExitTransition`: Fade out with no delay.
 *
 * @param categoryViewModel Shared `ViewModel` for performing `CRUD` operations on categories.
 *
 * @see CategoryViewModel
 * @see CategoryRoutes
 * @see categoryNavGraph
 */
@Composable
fun CategoryNavHost(categoryViewModel: CategoryViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = CategoryRoutes.Management.route,
        enterTransition = { fadeIn(animationSpec = tween(0)) },
        exitTransition = { fadeOut(animationSpec = tween(0)) },
        popEnterTransition = { fadeIn(animationSpec = tween(0)) },
        popExitTransition = { fadeOut(animationSpec = tween(0)) }
    ) {
        categoryNavGraph(navController, categoryViewModel)
    }
}

/**
 * Defines the navigation graph for the `Category` screens.
 *
 * This graph includes:
 * - [CategoryManagementScreen]: Displays all `Categories` with options to edit or delete.
 * - [CreateCategoryScreen]: Allows `User` to create a new `Category`.
 * - [UpdateCategoryScreen]: Enables editing of an existing `Category` by its Id.
 *
 * Navigation between screens is handled by [navController]
 * The [categoryViewModel] is passed to all screens to ensure shared state.
 *
 * @param navController The `NavController` that handles navigation between screens.
 * @param categoryViewModel The `ViewModel` providing data and logic for the `Category` screens.
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
        CategoryManagementScreen(
            viewModel = categoryViewModel,
            onCardSwipeDelete = { categoryId ->
                categoryViewModel.deleteCategory(categoryId, userId)
            },
            onCardSwipeEdit = { category ->
                // Prevent double navigation using isNavigating flag
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

        /*
         * On a successful Category create:
         * - Pops back to the previous screen.
         * - Clears errors and create result state.
         */
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
        /*
         * Retrieves the categoryId from the formatted route.
         *
         * Retrieves selected Category using the retrieved categoryId.
         */
        val categoryId = backStackEntry.arguments?.getInt("categoryId") ?: return@composable
        val selectedCategory = categoryViewModel.getCategoryById(categoryId)

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

        /*
         * On a successful Category update:
         * - Pops back to the previous screen.
         * - Clears errors and update result state.
         */
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