package com.example.maiplan.category

sealed class CategoryRoutes(val route: String) {
    data object Management : CategoryRoutes("category-management")
    data object Create : CategoryRoutes("create-category")
    data object Update : CategoryRoutes("update-category/{categoryId}") {
        fun withArgs(categoryId: Int) = "update-category/$categoryId"
    }
}