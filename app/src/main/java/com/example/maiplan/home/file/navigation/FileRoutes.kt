package com.example.maiplan.home.file.navigation

sealed class FileRoutes(val route: String) {
    data object FileMain : FileRoutes("file-main-screen")
}