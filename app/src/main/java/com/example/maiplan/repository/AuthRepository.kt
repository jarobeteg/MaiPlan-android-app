package com.example.maiplan.repository

import com.example.maiplan.network.ApiService
import com.example.maiplan.network.Token
import com.example.maiplan.network.UserRegister
import com.example.maiplan.network.UserLogin
import com.example.maiplan.network.UserResetPassword
import com.example.maiplan.network.UserResponse
import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Response

class AuthRepository(private val apiService: ApiService) {

    suspend fun register(user: UserRegister): Result<Token> {
        return try {
            handleResponse(apiService.register(user))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun login(user: UserLogin): Result<Token> {
        return try {
            handleResponse(apiService.login(user))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun resetPassword(user: UserResetPassword): Result<Token> {
        return try {
            handleResponse(apiService.resetPassword(user))
        } catch (e: Exception){
            Result.Error(e)
        }
    }

    suspend fun tokenRefresh(token: String): Result<Token> {
        return try {
            handleResponse(apiService.tokenRefresh(token))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getProfile(token: String): Result<UserResponse> {
        return try {
            handleResponse(apiService.getProfile(token))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    private fun <T> handleResponse(response: Response<T>): Result<T> {
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
}