package com.example.maiplan.home.event.navigation

/**
 * Represents the different navigation routes for the Event views and screens.
 *
 * Each object corresponds to a specific Event view or screen.
 *
 * @property route Every object holds a route string value to differentiate route endpoints.
 */
sealed class EventRoutes(val route: String) {
    /**
     * Route for the Event Screen With Nav, which is the Main Event Screen, it allows navigation between other event screen as well as the bottom nav.
     */
    data object EventMain : EventRoutes("event-main-screen")

    /**
     * Route for the Create Event Screen, used to create a new event.
     */
    data object Create : EventRoutes("create-event")

    /**
     * Route for the Update Event Screen, used to update existing event.
     *
     * This route includes a path parameter for the event Id.
     * Example: "update-event/3"
     */
    data object Update : EventRoutes("update-event/{eventId}") {

        /**
         * Returns a formatted route with the given eventId.
         *
         * @param eventId The Id of the event to update.
         * @return A route string like "update-event/3".
         */
        fun withArgs(eventId: Int) = "update-event/$eventId"
    }
}