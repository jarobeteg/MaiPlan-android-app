package com.example.maiplan.utils

import com.example.maiplan.network.UserResponse

object UserSession {
    var userId: Int? = null
    var email: String? = null
    var username: String? = null

    fun init(data: UserResponse) {
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