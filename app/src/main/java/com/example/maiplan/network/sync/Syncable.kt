package com.example.maiplan.network.sync

interface Syncable {
    suspend fun sync()
}