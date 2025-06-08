package com.example.maiplan.category.navigation

import com.example.maiplan.category.screens.*

/**
 * Represents the different navigation routes for the `Category` screens.
 *
 * Each object corresponds to a specific `Category` screen.
 *
 * @property route Every object holds a route string value to differentiate route endpoints.
 */
sealed class CategoryRoutes(val route: String) {
    /**
     * Route for the [CategoryManagementScreen], which lists and manages all `User` created `Categories`.
     */
    data object Management : CategoryRoutes("category-management")

    /**
     * Route for the [CreateCategoryScreen], used to create a new `Category`.
     */
    data object Create : CategoryRoutes("create-category")

    /**
     * Route for the [UpdateCategoryScreen], used to update existing `Category`.
     *
     * This route includes a path parameter for the `categoryId`.
     *
     * e.g. "update-category/3"
     */
    data object Update : CategoryRoutes("update-category/{categoryId}") {

        /**
         * Returns a formatted route with the given [categoryId].
         *
         * @param categoryId The Id of the category to update.
         * @return A route string like "update-category/3".
         */
        fun withArgs(categoryId: Int) = "update-category/$categoryId"
    }
}