package com.example.maiplan.repository

import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Response

/**
 * Processes an HTTP [Response] and converts it into a [Result].
 *
 * - Returns [Result.Success] if the response is successful and contains a body.
 * - Returns [Result.Failure] if the server responds with an error, extracting the error code.
 * - Returns [Result.Failure] with code -1 if no body is found or parsing fails.
 *
 * @param response The HTTP [Response] from Retrofit.
 * @return A [Result] representing the outcome of the network request.
 */
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

/**
 * Represents the possible outcomes of a repository operation.
 */
sealed class Result<out T> {
    /**
     * Represents an idle or uninitialized state (no operation in progress).
     */
    data object Idle : Result<Nothing>()

    /**
     * Represents a successful operation.
     *
     * @property data The result data.
     */
    data class Success<out T>(val data: T) : Result<T>()

    /**
     * Represents a failed operation with a server-provided error code.
     *
     * @property errorCode The error code returned by the server, or -1 if unavailable.
     */
    data class Failure(val errorCode: Int) : Result<Nothing>()

    /**
     * Represents an operation that failed due to an exception (for example, network failure).
     *
     * @property exception The thrown exception.
     */
    data class Error(val exception: Exception) : Result<Nothing>()
}