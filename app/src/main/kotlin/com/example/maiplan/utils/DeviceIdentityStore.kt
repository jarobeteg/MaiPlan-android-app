package com.example.maiplan.utils

import android.content.Context
import androidx.core.content.edit
import java.util.UUID

class DeviceIdentityStore(context: Context) {
    private companion object {
        const val DEVICE_ID = "device_id"
        val DEVICE_ID_LOCK = Any()
    }

    private val preferences = context.applicationContext
        .getSharedPreferences("tide_device_id", Context.MODE_PRIVATE)

    fun getOrCreateDeviceId(): UUID = synchronized(DEVICE_ID_LOCK) {
        val existingDeviceId = preferences.getString(DEVICE_ID, null)

        if (existingDeviceId != null) {
            runCatching { UUID.fromString(existingDeviceId) }.getOrNull()?.let { return it }
        }

        val newDeviceId = UUID.randomUUID()

        preferences.edit(commit = true) { putString(DEVICE_ID, newDeviceId.toString()) }

        return newDeviceId
    }
}