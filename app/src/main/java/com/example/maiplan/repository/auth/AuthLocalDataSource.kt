package com.example.maiplan.repository.auth

import android.content.Context
import com.example.maiplan.database.MaiPlanDatabase
import com.example.maiplan.database.dao.AuthDAO

class AuthLocalDataSource(private val context: Context) {

    private val database: MaiPlanDatabase by lazy {
        MaiPlanDatabase.getDatabase(context)
    }

    private val authDAO: AuthDAO by lazy {
        database.authDAO()
    }
}