package com.example.maiplan.repository

import com.example.maiplan.network.ApiService
import com.example.maiplan.network.Token
import com.example.maiplan.network.UserRegister
import com.example.maiplan.network.UserLogin
import com.example.maiplan.network.UserResponse
import com.google.gson.Gson
import com.google.gson.JsonObject

class AuthRepository(private val apiService: ApiService) {

    suspend fun register(user: UserRegister): Result<UserResponse> {
        return try {
            val response = apiService.register(user)
            if (response.isSuccessful) {
                Result.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                val json = Gson().fromJson(errorBody, JsonObject::class.java)
                val errorDetail = json.getAsJsonObject("detail")
                val errorCode = errorDetail.get("code").asInt
                Result.Failure(errorCode)
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun login(user: UserLogin): Result<Token> {
        return try {
            val response = apiService.login(user)
            if (response.isSuccessful) {
                Result.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                val json = Gson().fromJson(errorBody, JsonObject::class.java)
                val errorDetail = json.getAsJsonObject("detail")
                val errorCode = errorDetail.get("code").asInt
                Result.Failure(errorCode)
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getProfile(token: String): Result<UserResponse> {
        return try {
            val response = apiService.getProfile(token)
            if (response.isSuccessful) {
                Result.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                val json = Gson().fromJson(errorBody, JsonObject::class.java)
                val errorDetail = json.getAsJsonObject("detail")
                val errorCode = errorDetail.get("code").asInt
                Result.Failure(errorCode)
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    sealed class Result<out T> {
        data class Success<out T>(val data: T) : Result<T>()
        data class Failure(val errorCode: Int) : Result<Nothing>()
        data class Error(val exception: Exception) : Result<Nothing>()
    }
}