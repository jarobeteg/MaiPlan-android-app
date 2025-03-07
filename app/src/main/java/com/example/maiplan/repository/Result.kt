package com.example.maiplan.repository

sealed class Result<out T> {
    data object Idle : Result<Nothing>()
    data class Success<out T>(val data: T) : Result<T>()
    data class Failure(val errorCode: Int) : Result<Nothing>()
    data class Error(val exception: Exception) : Result<Nothing>()
}