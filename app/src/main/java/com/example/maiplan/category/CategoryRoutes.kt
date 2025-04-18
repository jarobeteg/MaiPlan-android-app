package com.example.maiplan.category

/**
 * Represents the different navigation routes for the Category screens.
 *
 * Each object corresponds to a specific Category screen.
 *
 * @property route Every object holds a route string value to differentiate route endpoints.
 */
sealed class CategoryRoutes(val route: String) {
    /**
     * Route for the Category Management Screen, which lists and manages all user created categories.
     */
    data object Management : CategoryRoutes("category-management")

    /**
     * Route for the Create Category Screen, used to create a new category.
     */
    data object Create : CategoryRoutes("create-category")

    /**
     * Route for the Update Category Screen, used to update existing category.
     *
     * This route includes a path parameter for the category Id.
     * Example: "update-category/3"
     */
    data object Update : CategoryRoutes("update-category/{categoryId}") {

        /**
         * Returns a formatted route with the given category Id.
         *
         * @param categoryId The Id of the category to update.
         * @return A route string like "update-category/3".
         */
        fun withArgs(categoryId: Int) = "update-category/$categoryId"
    }
}