package com.example.maiplan.category

import androidx.compose.runtime.Composable
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
import com.example.maiplan.utils.UserSession
import com.example.maiplan.viewmodel.CategoryViewModel

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

fun NavGraphBuilder.categoryNavGraph(
    navController: NavController,
    categoryViewModel: CategoryViewModel
) {
    composable(CategoryRoutes.Management.route) {
        CategoryManagementScreen(
            viewModel = categoryViewModel,
            onCardSwipeDelete = { categoryId ->
                categoryViewModel.deleteCategory(categoryId, UserSession.userId!!)
            },
            onCardSwipeEdit = { category ->
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

    composable(CategoryRoutes.Create.route) {
        CreateCategoryScreen(
            viewModel = categoryViewModel,
            onSaveClick = { name, description, color, icon ->
                categoryViewModel.createCategory(CategoryCreate(UserSession.userId!!, name, description, color, icon))
                navController.popBackStack()
            },
            onBackClick = {
                navController.popBackStack()
            }
        )
    }

    composable(
        route = CategoryRoutes.Update.route,
        arguments = listOf(navArgument("categoryId") { type = NavType.IntType })
    ) { backStackEntry ->
        val categoryId = backStackEntry.arguments?.getInt("categoryId") ?: return@composable
        val selectedCategory = categoryViewModel.getCategoryById(categoryId)

        selectedCategory.let { category ->
            UpdateCategoryScreen(
                viewModel = categoryViewModel,
                category = category,
                onSaveClick = { name, description, color, icon ->
                    categoryViewModel.updateCategory(CategoryResponse(category.categoryId, name, description, color, icon), UserSession.userId!!)
                    navController.popBackStack()
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}