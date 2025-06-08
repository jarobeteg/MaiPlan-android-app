package com.example.maiplan.utils.model

import com.example.maiplan.network.api.UserResponse

/**
 * A singleton object that holds basic user information during an active session.
 *
 * This object stores user data (Id, email, username) in memory after login,
 * fetched from a network [com.example.maiplan.network.api.UserResponse].
 *
 * ## Responsibilities:
 * - Initializing user session data after successful authentication.
 * - Clearing session data when the user logs out.
 *
 * @see com.example.maiplan.network.api.UserResponse
 */
object UserSession {
    /** The unique user Id. */
    var userId: Int? = null

    /** The user's email address. */
    var email: String? = null

    /** The user's username. */
    var username: String? = null

    /**
     * Initializes the user session with the provided [data] from the API.
     *
     * @param data The [com.example.maiplan.network.api.UserResponse] containing user information.
     *
     * @see com.example.maiplan.network.api.UserResponse
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