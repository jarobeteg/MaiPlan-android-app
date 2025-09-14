package com.example.maiplan.home.event.navigation

sealed class EventRoutes(val route: String) {
    data object EventMain : EventRoutes("event-main-screen")
    data object Create : EventRoutes("create-event")
    data object Update : EventRoutes("update-event/{eventId}") {
        fun withArgs(eventId: Int) = "update-event/$eventId"
    }
}