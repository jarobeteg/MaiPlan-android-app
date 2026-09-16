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
import com.example.maiplan.category.data.CreateCategoryInput
import com.example.maiplan.category.data.UpdateCategoryInput
import com.example.maiplan.category.screens.*
import com.example.maiplan.repository.Result
import com.example.maiplan.viewmodel.category.CategoryViewModel

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

fun NavGraphBuilder.categoryNavGraph(
    navController: NavController,
    categoryViewModel: CategoryViewModel
) {
    // --- Category Management Screen ---
    composable(CategoryRoutes.Management.route) {
        CategoryManagementScreen(
            viewModel = categoryViewModel,
            onCardSwipeDelete = { categoryId ->
                categoryViewModel.softDeleteCategory(categoryId)
            },
            onCardSwipeEdit = { category ->
                // Prevent double navigation using isNavigating flag
                if (categoryViewModel.isNavigating.value == false) {
                    categoryViewModel.startNavigation()
                    navController.navigate(CategoryRoutes.Update.withArgs(category.categoryLocalId))
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
                categoryViewModel.createCategory(
                    CreateCategoryInput(
                        name = name,
                        description = description,
                        color = color,
                        icon = icon
                    )
                )
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
        arguments = listOf(navArgument("categoryLocalId") { type = NavType.LongType })
    ) { backStackEntry ->
        /*
         * Retrieves the categoryId from the formatted route.
         *
         * Retrieves selected Category using the retrieved categoryId.
         */
        val categoryLocalId = backStackEntry.arguments?.getLong("categoryLocalId") ?: return@composable
        val selectedCategory = categoryViewModel.getCategory(categoryLocalId) ?: return@composable

        UpdateCategoryScreen(
            viewModel = categoryViewModel,
            category = selectedCategory,
            onSaveClick = { name, description, color, icon ->
                categoryViewModel.updateCategory(
                    UpdateCategoryInput(
                        categoryLocalId = selectedCategory.categoryLocalId,
                        name = name,
                        description = description,
                        color = color,
                        icon = icon
                    )
                )
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