package com.example.maiplan.utils.common

import com.example.maiplan.network.api.UserResponse

object UserSession {
    var userId: Int? = null
    var email: String? = null
    var username: String? = null

    fun setup(user: UserResponse) {
        userId = user.id
        email = user.email
        username = user.username
    }

    fun clear() {
        userId = null
        email = null
        username = null
    }

    fun isLoggedIn(): Boolean {
        return userId != null && email != null && username != null
    }
}