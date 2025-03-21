package com.example.maiplan.home.event

sealed class EventRoutes(val route: String) {
    data object Monthly : EventRoutes("event-monthly-view")
    data object Weekly : EventRoutes("event-weekly-view")
    data object Daily : EventRoutes("event-daily-view")
    data object Create : EventRoutes("create-event")
    data object Update : EventRoutes("update-event/{eventId}") {
        fun withArgs(eventId: Int) = "update-event/$eventId"
    }
}