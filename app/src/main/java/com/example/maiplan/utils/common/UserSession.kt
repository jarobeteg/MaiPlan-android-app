package com.example.maiplan.utils.common

import com.example.maiplan.network.api.UserResponse

/**
 * A singleton object that holds basic user information during an active session.
 *
 * This object stores user data (Id, email, username) in memory after login, fetched from a network [UserResponse].
 *
 * ## Responsibilities:
 * - Initializing user session data after successful authentication.
 * - Clearing session data when the user logs out.
 *
 * @property userId The unique user Id.
 * @property email The user's email address.
 * @property username The user's username.
 *
 * @see UserResponse
 */
object UserSession {
    var userId: Int? = null

    var email: String? = null

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