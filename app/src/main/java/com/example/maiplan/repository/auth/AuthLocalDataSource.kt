package com.example.maiplan.repository.auth

import android.content.Context
import com.example.maiplan.database.MaiPlanDatabase
import com.example.maiplan.database.dao.AuthDAO
import com.example.maiplan.database.entities.AuthEntity
import com.example.maiplan.database.entities.AuthEntityResponse
import com.example.maiplan.network.api.UserLogin
import com.example.maiplan.network.api.UserResponse
import com.example.maiplan.utils.common.PasswordUtils

class AuthLocalDataSource(private val context: Context) {

    private val database: MaiPlanDatabase by lazy {
        MaiPlanDatabase.getDatabase(context)
    }

    private val authDAO: AuthDAO by lazy {
        database.authDAO()
    }

    suspend fun getPendingUser(email: String): AuthEntity? {
        return authDAO.getPendingUser(email)
    }

    suspend fun authSync(entity: AuthEntity) {
        authDAO.authSync(entity)
    }

    suspend fun deleteUser(entity: AuthEntity): Int {
        return authDAO.deleteUser(entity)
    }

    suspend fun login(user: UserLogin): AuthEntityResponse? {
        val authEntityResponse = authDAO.loginUser(user.email)
        println("auth entity response: $authEntityResponse")
        return if (authEntityResponse != null) {
            if (PasswordUtils.verifyPassword(user.password, authEntityResponse.passwordHash)) {
                authEntityResponse
            } else {
                null
            }
        } else {
            null
        }
    }
}