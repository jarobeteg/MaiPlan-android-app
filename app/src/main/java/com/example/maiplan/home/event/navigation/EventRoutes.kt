package com.example.maiplan.home.event.navigation

import com.example.maiplan.home.event.screens.*

/**
 * Represents the different navigation routes for the `Event` screens.
 *
 * Each object corresponds to a specific `Event` screen.
 *
 * @property route Every object holds a route string value to differentiate route endpoints.
 */
sealed class EventRoutes(val route: String) {
    /**
     * Route for the [EventScreen], which is the `Main Event Screen`, it allows navigation between other event screen as well as home tabs.
     */
    data object EventMain : EventRoutes("event-main-screen")

    /**
     * Route for the [CreateEventScreen], used to create a new event.
     */
    data object Create : EventRoutes("create-event")

    /**
     * Route for the [UpdateEventScreen], used to update existing event.
     *
     * This route includes a path parameter for the `eventId`.
     * Example: "update-event/3"
     */
    data object Update : EventRoutes("update-event/{eventId}") {

        /**
         * Returns a formatted route with the given [eventId].
         *
         * @param eventId The Id of the event to update.
         * @return A route string like "update-event/3".
         */
        fun withArgs(eventId: Int) = "update-event/$eventId"
    }
}