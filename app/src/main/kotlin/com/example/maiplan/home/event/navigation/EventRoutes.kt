package com.example.maiplan.home.event.navigation

sealed class EventRoutes(val route: String) {
    data object EventMain : EventRoutes("event-main-screen")
    data object Create : EventRoutes("create-event")
    data object Update : EventRoutes("update-event/{eventLocalId}") {
        fun withArgs(eventLocalId: Long) = "update-event/$eventLocalId"
    }
}
