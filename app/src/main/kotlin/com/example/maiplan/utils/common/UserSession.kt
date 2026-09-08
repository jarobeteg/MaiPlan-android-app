package com.example.maiplan.utils.common

import com.example.maiplan.database.entities.UserEntity
import java.util.UUID

object UserSession {
    var userLocalId: Long? = null
        private set

    var userSyncId: UUID? = null
        private set

    var email: String? = null
        private set

    var username: String? = null
        private set

    fun setup(user: UserEntity) {
        userLocalId = user.userLocalId
        userSyncId = user.syncId
        email = user.email
        username = user.username
    }

    fun clear() {
        userLocalId = null
        userSyncId = null
        email = null
        username = null
    }

    fun isLoggedIn(): Boolean {
        return userLocalId != null && userSyncId != null
    }
}