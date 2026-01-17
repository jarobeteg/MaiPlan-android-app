package com.example.maiplan.home.more.navigation

sealed class MoreRoutes(val route: String) {
    data object MoreMain : MoreRoutes("more-main-screen")
}