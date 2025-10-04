package com.example.maiplan.repository.auth

import android.content.Context
import com.example.maiplan.database.MaiPlanDatabase
import com.example.maiplan.database.dao.AuthDAO
import com.example.maiplan.database.entities.AuthEntity

class AuthLocalDataSource(private val context: Context) {

    private val database: MaiPlanDatabase by lazy {
        MaiPlanDatabase.getDatabase(context)
    }

    private val authDAO: AuthDAO by lazy {
        database.authDAO()
    }

    suspend fun register(user: AuthEntity) {
        authDAO.insertUser(user)
    }

    suspend fun getPendingUser(email: String): AuthEntity? {
        return authDAO.getPendingUser(email)
    }

    suspend fun authSync(entity: AuthEntity): Int {
        var result = 0
        if (entity.userId == 0) {
            result = authDAO.authDataUpdate(
                email = entity.email,
                username = entity.username,
                balance = entity.balance,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt,
                passwordHash = entity.passwordHash,
                lastModified = entity.lastModified,
                syncState = entity.syncState,
                isDeleted = entity.isDeleted,
                serverId = entity.serverId ?: 0
            )

            if (result == 0) {
                result = authDAO.authDataInsert(
                    email = entity.email,
                    username = entity.username,
                    balance = entity.balance,
                    createdAt = entity.createdAt,
                    updatedAt = entity.updatedAt,
                    passwordHash = entity.passwordHash,
                    lastModified = entity.lastModified,
                    syncState = entity.syncState,
                    isDeleted = entity.isDeleted,
                    serverId = entity.serverId ?: 0
                )
            }
        } else {
            result = authDAO.authSync(entity)
        }
        return result
    }

    suspend fun deleteUser(entity: AuthEntity): Int {
        return authDAO.deleteUser(entity)
    }
}