package com.example.maiplan.main.navigation

sealed class MainRoutes(val route: String) {
    data object Login : MainRoutes("login")
    data object Register : MainRoutes("register")
}