package com.example.maiplan.utils

import com.example.maiplan.network.UserResponse

/**
 * A singleton object that holds basic user information during an active session.
 *
 * This object stores user data (Id, email, username) in memory after login,
 * fetched from a network [UserResponse].
 *
 * ## Responsibilities:
 * - Initializing user session data after successful authentication.
 * - Clearing session data when the user logs out.
 *
 * @see UserResponse
 */
object UserSession {
    /** The unique user ID. */
    var userId: Int? = null

    /** The user's email address. */
    var email: String? = null

    /** The user's username. */
    var username: String? = null

    /**
     * Initializes the user session with the provided [data] from the API.
     *
     * @param data The [UserResponse] containing user information.
     *
     * @see UserResponse
     */
    fun init(data: UserResponse) {
        userId = data.id
        email = data.email
        username = data.username
    }

    /**
     * Clears all stored user session data.
     */
    fun clear() {
        userId = null
        email = null
        username = null
    }
}