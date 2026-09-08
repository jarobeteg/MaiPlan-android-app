package com.example.maiplan.repository

import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CancellationException
import retrofit2.Response

fun <T> handleRemoteResponse(response: Response<T>): Result<T> {
    return if (response.isSuccessful) {
        response.body()?.let {
            Result.Success(it)
        } ?: Result.Failure(
            errorCode = -1,
            httpStatus = response.code()
        )
    } else {
        val errorCode = runCatching {
            val errorBody = response.errorBody()?.string()
            val json = Gson().fromJson(errorBody, JsonObject::class.java)
            val errorDetail = json?.getAsJsonObject("detail")

            errorDetail?.get("code")?.asInt
        }.getOrNull() ?: -1

        Result.Failure(
            errorCode = errorCode,
            httpStatus = response.code()
        )
    }
}

suspend fun <T> handleLocalResponse(block: suspend () -> T): Result<T> {
    return try {
        val data = block()
        Result.Success(data)
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        Result.Error(exception)
    }
}

sealed class Result<out T> {
    data object Idle : Result<Nothing>()
    data object Loading : Result<Nothing>()
    data class Success<out T>(val data: T) : Result<T>()
    data class Failure(val errorCode: Int, val httpStatus: Int? = null) : Result<Nothing>()
    data class Error(val exception: Exception) : Result<Nothing>()
}

inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> = when (this) {
    is Result.Success -> Result.Success(transform(data))
    is Result.Failure -> Result.Failure(errorCode, httpStatus)
    is Result.Error -> Result.Error(exception)
    is Result.Loading -> Result.Loading
    is Result.Idle -> Result.Idle
}

fun <T> Result<List<T>>.orEmptyList(): List<T> = when (this) {
    is Result.Success -> data
    else -> emptyList()
}
