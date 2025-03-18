package com.example.maiplan.repository

import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Response

fun <T> handleResponse(response: Response<T>): Result<T> {
    return if (response.isSuccessful) {
        response.body()?.let {
            Result.Success(it)
        } ?: Result.Failure(-1)
    } else {
        val errorBody = response.errorBody()?.string()
        val json = Gson().fromJson(errorBody, JsonObject::class.java)
        val errorDetail = json.getAsJsonObject("detail")
        val errorCode = errorDetail?.get("code")?.asInt ?: -1
        Result.Failure(errorCode)
    }
}

sealed class Result<out T> {
    data object Idle : Result<Nothing>()
    data class Success<out T>(val data: T) : Result<T>()
    data class Failure(val errorCode: Int) : Result<Nothing>()
    data class Error(val exception: Exception) : Result<Nothing>()
}