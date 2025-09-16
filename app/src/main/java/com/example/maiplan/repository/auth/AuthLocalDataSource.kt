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

    suspend fun getPendingUsers(): List<AuthEntity> {
        return authDAO.getPendingUsers()
    }

    suspend fun markSynced(userIds: List<Int>): Boolean {
        return authDAO.markUsersAsSynced(userIds)
    }
}