package com.example.maiplan.home.event.navigation

sealed class EventRoutes(val route: String) {
    data object EventMain : EventRoutes("event-main-screen")
    data object Create : EventRoutes("create-event")
    data object Update : EventRoutes("update-event/{eventLocalId}?occurrenceDay={occurrenceDay}") {
        fun withArgs(eventLocalId: Long, occurrenceDate: java.time.LocalDate? = null) =
            "update-event/$eventLocalId" + (occurrenceDate?.let { "?occurrenceDay=${it.toEpochDay()}" } ?: "")
    }
}
