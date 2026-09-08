package com.example.maiplan.repository.auth

import android.content.Context
import androidx.room.withTransaction
import com.example.maiplan.database.MaiPlanDatabase
import com.example.maiplan.database.dao.UserDAO
import com.example.maiplan.database.entities.UserEntity
import com.example.maiplan.network.api.UserResponse
import com.example.maiplan.utils.mapper.toUserEntity
import java.util.UUID

class UserLocalDataSource(context: Context) {

    private val appContext = context.applicationContext

    private val database: MaiPlanDatabase by lazy {
        MaiPlanDatabase.getDatabase(appContext)
    }

    private val userDAO: UserDAO by lazy {
        database.userDAO()
    }

    suspend fun reconcileServerUser(response: UserResponse): UserEntity {
        return database.withTransaction {
            val incomingUser: UserEntity = response.toUserEntity()
            val existingUser: UserEntity? = userDAO.getUserBySyncId(incomingUser.syncId)

            if (existingUser == null) {
                val generatedLocalId = userDAO.insertUser(incomingUser)

                incomingUser.copy(
                    userLocalId = generatedLocalId
                )
            } else {
                check(existingUser.email == incomingUser.email) {
                    "Server returned a different email for user ${incomingUser.syncId}"
                }

                userDAO.applyServerUser(
                    username = incomingUser.username,
                    serverVersion = response.serverVersion,
                    createdAt = incomingUser.createdAt,
                    updatedAt = incomingUser.updatedAt,
                    deletedAt = incomingUser.deletedAt,
                    syncId = incomingUser.syncId
                )

                userDAO.getUserBySyncId(incomingUser.syncId)
                    ?: error("User disappeared during server reconciliation")
            }
        }
    }

    suspend fun getActiveUserBySyncId(syncId: UUID): UserEntity? {
        return userDAO.getActiveUserBySyncId(syncId)
    }
}
