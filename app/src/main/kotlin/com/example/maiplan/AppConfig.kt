package com.example.maiplan

import android.content.Context

object AppConfig {
    fun apiBaseUrl(context: Context): String =
        context.getString(R.string.api_base_url)
}