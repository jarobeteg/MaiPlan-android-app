package com.example.maiplan.utils.common

import com.example.maiplan.network.api.UserResponse

object UserSession {
    var userId: Int? = null
    var email: String? = null
    var username: String? = null

    fun setup(data: UserResponse) {
        userId = data.id
        email = data.email
        username = data.username
    }

    fun clear() {
        userId = null
        email = null
        username = null
    }
}